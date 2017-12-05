package org.mentionbattle.snadapter.impl.startup

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.CoreListener
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.startup.components.ComponentSystem
import org.mentionbattle.snadapter.impl.startup.components.ReflectionComponent
import org.mentionbattle.snadapter.impl.startup.configuration.Configuration
import org.mentionbattle.snadapter.impl.startup.configuration.ConfigurationParser

class StartUpManager : AutoCloseable{

    private lateinit var socialNetworks : Map<String, SocialNetworkHandler>
    private lateinit var configuration : Configuration

    fun initialize(packages : List<String>) {
        configuration = ConfigurationParser().parse("sna.config")
        //setup components
        setupComponents(configuration, packages)

        //setup social networks
        socialNetworks = setupSocialNetworks(configuration)
    }

    private fun setupComponents(configuration: Configuration, packages : List<String>) {
        val reflectionComponent = ReflectionComponent(packages)
        val initializers = reflectionComponent.getAnnotatedTypes(SocialNetworkInitializer::class.java)
        val defaultComponents = mutableListOf<Any>()

        for (i in initializers) {
            val annotation = i.getAnnotation(SocialNetworkInitializer::class.java)
            val r = i.constructors[0].newInstance(configuration.socialNetworkInitializers[annotation.name])
            defaultComponents.add(r)
        }

        runBlocking {
            ComponentSystem.setup(reflectionComponent, defaultComponents)
        }
    }

    private fun setupSocialNetworks(configuration: Configuration) : Map<String, SocialNetworkHandler> {
        val result = mutableMapOf<String, SocialNetworkHandler>()
        val reflection = ComponentSystem.getComponent(ReflectionComponent::class.java) as ReflectionComponent
        val socialNetworks = reflection.getAnnotatedTypes(SocialNetwork::class.java)
        for (s in socialNetworks) {
            val socialNetworkAnnotation = s.getAnnotation(SocialNetwork::class.java)
            if (socialNetworkAnnotation.name in configuration.socialNetworks) {
                val name = socialNetworkAnnotation.name
                try {
                    result[name] = createSocialNetwork(s)
                } catch (e : Exception) {
                    System.err.println("ERROR : social network with name $name has been crashed in constructor with:")
                    System.err.println(e.cause)
                }
            }
        }
        return result
    }

    private fun createSocialNetwork(handler : Class<*>) : SocialNetworkHandler {
        val constructor = handler.constructors[0];

        var arguments = arrayOfNulls<Any>(constructor.parameterTypes.size)
        var curArgs = 0;
        for (p in constructor.parameterTypes) {
            arguments[curArgs++] = ComponentSystem.getComponent(p);
        }
        return constructor.newInstance(*arguments) as SocialNetworkHandler
    }


    suspend fun run() {
        val jobs = mutableListOf<Job>()
        val listener = ComponentSystem.getComponent(CoreListener::class.java) as CoreListener
        launch {
            listener.run(configuration.port)
        }
        for (k in socialNetworks.keys) {
            jobs.add(
                    launch {
                        socialNetworks[k]?.processData()
                    }
            )
        }
        while (true) {
            val result = readLine()
            if (result.equals("exit")) {
                break
            }
        }

        println("Shutdown starting...")
        var eventQueue = ComponentSystem.getComponent(PrimitiveEventQueue::class.java) as PrimitiveEventQueue
        eventQueue.addEvent(ExitEvent())
        jobs.forEach({j ->
            j.join()
        })
    }

    override fun close() {

    }

}