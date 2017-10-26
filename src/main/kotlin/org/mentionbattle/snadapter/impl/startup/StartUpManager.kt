package org.mentionbattle.snadapter.impl.startup

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.api.core.SocialNetworkInitializer
import org.mentionbattle.snadapter.impl.SocialNetworkAdapter
import org.mentionbattle.snadapter.impl.startup.components.ComponentSystem
import org.mentionbattle.snadapter.impl.startup.components.ReflectionComponent
import org.mentionbattle.snadapter.impl.startup.configuration.Configuration
import org.mentionbattle.snadapter.impl.startup.configuration.ConfigurationParser

class StartUpManager : AutoCloseable{

    lateinit var configuration : Configuration
    fun initialize(packages : List<String>) {
        configuration = ConfigurationParser().Parse("sna.config")
        //first : start up
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

    fun run() {
        val sna = ComponentSystem.getComponent(SocialNetworkAdapter::class.java) as SocialNetworkAdapter
        sna.run()
    }

    override fun close() {

    }

}