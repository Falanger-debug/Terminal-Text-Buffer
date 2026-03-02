package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import terminalbuffer.exceptions.InvalidConfigurationException
import terminalbuffer.models.Color

class TerminalBufferInitTest {
    @Nested
    inner class InitialState {

        @Test
        fun `should initialize cursor at top-left origin`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            // Act & Assert
            assertEquals(0, buffer.cursor.col)
            assertEquals(0, buffer.cursor.row)
        }

        @Test
        fun `should initialize with zero current scrollback size`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            // Act & Assert
            assertEquals(0, buffer.currentScrollBackSize)
        }
    }

    @Nested
    inner class ExceptionMessages {

        @Test
        fun `should provide descriptive error message for invalid width`() {
            // Act
            val exception = assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 0, height = 24, maxScrollBack = 100)
            }

            // Assert
            assertTrue(exception.message!!.contains("Width must be a positive integer"))
        }

        @Test
        fun `should provide descriptive error message for invalid height`() {
            // Act
            val exception = assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = -5, maxScrollBack = 100)
            }

            // Assert
            assertTrue(exception.message!!.contains("Height must be a positive integer"))
        }

        @Test
        fun `should provide descriptive error message for invalid scrollback`() {
            // Act
            val exception = assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = 24, maxScrollBack = -1)
            }

            // Assert
            assertTrue(exception.message!!.contains("Max scroll back must be an integer"))
        }
    }

    @Nested
    inner class ValidConfigurations {

        @Test
        fun `should successfully create buffer with typical valid dimensions`() {
            // Arrange & Act
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            // Assert
            assertEquals(80, buffer.width)
            assertEquals(24, buffer.height)
            assertEquals(1000, buffer.maxScrollBack)
        }

        @Test
        fun `should successfully create buffer with minimum allowed dimensions`() {
            // Arrange & Act
            val buffer = TerminalBuffer(width = 1, height = 1, maxScrollBack = 0)

            // Assert
            assertEquals(1, buffer.width)
            assertEquals(1, buffer.height)
            assertEquals(0, buffer.maxScrollBack)
        }

        @Test
        fun `should successfully create buffer with maximum allowed dimensions`() {
            // Arrange & Act
            val buffer = TerminalBuffer(
                width = TerminalBuffer.MAX_WIDTH,
                height = TerminalBuffer.MAX_HEIGHT,
                maxScrollBack = TerminalBuffer.MAX_SCROLL_BACK
            )

            // Assert
            assertEquals(TerminalBuffer.MAX_WIDTH, buffer.width)
            assertEquals(TerminalBuffer.MAX_HEIGHT, buffer.height)
            assertEquals(TerminalBuffer.MAX_SCROLL_BACK, buffer.maxScrollBack)
        }

        @Test
        fun `should initialize screen with default empty cells`() {
            // Arrange & Act
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            // Assert
            assertEquals(" ", buffer.getCharAt(0, 0))
            assertEquals(" ", buffer.getCharAt(79, 23))
        }

        @Test
        fun `should initialize screen with default attributes`() {
            // Arrange
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            // Act
            val attrs = buffer.getAttributesAt(0, 0)

            // Assert
            assertEquals(Color.DEFAULT_FG, attrs.foreground)
            assertEquals(Color.DEFAULT_BG, attrs.background)
            assertEquals(0, attrs.styles.size)
        }
    }

    @Nested
    inner class InvalidLowerBoundaries {

        @Test
        fun `should throw InvalidConfigurationException for zero or negative width`() {
            // Act & Assert
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 0, height = 24, maxScrollBack = 100)
            }
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = -1, height = 24, maxScrollBack = 100)
            }
        }

        @Test
        fun `should throw InvalidConfigurationException for zero or negative height`() {
            // Act & Assert
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = 0, maxScrollBack = 100)
            }
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = -1, maxScrollBack = 100)
            }
        }

        @Test
        fun `should throw InvalidConfigurationException for negative scrollBack`() {
            // Act & Assert
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = 24, maxScrollBack = -1)
            }
        }
    }

    @Nested
    inner class InvalidUpperBoundaries {

        @Test
        fun `should throw InvalidConfigurationException when width exceeds maximum`() {
            // Act & Assert
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(
                    width = TerminalBuffer.MAX_WIDTH + 1, height = 24, maxScrollBack = 100
                )
            }
        }

        @Test
        fun `should throw InvalidConfigurationException when height exceeds maximum`() {
            // Act & Assert
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(
                    width = 80, height = TerminalBuffer.MAX_HEIGHT + 1, maxScrollBack = 100
                )
            }
        }

        @Test
        fun `should throw InvalidConfigurationException when scrollBack exceeds maximum`() {
            // Act & Assert
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(
                    width = 80, height = 24, maxScrollBack = TerminalBuffer.MAX_SCROLL_BACK + 1
                )
            }
        }
    }
}