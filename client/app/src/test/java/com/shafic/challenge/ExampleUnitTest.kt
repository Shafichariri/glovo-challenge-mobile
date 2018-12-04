package com.shafic.challenge

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //assertEquals(4, 2 + 2)

        val test = arrayListOf<Int>(1, 2, 3).reduce { accumulator, latLng ->
            print("\n$accumulator $latLng")
            return@reduce accumulator + latLng
        }
        assertEquals(test, 6)

        var some: Int = 0
        arrayListOf<Int>(1, 2, 3, 4, 5, 6, 7).forEach {
            if (it == 4) {
                print("\nIT IS HERE: $it")
                return@forEach
            }
            if (it == 5) {
                print("\nT IS HERE: $it")
                some = it
            }
        }


        assertEquals(some, 5)
    }
}
