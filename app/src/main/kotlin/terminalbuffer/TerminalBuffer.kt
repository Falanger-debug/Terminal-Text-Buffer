package terminalbuffer

import terminalbuffer.exceptions.InvalidConfigurationException
import terminalbuffer.models.Cell
import terminalbuffer.models.Cursor
import terminalbuffer.models.TextAttributes

class TerminalBuffer(
    val width: Int, val height: Int, val maxScrollBack: Int
) {
    companion object {
        const val MAX_WIDTH = 2000
        const val MAX_HEIGHT = 2000
        const val MAX_SCROLL_BACK = 100_000
    }

    init {
        if (width !in 1..MAX_WIDTH) {
            throw InvalidConfigurationException("Width must be a positive integer between 1 and $MAX_WIDTH.")
        }
        if (height !in 1..MAX_HEIGHT) {
            throw InvalidConfigurationException("Height must be a positive integer between 1 and $MAX_HEIGHT.")
        }
        if (maxScrollBack !in 0..MAX_SCROLL_BACK) {
            throw InvalidConfigurationException("Max scroll back must be an integer between 0 and $MAX_SCROLL_BACK.")
        }
    }

    private val screen: Array<Array<Cell>> = Array(height) { Array(width) { Cell() } }
    private val scrollBack: ArrayDeque<Array<Cell>> = ArrayDeque(maxScrollBack)
    val cursor = Cursor(width, height)
    private var currentAttributes: TextAttributes = TextAttributes()
    val currentScrollBackSize: Int
        get() = scrollBack.size

    fun setAttributes(attributes: TextAttributes) {
        currentAttributes = attributes
    }

    fun setCursorPosition(col: Int, row: Int) {
        cursor.setPosition(col, row)
    }

    fun moveCursor(up: Int = 0, down: Int = 0, left: Int = 0, right: Int = 0) {
        cursor.move(up, down, left, right)
    }


    fun writeText(text: String) {
        for (char in text) {
            val currentRow = cursor.row
            val currentCol = cursor.col
            val cell = screen[currentRow][currentCol]

            cell.update(char, currentAttributes)

            if (currentCol == width - 1) break

            cursor.move(right = 1)
        }
    }

    fun insertText(text: String) {
        for (char in text) {
            val currentRow = cursor.row
            val currentCol = cursor.col

            for (col in width - 1 downTo currentCol + 1) {
                val targetCell = screen[currentRow][col]
                val sourceCell = screen[currentRow][col - 1]

                targetCell.update(sourceCell.char, sourceCell.attributes)
            }

            screen[currentRow][currentCol].update(char, currentAttributes)

            if (currentCol == width - 1) {
                if (currentRow == height - 1) {
                    insertEmptyLineAtTheBottom()
                    cursor.setPosition(0, height - 1)
                } else {
                    cursor.setPosition(0, currentRow + 1)
                }
            } else {
                cursor.move(right = 1)
            }
        }
    }

    fun fillLine(char: Char = ' ') {
        val currentRow = cursor.row

        for (col in 0 until width) {
            screen[currentRow][col].update(char, currentAttributes)
        }
    }

    fun insertEmptyLineAtTheBottom() {
        val topRow = screen[0]

        if (maxScrollBack > 0) {
            if (scrollBack.size == maxScrollBack) {
                scrollBack.removeFirst()
            }
            scrollBack.addLast(topRow)
        }

        for (row in 0 until height - 1) {
            screen[row] = screen[row + 1]
        }

        screen[height - 1] = Array(width) { Cell() }
    }

    fun clearScreen() {
        val defaultAttributes = TextAttributes()

        for (row in 0 until height) {
            for (col in 0 until width) {
                screen[row][col].update(' ', defaultAttributes)
            }
        }

        cursor.setPosition(0, 0)
    }

    fun clearScreenAndScrollBack() {
        clearScreen()
        scrollBack.clear()
    }

    fun getCharAt(col: Int, row: Int): Char {
        return getCellAt(col, row).char
    }

    fun getAttributesAt(col: Int, row: Int): TextAttributes {
        return getCellAt(col, row).attributes
    }

    fun getLineAsString(row: Int): String {
        return if (row in 0..<height) {
            screen[row].joinToString("") { it.char.toString() }
        } else if (row < 0 && row >= -scrollBack.size) {
            val indexInDeque = scrollBack.size + row
            scrollBack[indexInDeque].joinToString("") { it.char.toString() }
        } else {
            throw IndexOutOfBoundsException("Row index $row is out of bounds. Valid range is 0 to ${height - 1} for screen and -${scrollBack.size} to -1 for scroll back.")
        }
    }

    fun getScreenAsString(): String {
        return (0 until height).joinToString("\n") { row ->
            getLineAsString(row)
        }
    }

    fun getEntireContentAsString(): String {
        return (-scrollBack.size until height).joinToString("\n") { row ->
            getLineAsString(row)
        }
    }


    private fun getCellAt(col: Int, row: Int): Cell {
        if (col !in 0 until width) {
            throw IndexOutOfBoundsException("Column index $col is out of bounds. Valid range is 0 to ${width - 1}.")
        }

        return if (row in 0 until height) {
            screen[row][col]
        } else if (row < 0 && row >= -scrollBack.size) {
            val indexInDeque = scrollBack.size + row
            scrollBack[indexInDeque][col]
        } else {
            val scrollBackMsg = if (scrollBack.isNotEmpty()) {
                " and from -${scrollBack.size} to -1 for scroll back"
            } else {
                " (scroll back is currently empty)"
            }
            throw IndexOutOfBoundsException("Row index $row is out of bounds. Valid range is 0 to ${height - 1} for screen$scrollBackMsg.")
        }
    }
}
