import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mentionbattle.snadapter.api.core.Component
import org.mentionbattle.snadapter.impl.startup.components.ComponentSystem
import org.mentionbattle.snadapter.impl.startup.components.ReflectionComponent
import testComponents.BarComponent
import testComponents.FooComponent
import testComponents.NotComponent

val packages = listOf("testComponents")

class ComponentSystemTest {

    @Before fun InitializeContext() {

    }

    @Test fun ReflectionComponentInitializerTest() {
        var reflection = ReflectionComponent(packages)
        val result = reflection.getAnnotatedTypes(Component::class.java).filter({t -> t == FooComponent::class.java}).firstOrNull()
        assert(result != null)
    }


    @Test fun SetupComponentSystemTest() {
        var reflection = ReflectionComponent(packages)
        runBlocking {
            ComponentSystem.setup(reflection, listOf())
        }

        var barComponent = ComponentSystem.getComponent(BarComponent::class.java) as BarComponent?
        assert(barComponent != null)
        assert(barComponent?.foo != null)
    }

   /* @Test fun SetupComponentSystemTest2() {
        var reflection = ReflectionComponent(packages)
        runBlocking {
            ComponentSystem.setup(reflection, listOf())
        }

        var notComponent = ComponentSystem.getComponent(NotComponent::class.java)
        assert(notComponent == null)
    }*/
}