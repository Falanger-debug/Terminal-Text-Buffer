package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import terminalbuffer.exceptions.InvalidConfigurationException
import terminalbuffer.models.Color
import terminalbuffer.models.TextAttributes

class TerminalBufferResizeTest {

    @Nested
    inner class IncreasingSize {

        @Test
        fun `should expand screen and keep existing content intact`() {
            // Arrange
            val buffer = TerminalBuffer(width = 3, height = 2, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Top")
            buffer.setCursorPosition(0, 1)
            buffer.writeText("Bot")

            // Act
            buffer.resize(newWidth = 5, newHeight = 3)

            // Assert
            assertEquals(5, buffer.width)
            assertEquals(3, buffer.height)

            val expected = "Top  \nBot  \n     "
            assertEquals(expected, buffer.getScreenAsString())
        }

        @Test
        fun `should preserve cell attributes when resizing`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 5, maxScrollBack = 10)
            buffer.setAttributes(TextAttributes(foreground = Color.RED))
            buffer.setCursorPosition(0, 0)
            buffer.writeText("X")

            // Act
            buffer.resize(newWidth = 10, newHeight = 10)

            // Assert
            val attrs = buffer.getAttributesAt(0, 0)
            assertEquals("X", buffer.getCharAt(0, 0))
            assertEquals(Color.RED, attrs.foreground)
        }
    }

    @Nested
    inner class DecreasingSize {

        @Test
        fun `should crop text on the right when width is decreased`() {
            // Arrange
            val buffer = TerminalBuffer(width = 6, height = 2, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("123456")

            // Act
            buffer.resize(newWidth = 3, newHeight = 2)

            // Assert
            assertEquals(3, buffer.width)
            assertEquals("123\n   ", buffer.getScreenAsString())
        }

        @Test
        fun `should push top lines to scrollback when height is decreased`() {
            // Arrange
            val buffer = TerminalBuffer(width = 4, height = 4, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Row1")
            buffer.setCursorPosition(0, 1)
            buffer.writeText("Row2")
            buffer.setCursorPosition(0, 2)
            buffer.writeText("Row3")
            buffer.setCursorPosition(0, 3)
            buffer.writeText("Row4")

            // Act
            buffer.resize(newWidth = 4, newHeight = 2)

            // Assert
            assertEquals("Row3\nRow4", buffer.getScreenAsString())
            assertEquals(2, buffer.currentScrollBackSize)
            assertEquals("Row2", buffer.getLineAsString(-1))
            assertEquals("Row1", buffer.getLineAsString(-2))
        }

        @Test
        fun `should combine cropping and scrollback pushing correctly`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("12345")
            buffer.setCursorPosition(0, 1)
            buffer.writeText("ABCDE")
            buffer.setCursorPosition(0, 2)
            buffer.writeText("VWXYZ")

            // Act
            buffer.resize(newWidth = 3, newHeight = 2)

            // Assert
            assertEquals("ABC\nVWX", buffer.getScreenAsString())
            assertEquals(1, buffer.currentScrollBackSize)
            assertEquals("12345", buffer.getLineAsString(-1))
        }

        @Test
        fun `should discard top lines when height is decreased and maxScrollBack is zero`() {
            // Arrange
            val buffer = TerminalBuffer(width = 4, height = 3, maxScrollBack = 0)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("1111")
            buffer.setCursorPosition(0, 1)
            buffer.writeText("2222")
            buffer.setCursorPosition(0, 2)
            buffer.writeText("3333")

            // Act
            buffer.resize(newWidth = 4, newHeight = 2)

            // Assert
            assertEquals("2222\n3333", buffer.getScreenAsString())
            assertEquals(0, buffer.currentScrollBackSize)
        }
    }

    @Nested
    inner class UnchangedSize {
        @Test
        fun `should do nothing and keep existing content when resizing to the same dimensions`() {
            // Arrange
            val buffer = TerminalBuffer(width = 3, height = 3, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("ABC")

            // Act
            buffer.resize(newWidth = 3, newHeight = 3)

            // Assert
            assertEquals(3, buffer.width)
            assertEquals(3, buffer.height)
            assertEquals("ABC\n   \n   ", buffer.getScreenAsString())
            assertEquals(0, buffer.currentScrollBackSize)
        }
    }

    @Nested
    inner class CursorClamping {

        @Test
        fun `should clamp cursor to new bounds if it was left outside`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 10, maxScrollBack = 100)
            buffer.setCursorPosition(8, 8)

            // Act
            buffer.resize(newWidth = 4, newHeight = 4)

            // Assert
            assertEquals(3, buffer.cursor.col)
            assertEquals(3, buffer.cursor.row)
        }

        @Test
        fun `should not move cursor if it is within new bounds`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 10, maxScrollBack = 100)
            buffer.setCursorPosition(2, 2)

            // Act
            buffer.resize(newWidth = 4, newHeight = 4)

            // Assert
            assertEquals(2, buffer.cursor.col)
            assertEquals(2, buffer.cursor.row)
        }
    }

    @Nested
    inner class InvalidBounds {

        @Test
        fun `should throw exception for invalid new dimensions`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 10, maxScrollBack = 100)

            // Act & Assert
            assertThrows<InvalidConfigurationException> { buffer.resize(0, 10) }
            assertThrows<InvalidConfigurationException> { buffer.resize(10, 0) }
            assertThrows<InvalidConfigurationException> { buffer.resize(TerminalBuffer.MAX_WIDTH + 1, 10) }
            assertThrows<InvalidConfigurationException> { buffer.resize(10, TerminalBuffer.MAX_HEIGHT + 1) }
        }
    }
}