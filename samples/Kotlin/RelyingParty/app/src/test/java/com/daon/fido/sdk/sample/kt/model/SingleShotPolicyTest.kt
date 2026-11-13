package com.daon.fido.sdk.sample.kt.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SingleShotPolicyTest {
    @Test
    fun `missing parameter defaults to all registered`() {
        assertEquals(SingleShotPolicy.ALL_REGISTERED, SingleShotPolicy.fromParameter(null))
    }

    @Test
    fun `every selection round trips through the service parameter`() {
        SingleShotPolicy.entries.forEach { policy ->
            assertEquals(policy, SingleShotPolicy.fromParameter(policy.name))
        }
    }

    @Test
    fun `invalid parameter fails instead of falling back to OR`() {
        for (value in listOf("", "unknown", "silent_and_fingerprint")) {
            assertThrows(IllegalArgumentException::class.java) {
                SingleShotPolicy.fromParameter(value)
            }
        }
    }

    @Test
    fun `four modes have distinct labels`() {
        assertEquals(4, SingleShotPolicy.entries.size)
        assertEquals(4, SingleShotPolicy.entries.map { it.labelResId }.toSet().size)
    }
}
