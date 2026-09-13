package com.rique.maillite.core.push

import org.junit.Assert.assertEquals
import org.junit.Test

class PushMessageParserTest {

    private val parser = PushMessageParser()

    @Test
    fun `usa o titulo do payload quando presente`() {
        // Arrange
        val title = "Nova mensagem de Maria"
        val body = "Confira o assunto..."
        val defaultTitle = "MailLite"

        // Act
        val result = parser.parse(title = title, body = body, defaultTitle = defaultTitle)

        // Assert
        assertEquals(title, result.title)
        assertEquals(body, result.body)
    }

    @Test
    fun `usa o titulo padrao quando o payload nao traz titulo`() {
        // Arrange
        val defaultTitle = "MailLite"

        // Act
        val result = parser.parse(title = null, body = "Conteúdo qualquer", defaultTitle = defaultTitle)

        // Assert
        assertEquals(defaultTitle, result.title)
    }

    @Test
    fun `usa string vazia quando o payload nao traz corpo`() {
        // Arrange
        val defaultTitle = "MailLite"

        // Act
        val result = parser.parse(title = "Título qualquer", body = null, defaultTitle = defaultTitle)

        // Assert
        assertEquals("", result.body)
    }
}