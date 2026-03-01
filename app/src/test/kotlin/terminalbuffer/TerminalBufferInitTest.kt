package terminalbuffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import terminalbuffer.exceptions.InvalidConfigurationException
import terminalbuffer.models.Color

class TerminalBufferInitTest {
    @Nested
    inner class ValidConfigurations {

        @Test
        fun `should successfully create buffer with typical valid dimensions`() {
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            assertEquals(80, buffer.width)
            assertEquals(24, buffer.height)
            assertEquals(1000, buffer.maxScrollBack)
        }

        @Test
        fun `should successfully create buffer with minimum allowed dimensions`() {
            val buffer = TerminalBuffer(width = 1, height = 1, maxScrollBack = 0)

            assertEquals(1, buffer.width)
            assertEquals(1, buffer.height)
            assertEquals(0, buffer.maxScrollBack)
        }

        @Test
        fun `should successfully create buffer with maximum allowed dimensions`() {
            val buffer = TerminalBuffer(
                width = TerminalBuffer.MAX_WIDTH,
                height = TerminalBuffer.MAX_HEIGHT,
                maxScrollBack = TerminalBuffer.MAX_SCROLL_BACK
            )

            assertEquals(TerminalBuffer.MAX_WIDTH, buffer.width)
            assertEquals(TerminalBuffer.MAX_HEIGHT, buffer.height)
            assertEquals(TerminalBuffer.MAX_SCROLL_BACK, buffer.maxScrollBack)
        }

        @Test
        fun `should initialize screen with default empty cells`() {
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            assertEquals(' ', buffer.getCharAt(0, 0))
            assertEquals(' ', buffer.getCharAt(79, 23))
        }

        @Test
        fun `should initialize screen with default attributes`() {
            val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 1000)

            val attrs = buffer.getAttributesAt(0, 0)

            assertEquals(Color.DEFAULT_FG, attrs.foreground)
            assertEquals(Color.DEFAULT_BG, attrs.background)
            assertEquals(0, attrs.styles.size)
        }
    }

    @Nested
    inner class InvalidLowerBoundaries {

        @Test
        fun `should throw InvalidConfigurationException for zero or negative width`() {
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 0, height = 24, maxScrollBack = 100)
            }
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = -1, height = 24, maxScrollBack = 100)
            }
        }

        @Test
        fun `should throw InvalidConfigurationException for zero or negative height`() {
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = 0, maxScrollBack = 100)
            }
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = -1, maxScrollBack = 100)
            }
        }

        @Test
        fun `should throw InvalidConfigurationException for negative scrollBack`() {
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(width = 80, height = 24, maxScrollBack = -1)
            }
        }
    }

    @Nested
    inner class InvalidUpperBoundaries {

        @Test
        fun `should throw InvalidConfigurationException when width exceeds maximum`() {
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(
                    width = TerminalBuffer.MAX_WIDTH + 1,
                    height = 24,
                    maxScrollBack = 100
                )
            }
        }

        @Test
        fun `should throw InvalidConfigurationException when height exceeds maximum`() {
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(
                    width = 80,
                    height = TerminalBuffer.MAX_HEIGHT + 1,
                    maxScrollBack = 100
                )
            }
        }

        @Test
        fun `should throw InvalidConfigurationException when scrollBack exceeds maximum`() {
            assertThrows<InvalidConfigurationException> {
                TerminalBuffer(
                    width = 80,
                    height = 24,
                    maxScrollBack = TerminalBuffer.MAX_SCROLL_BACK + 1
                )
            }
        }
    }
}