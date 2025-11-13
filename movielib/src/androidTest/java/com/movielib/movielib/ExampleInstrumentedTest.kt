package com.movielib.movielib

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Test que se ejecuta en el dispositivo android
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Contexto de la app bajo prueba
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.movielib.movielib.test", appContext.packageName)
    }
}
