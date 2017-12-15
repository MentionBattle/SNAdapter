package org.mentionbattle.snadapter.impl.startup

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.mentionbattle.snadapter.api.core.SocialNetwork
import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer
import org.mentionbattle.snadapter.api.core.socialnetworks.SocialNetworkHandler
import org.mentionbattle.snadapter.impl.common.Core
import org.mentionbattle.snadapter.impl.eventsystem.ExitEvent
import org.mentionbattle.snadapter.impl.eventsystem.PrimitiveEventQueue
import org.mentionbattle.snadapter.impl.startup.components.ComponentSystem
import org.mentionbattle.snadapter.impl.startup.components.ReflectionComponent
import org.mentionbattle.snadapter.impl.startup.configuration.Configuration
import org.mentionbattle.snadapter.impl.startup.configuration.ConfigurationParser
import kotlin.concurrent.thread

class StartUpManager(val configuration : Configuration, packages : List<String>) : AutoCloseable {

    internal lateinit var socialNetworks: Map<String, SocialNetworkHandler>
    private val threads = mutableListOf<Thread>()

    init {
        //setup components
        setupComponents(configuration, packages)
        //setup social networks
        socialNetworks = setupSocialNetworks(configuration)
    }

    private fun setupComponents(configuration: Configuration, packages: List<String>) {
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

    private fun setupSocialNetworks(configuration: Configuration): Map<String, SocialNetworkHandler> {
        val result = mutableMapOf<String, SocialNetworkHandler>()
        val reflection = ComponentSystem.getComponent(ReflectionComponent::class.java) as ReflectionComponent
        val socialNetworks = reflection.getAnnotatedTypes(SocialNetwork::class.java)
        for (s in socialNetworks) {
            val socialNetworkAnnotation = s.getAnnotation(SocialNetwork::class.java)
            if (socialNetworkAnnotation.name in configuration.socialNetworks) {
                val name = socialNetworkAnnotation.name
                try {
                    result[name] = createSocialNetwork(s)
                } catch (e: Exception) {
                    System.err.println("ERROR : social network with name $name has been crashed in constructor with:")
                    System.err.println(e.cause)
                }
            }
        }
        return result
    }

    private fun createSocialNetwork(handler: Class<*>): SocialNetworkHandler {
        val constructor = handler.constructors[0];

        var arguments = arrayOfNulls<Any>(constructor.parameterTypes.size)
        var curArgs = 0;
        for (p in constructor.parameterTypes) {
            arguments[curArgs++] = ComponentSystem.getComponent(p);
        }
        return constructor.newInstance(*arguments) as SocialNetworkHandler
    }


    suspend fun run() {
        val core = ComponentSystem.getComponent(Core::class.java) as Core
        launch {
            core.run(configuration)
        }
        for (k in socialNetworks.keys) {
            threads.add(
                    thread {
                        try {
                            socialNetworks[k]?.processData()
                        } catch (e: Exception) {
                            System.err.println("ERROR :: Social network with name $k has been crashed in process data with:")
                            System.err.println(e)
                        }
                    }
            )
        }
    }

    override fun close() {
        System.err.println("Log :: Shutdown started...")
        var eventQueue = ComponentSystem.getComponent(PrimitiveEventQueue::class.java) as PrimitiveEventQueue
        eventQueue.addEvent(ExitEvent())
        threads.forEach({ j ->
            j.join()
        })
    }

}