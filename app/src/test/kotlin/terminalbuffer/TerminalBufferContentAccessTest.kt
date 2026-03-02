package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import terminalbuffer.models.Color
import terminalbuffer.models.Style
import terminalbuffer.models.TextAttributes

class TerminalBufferContentAccessTest {

    @Nested
    inner class GetCharAtAndAttributesAt {

        @Test
        fun `should return correct character and attributes from active screen`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            val attrs = TextAttributes(foreground = Color.CYAN, styles = mutableSetOf(Style.ITALIC))

            // Act
            buffer.setAttributes(attrs)
            buffer.setCursorPosition(3, 2)
            buffer.writeText("X")

            // Assert
            val retrievedAttrs = buffer.getAttributesAt(3, 2)
            assertEquals("X", buffer.getCharAt(3, 2))
            assertEquals(Color.CYAN, retrievedAttrs.foreground)
            assertTrue(retrievedAttrs.styles.contains(Style.ITALIC))
        }

        @Test
        fun `should return correct character and attributes from scrollback history`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)

            // Act
            buffer.setAttributes(TextAttributes(background = Color.YELLOW))
            buffer.setCursorPosition(5, 0)
            buffer.writeText("Z")
            buffer.insertEmptyLineAtTheBottom()

            // Assert
            assertEquals("Z", buffer.getCharAt(5, -1))
            assertEquals(Color.YELLOW, buffer.getAttributesAt(5, -1).background)
        }

        @Test
        fun `should throw IndexOutOfBoundsException for invalid column index`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)

            // Act & Assert
            assertThrows<IndexOutOfBoundsException> { buffer.getCharAt(10, 0) }
            assertThrows<IndexOutOfBoundsException> { buffer.getAttributesAt(-1, 0) }
        }

        @Test
        fun `should throw IndexOutOfBoundsException for invalid row index`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.insertEmptyLineAtTheBottom()

            // Act & Assert
            assertThrows<IndexOutOfBoundsException> { buffer.getCharAt(0, 5) }
            assertThrows<IndexOutOfBoundsException> { buffer.getAttributesAt(0, -2) }
        }
    }

    @Nested
    inner class GetScreenAsString {

        @Test
        fun `should return empty screen formatted with newlines`() {
            // Arrange
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollBack = 10)

            // Act
            val result = buffer.getScreenAsString()

            // Assert
            assertEquals("   \n   ", result)
        }

        @Test
        fun `should return screen content correctly joined by newlines`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Baba ")
            buffer.setCursorPosition(0, 2)
            buffer.writeText("Yaga ")

            // Act
            val result = buffer.getScreenAsString()

            // Assert
            val expected = "Baba \n     \nYaga "
            assertEquals(expected, result)
        }
    }

    @Nested
    inner class GetEntireContentAsString {

        @Test
        fun `should return only screen content when scrollback is empty`() {
            // Arrange
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Hi ")

            // Act
            val result = buffer.getEntireContentAsString()

            // Assert
            assertEquals("Hi \n   ", result)
        }

        @Test
        fun `should combine scrollback history and screen content correctly`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 2, maxScrollBack = 10)

            buffer.setCursorPosition(0, 0)
            buffer.writeText("Line1")
            buffer.insertEmptyLineAtTheBottom()
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Line2")

            // Act
            val result = buffer.getEntireContentAsString()

            // Assert
            val expected = "Line1\nLine2\n     "
            assertEquals(expected, result)
        }

        @Test
        fun `should respect scrollback limits when generating entire content`() {
            // Arrange
            val buffer = TerminalBuffer(width = 4, height = 1, maxScrollBack = 1)

            buffer.setCursorPosition(0, 0)
            buffer.writeText("One ")
            buffer.insertEmptyLineAtTheBottom()

            buffer.setCursorPosition(0, 0)
            buffer.writeText("Two ")
            buffer.insertEmptyLineAtTheBottom()

            buffer.setCursorPosition(0, 0)
            buffer.writeText("Curr")

            // Act
            val result = buffer.getEntireContentAsString()

            // Assert
            val expected = "Two \nCurr"
            assertEquals(expected, result)
        }

        @Test
        fun `should return only screen content when maxScrollBack is zero and lines were pushed`() {
            // Arrange
            val buffer = TerminalBuffer(width = 4, height = 2, maxScrollBack = 0)

            // Act
            buffer.writeText("Drop")
            buffer.insertEmptyLineAtTheBottom()
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Keep")
            val result = buffer.getEntireContentAsString()

            // Assert
            val expected = "Keep\n    "
            assertEquals(expected, result)
        }
    }

    @Nested
    inner class GetLineAsString {

        @Test
        fun `should return correct line from active screen`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)

            // Act
            buffer.setCursorPosition(0, 1)
            buffer.writeText("Hello")
            val line = buffer.getLineAsString(1)

            // Assert
            assertEquals("Hello", line)
        }

        @Test
        fun `should return correct line from scrollback history`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)

            // Act
            buffer.writeText("Old  ")
            buffer.insertEmptyLineAtTheBottom()
            val line = buffer.getLineAsString(-1)

            // Assert
            assertEquals("Old  ", line)
        }

        @Test
        fun `should format exception message correctly when scrollback is empty`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)

            // Act & Assert
            val exception = assertThrows<IndexOutOfBoundsException> { buffer.getLineAsString(-1) }
            assertTrue(exception.message!!.contains("(scroll back is currently empty)"))
        }

        @Test
        fun `should format exception message correctly when scrollback is populated`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)

            // Act
            buffer.insertEmptyLineAtTheBottom()
            buffer.insertEmptyLineAtTheBottom()

            // Act & Assert
            val exception = assertThrows<IndexOutOfBoundsException> { buffer.getLineAsString(-3) }
            assertTrue(exception.message!!.contains("and from -2 to -1 for scroll back"))
        }
    }
}