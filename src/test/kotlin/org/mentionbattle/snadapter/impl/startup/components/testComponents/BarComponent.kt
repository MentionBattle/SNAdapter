package org.mentionbattle.snadapter.impl.startup.components.testComponents

import org.mentionbattle.snadapter.api.core.Component

@Component
public class BarComponent(foo : FooComponent) {
    val foo = foo
}