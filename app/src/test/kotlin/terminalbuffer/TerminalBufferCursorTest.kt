package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TerminalBufferCursorTest {

    @Nested
    inner class SetPosition {
        @Test
        fun `should set cursor to valid position`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)

            // Act
            buffer.setCursorPosition(10, 5)

            // Assert
            assertEquals(10, buffer.cursor.col)
            assertEquals(5, buffer.cursor.row)
        }

        @Test
        fun `should clamp cursor to zero when setting negative coordinates`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)

            // Act
            buffer.setCursorPosition(-5, -10)

            // Assert
            assertEquals(0, buffer.cursor.col)
            assertEquals(0, buffer.cursor.row)
        }

        @Test
        fun `should clamp cursor to max bounds when setting exceeding coordinates`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)

            // Act
            buffer.setCursorPosition(100, 50)

            // Assert
            assertEquals(79, buffer.cursor.col)
            assertEquals(23, buffer.cursor.row)
        }
    }

    @Nested
    inner class MoveCursor {
        @Test
        fun `should move cursor up and left correctly within bounds`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)
            buffer.setCursorPosition(10, 10)

            // Act
            buffer.moveCursor(up = 2, left = 3)

            // Assert
            assertEquals(7, buffer.cursor.col)
            assertEquals(8, buffer.cursor.row)
        }

        @Test
        fun `should move cursor down and right correctly within bounds`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)
            buffer.setCursorPosition(10, 10)

            // Act
            buffer.moveCursor(down = 5, right = 10)

            // Assert
            assertEquals(20, buffer.cursor.col)
            assertEquals(15, buffer.cursor.row)
        }

        @Test
        fun `should clamp to top-left edge when moving excessively outside screen bounds`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)
            buffer.setCursorPosition(40, 12)

            // Act
            buffer.moveCursor(up = 100, left = 100)

            // Assert
            assertEquals(0, buffer.cursor.col)
            assertEquals(0, buffer.cursor.row)
        }

        @Test
        fun `should clamp to bottom-right edge when moving excessively outside screen bounds`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)
            buffer.setCursorPosition(40, 12)

            // Act
            buffer.moveCursor(down = 100, right = 100)

            // Assert
            assertEquals(79, buffer.cursor.col)
            assertEquals(23, buffer.cursor.row)
        }
    }
}