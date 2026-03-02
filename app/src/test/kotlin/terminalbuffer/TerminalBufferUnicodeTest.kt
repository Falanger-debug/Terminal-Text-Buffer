package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TerminalBufferUnicodeTest {

    @Nested
    inner class WriteUnicodeText {

        @Test
        fun `should write mixed ASCII and emoji characters correctly`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            val text = "Hi👋" // (👋 2 chars)

            // Act
            buffer.writeText(text)

            // Assert
            assertEquals(3, buffer.cursor.col)
            assertEquals("H", buffer.getCharAt(0, 0))
            assertEquals("i", buffer.getCharAt(1, 0))
            assertEquals("👋", buffer.getCharAt(2, 0))
            assertEquals(" ", buffer.getCharAt(3, 0))
        }

        @Test
        fun `should correctly truncate text with emojis at the edge of the screen`() {
            // Arrange
            val buffer = TerminalBuffer(width = 4, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            val text = "1🚀2🌟3"

            // Act
            buffer.writeText(text)

            // Assert
            assertEquals("1", buffer.getCharAt(0, 0))
            assertEquals("🚀", buffer.getCharAt(1, 0))
            assertEquals("2", buffer.getCharAt(2, 0))
            assertEquals("🌟", buffer.getCharAt(3, 0))
            assertEquals(3, buffer.cursor.col)
        }

        @Test
        fun `should write and retrieve Chinese (CJK) characters correctly`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            val text = "A你好B" // "A你好B" (A + Ni + Hao + B)

            // Act
            buffer.writeText(text)

            // Assert
            assertEquals(4, buffer.cursor.col)
            assertEquals("A", buffer.getCharAt(0, 0))
            assertEquals("你", buffer.getCharAt(1, 0))
            assertEquals("好", buffer.getCharAt(2, 0))
            assertEquals("B", buffer.getCharAt(3, 0))
            assertEquals(" ", buffer.getCharAt(4, 0))

            val expectedLine = "A你好B      "
            assertEquals(expectedLine, buffer.getLineAsString(0))
        }
    }

    @Nested
    inner class InsertUnicodeText {

        @Test
        fun `should insert emoji and cleanly shift existing text to the right`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("AB")
            buffer.setCursorPosition(1, 0)

            // Act
            buffer.insertText("🍎")

            // Assert
            assertEquals("A", buffer.getCharAt(0, 0))
            assertEquals("🍎", buffer.getCharAt(1, 0))
            assertEquals("B", buffer.getCharAt(2, 0))
            val expectedLine = "A🍎B       "
            assertEquals(expectedLine, buffer.getLineAsString(0))
        }

        @Test
        fun `should wrap overflowing emojis safely to the next line`() {
            // Arrange
            val buffer = TerminalBuffer(width = 3, height = 3, maxScrollBack = 100)
            buffer.setCursorPosition(2, 0)

            // Act
            buffer.insertText("🚀🌟")

            // Assert
            assertEquals("🚀", buffer.getCharAt(2, 0))
            assertEquals("🌟", buffer.getCharAt(0, 1))
            assertEquals(1, buffer.cursor.col)
            assertEquals(1, buffer.cursor.row)
        }
    }

    @Nested
    inner class UnicodeContentAccess {

        @Test
        fun `getScreenAsString should properly render multiple wide characters`() {
            // Arrange
            val buffer = TerminalBuffer(width = 4, height = 2, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("A🦀B")
            buffer.setCursorPosition(0, 1)
            buffer.writeText("XYZ")

            // Act
            val result = buffer.getScreenAsString()

            // Assert
            val expected = "A🦀B \nXYZ "
            assertEquals(expected, result)
        }

        @Test
        fun `getEntireContentAsString should preserve emojis in scrollback history`() {
            // Arrange
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("👽")

            // Act
            buffer.insertEmptyLineAtTheBottom()
            buffer.setCursorPosition(0, 0)
            buffer.writeText("OK")

            // Assert
            assertEquals("👽", buffer.getCharAt(0, -1))

            val expected = "👽  \nOK \n   "
            assertEquals(expected, buffer.getEntireContentAsString())
        }
    }

    @Nested
    inner class ResizeUnicodeText {

        @Test
        fun `should preserve unicode characters without corruption when cropping screen`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 2, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("1👽2🚀3")

            // Act
            buffer.resize(newWidth = 3, newHeight = 2)

            // Assert
            assertEquals("1👽2\n   ", buffer.getScreenAsString())
            assertEquals("👽", buffer.getCharAt(1, 0))
        }

        @Test
        fun `should push unicode characters safely to scrollback on height decrease`() {
            // Arrange
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("🦀🦀🦀")
            buffer.setCursorPosition(0, 1)
            buffer.writeText("🌟🌟🌟")

            // Act
            buffer.resize(newWidth = 3, newHeight = 1)

            // Assert
            assertEquals("🌟🌟🌟", buffer.getScreenAsString())
            assertEquals(1, buffer.currentScrollBackSize)
            assertEquals("🦀🦀🦀", buffer.getLineAsString(-1))
        }
    }
}