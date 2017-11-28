package org.mentionbattle.snadapter.impl.startup.components

import org.mentionbattle.snadapter.api.core.Component
import org.reflections.Reflections
import org.reflections.util.FilterBuilder
import org.reflections.util.ClasspathHelper
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import java.util.LinkedList

object ComponentSystem {

    private lateinit var components:MutableMap<Class<out Any>, Any>

    private var isInitialized = false;


    suspend fun setup(reflectionComponent: ReflectionComponent, defaultComponents : List<Any>) {

        if (isInitialized) {
            throw IllegalStateException("Component system has been already initialized")
        }
        components = mutableMapOf()
        defaultComponents.forEach({p -> components[p.javaClass] = p})
        components[ReflectionComponent::class.java] = reflectionComponent

        val componentsCollection = mutableSetOf<Class<out Any>>()
        componentsCollection.addAll(reflectionComponent.getAnnotatedTypes(Component::class.java));

        for (c in componentsCollection)
            if (!components.containsKey(c))
                injectComponent(c, componentsCollection, mutableSetOf())

        isInitialized = true;
    }

    private fun injectComponent(c: Class<out Any>, componentCollection: Set<Class<out Any>>,
                        visitedComponents : MutableSet<Class<out Any>>) {

        if (visitedComponents.contains(c)) {
            throw Exception()
        }
        if (c.constructors.count() > 1)
            throw Exception()

        visitedComponents.add(c);

        val constructor = c.constructors[0];
        val parameters = constructor.parameterTypes

        var arguments = arrayOfNulls<Any>(parameters.size)
        var curArgs = 0;
        for (p in parameters) {
            if (!(componentCollection.contains(p) || components.containsKey(p))) {
                throw Exception()
            }
            if (!components.containsKey(p)) {
                injectComponent(p as Class<out Any>, componentCollection, visitedComponents)
            }

            var t = components.get(p);
            if (t != null) {
                arguments[curArgs++] = t
            }
        }
        components[c] = constructor.newInstance(*arguments) as Any
    }

    fun getComponent(c : Class<out Any>): Any? {
        return components[c];
    }
}