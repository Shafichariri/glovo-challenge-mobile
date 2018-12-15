package com.shafic.challenge

import org.junit.Test
import java.lang.ref.WeakReference

class Owner {
    var demo: Demo? = null 
}
class Demo {
    val weakSelf: WeakReference<Demo> by lazy { WeakReference(this) }
    
    fun getFive(): Int {
        return 5
    }
}

class TestingKotlin {
    
    @Test
    fun testingTheory() {
        val demo = Demo()
        val value = demo.weakSelf.get()?.getFive() ?: 0
        assert(value == 5, lazyMessage = { print("YES THIS IS TRUE") })

        var owner: Owner? = Owner()
        owner?.demo = Demo()
        var newDemo = owner?.demo
        owner?.demo = null

        val newValue = newDemo?.weakSelf?.get()?.getFive() ?: -1
        assert(newValue == 5, lazyMessage = { print("SECOND TIME AROUND") })
    }
}
