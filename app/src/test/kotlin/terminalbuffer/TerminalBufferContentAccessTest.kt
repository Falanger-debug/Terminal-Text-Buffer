package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
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
            assertEquals('X', buffer.getCharAt(3, 2))
            assertEquals(Color.CYAN, retrievedAttrs.foreground)
            assert(retrievedAttrs.styles.contains(Style.ITALIC))
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
            assertEquals('Z', buffer.getCharAt(5, -1))
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

            // Assert
            assertThrows<IndexOutOfBoundsException> { buffer.getCharAt(0, 5) }
            assertThrows<IndexOutOfBoundsException> { buffer.getAttributesAt(0, -2) }
        }
    }
}