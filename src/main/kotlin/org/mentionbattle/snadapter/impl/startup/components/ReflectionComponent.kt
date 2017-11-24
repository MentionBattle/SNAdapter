package org.mentionbattle.snadapter.impl.startup.components

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.util.*

class ReflectionComponent(packages:List<String>) {
    private val reflections : Reflections = createReflections(packages)

    private fun createReflections(packages:List<String>) : Reflections {
        var filterBuilder = FilterBuilder();
        packages.forEach { p -> filterBuilder.includePackage(p) }

        val classLoadersList = LinkedList<ClassLoader>()
        classLoadersList.add(ClasspathHelper.contextClassLoader())
        classLoadersList.add(ClasspathHelper.staticClassLoader())

        return Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner(), TypeAnnotationsScanner())
                .setUrls(ClasspathHelper.forClassLoader(*classLoadersList.toTypedArray()))
                .filterInputsBy(filterBuilder))
    }

    public fun getAnnotatedTypes(annotation: Class<out Annotation>): MutableSet<Class<*>> {
        return reflections.getTypesAnnotatedWith(annotation)
    }
}