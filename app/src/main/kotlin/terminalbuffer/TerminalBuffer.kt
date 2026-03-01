package terminalbuffer

import terminalbuffer.exceptions.InvalidConfigurationException
import terminalbuffer.models.Cell
import terminalbuffer.models.Cursor
import terminalbuffer.models.TextAttributes

class TerminalBuffer (
    val width: Int,
    val height: Int,
    val maxScrollBack: Int
){
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
// TODO
    }

    fun insertText(text: String) {
// TODO
    }

    fun fillLine(char: Char = ' ') {
// TODO
    }

    fun insertEmptyLineAtTheBottom() {
// TODO
    }

    fun clearScreen() {
// TODO
    }

    fun clearScreenAndScrollBack() {
// TODO
    }

    fun getCharAt(col: Int, row: Int): Char {
        return screen[row][col].char // TODO
    }

    fun getAttributesAt(col: Int, row: Int): TextAttributes {
        return screen[row][col].attributes // TODO
    }

    fun getLineAsString(row: Int): String {
        return screen[row].joinToString("") { it.char.toString() } // TODO
    }

    fun getScreenAsString(): String {
        return screen.joinToString("\n") { row -> row.joinToString("") { it.char.toString() } } // TODO
    }

    fun getEntireContentAsString(): String {
        // TODO
        val scrollBackContent = scrollBack.joinToString("\n") { row -> row.joinToString("") { it.char.toString() } }
        val screenContent = getScreenAsString()
        return if (scrollBackContent.isEmpty()) screenContent else "$scrollBackContent\n$screenContent"
    }
}

fun main() {
    println(TerminalBuffer(80, 60, 1000))
}