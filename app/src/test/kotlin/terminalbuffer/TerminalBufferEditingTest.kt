package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import terminalbuffer.models.Color
import terminalbuffer.models.Style
import terminalbuffer.models.TextAttributes

class TerminalBufferEditingTest {

    @Nested
    inner class WriteText {

        @Test
        fun `should write short text and move cursor horizontally`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)

            // Act
            buffer.writeText("Hi")

            // Assert
            assertEquals(2, buffer.cursor.col)
            assertEquals(0, buffer.cursor.row)
            assertEquals('H', buffer.getCharAt(0, 0))
            assertEquals('i', buffer.getCharAt(1, 0))
            assertEquals(' ', buffer.getCharAt(2, 0))
        }

        @Test
        fun `should do nothing when writing an empty string`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(5, 2)

            // Act
            buffer.writeText("")

            // Assert
            assertEquals(5, buffer.cursor.col)
            assertEquals(2, buffer.cursor.row)
            assertEquals(' ', buffer.getCharAt(5, 2))
        }

        @Test
        fun `should write text exactly fitting the line and stop cursor at the edge`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)

            // Act
            buffer.writeText("0123456789")

            // Assert
            assertEquals('0', buffer.getCharAt(0, 0))
            assertEquals('9', buffer.getCharAt(9, 0))
            assertEquals(9, buffer.cursor.col)
        }

        @Test
        fun `should truncate text that exceeds the line width`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(8, 0)

            // Act
            buffer.writeText("ABCD")

            // Assert
            assertEquals('A', buffer.getCharAt(8, 0))
            assertEquals('B', buffer.getCharAt(9, 0))
            assertEquals(9, buffer.cursor.col)
            assertEquals(0, buffer.cursor.row)
        }

        @Test
        fun `should apply current attributes independently to each cell`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setAttributes(
                TextAttributes(
                    foreground = Color.RED, background = Color.BLACK, styles = mutableSetOf(Style.BOLD)
                )
            )

            // Act
            buffer.writeText("XY")

            // Assert
            val attrsX = buffer.getAttributesAt(0, 0)
            val attrsY = buffer.getAttributesAt(1, 0)

            assertEquals(Color.RED, attrsX.foreground)
            assert(attrsX.styles.contains(Style.BOLD))

            assertEquals(Color.RED, attrsY.foreground)
            attrsX.styles.add(Style.ITALIC)

            assert(!attrsY.styles.contains(Style.ITALIC)) { "Styles set should be deeply copied!" }
        }
    }

    @Nested
    inner class FillLine {
        @Test
        fun `should fill entire current line with specified character`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(5, 2)

            // Act
            buffer.fillLine('*')

            // Assert
            assertEquals('*', buffer.getCharAt(0, 2))
            assertEquals('*', buffer.getCharAt(5, 2))
            assertEquals('*', buffer.getCharAt(9, 2))
            assertEquals(' ', buffer.getCharAt(0, 1))
            assertEquals(' ', buffer.getCharAt(0, 3))
        }
    }

    @Nested
    inner class InsertText {
        @Test
        fun `should insert text and shift existing characters to the right`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("ABCF")
            buffer.setCursorPosition(3, 0)

            // Act
            buffer.insertText("DE")

            // Assert
            assertEquals('A', buffer.getCharAt(0, 0))
            assertEquals('B', buffer.getCharAt(1, 0))
            assertEquals('C', buffer.getCharAt(2, 0))
            assertEquals('D', buffer.getCharAt(3, 0))
            assertEquals('E', buffer.getCharAt(4, 0))
            assertEquals('F', buffer.getCharAt(5, 0))
        }

        @Test
        fun `should push characters off the right edge if line overflows`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 100)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("12345")
            buffer.setCursorPosition(0, 0)

            // Act
            buffer.insertText("A")

            // Assert
            assertEquals('A', buffer.getCharAt(0, 0))
            assertEquals('1', buffer.getCharAt(1, 0))
            assertEquals('4', buffer.getCharAt(4, 0))
        }

        @Test
        fun `should wrap to the next line when text exceeds line width`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 100)
            buffer.setCursorPosition(3, 0)

            // Act
            buffer.insertText("XYZ")

            // Assert
            assertEquals('X', buffer.getCharAt(3, 0))
            assertEquals('Y', buffer.getCharAt(4, 0))
            assertEquals('Z', buffer.getCharAt(0, 1))
            assertEquals(1, buffer.cursor.col)
            assertEquals(1, buffer.cursor.row)
        }
    }

    @Nested
    inner class InsertEmptyLineAtTheBottom {
        @Test
        fun `should shift screen up and clear bottom row`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Row 1")
            buffer.setCursorPosition(0, 1)
            buffer.writeText("Row 2")
            buffer.setCursorPosition(0, 2)
            buffer.writeText("Row 3")

            // Act
            buffer.insertEmptyLineAtTheBottom()

            // Assert
            assertEquals('R', buffer.getCharAt(0, 0))
            assertEquals('2', buffer.getCharAt(4, 0))
            assertEquals('R', buffer.getCharAt(0, 1))
            assertEquals('3', buffer.getCharAt(4, 1))
            assertEquals(' ', buffer.getCharAt(0, 2))
        }

        @Test
        fun `should preserve scrolled lines in scrollback history`() {
            // Arrange
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)
            buffer.setCursorPosition(0, 0)
            buffer.writeText("First")

            // Act
            buffer.insertEmptyLineAtTheBottom()

            // Assert
            assertEquals(1, buffer.currentScrollBackSize)
            assertEquals("First", buffer.getLineAsString(-1))
        }

        @Test
        fun `should respect max scrollback capacity and remove oldest lines`() {
            // Arrange
            val maxHistory = 2
            val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = maxHistory)
            buffer.insertEmptyLineAtTheBottom()
            buffer.insertEmptyLineAtTheBottom()
            buffer.setCursorPosition(0, 0)
            buffer.writeText("Stay")

            // Act
            buffer.insertEmptyLineAtTheBottom()

            // Assert
            assertEquals(2, buffer.currentScrollBackSize)
            assertEquals("Stay ", buffer.getLineAsString(-1))
            assertEquals("     ", buffer.getLineAsString(-2))
        }
    }

    @Nested
    inner class ClearOperations {

        @Test
        fun `clearScreen should replace all characters with spaces and reset cursor to origin`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setCursorPosition(2, 2)
            buffer.writeText("Hello")
            buffer.setCursorPosition(9, 4)

            // Act
            buffer.clearScreen()

            // Assert
            assertEquals(0, buffer.cursor.col)
            assertEquals(0, buffer.cursor.row)
            for (row in 0 until 5) {
                for (col in 0 until 10) {
                    assertEquals(
                        ' ', buffer.getCharAt(col, row), "Cell ($col, $row) should be cleared to space"
                    )
                }
            }
        }

        @Test
        fun `clearScreen should reset all attributes to default across the entire screen`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            val customAttrs = TextAttributes(
                foreground = Color.RED, background = Color.BLUE, styles = mutableSetOf(Style.BOLD)
            )
            buffer.setAttributes(customAttrs)
            buffer.setCursorPosition(0, 1)
            buffer.fillLine('X')

            // Act
            buffer.clearScreen()

            // Assert
            val attrs = buffer.getAttributesAt(5, 1)
            assertEquals(Color.DEFAULT_FG, attrs.foreground)
            assertEquals(Color.DEFAULT_BG, attrs.background)
            assertEquals(0, attrs.styles.size)
        }

        @Test
        fun `clearScreen should not affect the scrollback history`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.insertEmptyLineAtTheBottom()
            buffer.insertEmptyLineAtTheBottom()

            // Act
            buffer.clearScreen()

            // Assert
            assertEquals(2, buffer.currentScrollBackSize)
        }

        @Test
        fun `clearScreenAndScrollBack should completely reset screen, cursor, and empty scrollback`() {
            // Arrange
            val buffer = TerminalBuffer(width = 10, height = 5, maxScrollBack = 100)
            buffer.setAttributes(TextAttributes(foreground = Color.GREEN))
            buffer.writeText("Dirty Text")
            buffer.insertEmptyLineAtTheBottom()
            buffer.insertEmptyLineAtTheBottom()
            buffer.setCursorPosition(5, 3)

            // Act
            buffer.clearScreenAndScrollBack()

            // Assert
            assertEquals(0, buffer.cursor.col)
            assertEquals(0, buffer.cursor.row)
            assertEquals(' ', buffer.getCharAt(0, 0))
            assertEquals(Color.DEFAULT_FG, buffer.getAttributesAt(0, 0).foreground)
            assertEquals(0, buffer.currentScrollBackSize)
        }
    }

}