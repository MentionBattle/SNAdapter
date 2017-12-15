package org.mentionbattle.snadapter.impl.startup.components

import kotlinx.coroutines.experimental.runBlocking
import org.junit.AfterClass
import org.junit.Test
import org.junit.Before
import org.junit.BeforeClass
import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.impl.startup.components.testComponents.BarComponent
import org.mentionbattle.snadapter.impl.startup.components.testComponents.FooComponent
import org.mentionbattle.snadapter.impl.startup.components.testComponents.NotComponent

val packages = listOf("org/mentionbattle/snadapter/impl/startup/components/testComponents")

class ComponentSystemTest {
    companion object {
        @BeforeClass
        @JvmStatic fun setup() {
            var reflection = ReflectionComponent(packages)
            runBlocking {
                ComponentSystem.setup(reflection, listOf())
            }
        }

        @AfterClass
        @JvmStatic fun destroy() {
            ComponentSystem.destroy()
        }
    }


    @Test fun ReflectionComponentInitializerTest() {
        var reflection = ReflectionComponent(packages)
        val result = reflection.getAnnotatedTypes(Component::class.java).filter({t -> t == FooComponent::class.java}).firstOrNull()
        assert(result != null)
    }


    @Test fun SetupComponentSystemTest() {
        var barComponent = ComponentSystem.getComponent(BarComponent::class.java) as BarComponent
        assert(barComponent != null)
        assert(barComponent?.foo != null)
    }

    @Test fun SetupComponentSystemTest2() {
        var notComponent = ComponentSystem.getComponent(NotComponent::class.java)
        assert(notComponent == null)
    }
}