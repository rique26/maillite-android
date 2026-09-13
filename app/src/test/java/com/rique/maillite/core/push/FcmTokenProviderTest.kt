package com.rique.maillite.core.push

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FcmTokenProviderTest {

    private lateinit var firebaseMessaging: FirebaseMessaging
    private lateinit var task: Task<String>
    private lateinit var provider: FcmTokenProvider

    @Before
    fun setUp() {
        firebaseMessaging = mockk()
        task = mockk(relaxed = true)
        provider = FcmTokenProvider(firebaseMessaging)

        every { firebaseMessaging.token } returns task
    }

    @Test
    fun `getCurrentToken retorna o token quando o Firebase responde com sucesso`() = runTest {
        // Arrange
        val expectedToken = "fcm-token-abc123"
        val successSlot = slot<OnSuccessListener<String>>()
        every { task.addOnSuccessListener(capture(successSlot)) } answers {
            successSlot.captured.onSuccess(expectedToken)
            task
        }
        every { task.addOnFailureListener(any()) } returns task

        // Act
        val result = provider.getCurrentToken()

        // Assert
        assertEquals(expectedToken, result)
    }

    @Test
    fun `getCurrentToken retorna null quando o Firebase falha em obter o token`() = runTest {
        // Arrange
        val failureSlot = slot<OnFailureListener>()
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(capture(failureSlot)) } answers {
            failureSlot.captured.onFailure(Exception("sem Google Play Services"))
            task
        }

        // Act
        val result = provider.getCurrentToken()

        // Assert
        assertNull(result)
    }
}