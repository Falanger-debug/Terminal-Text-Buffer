package terminalbuffer.models

class Cursor (
    private val maxCol: Int,
    private val maxRow: Int
){
    var col: Int = 0
        private set
    var row: Int = 0
        private set

    fun setPosition(col: Int, row: Int) {
        this.col = col.coerceIn(0, maxCol - 1)
        this.row = row.coerceIn(0, maxRow - 1)
    }

    fun move(up: Int = 0, down: Int = 0, left: Int = 0, right: Int = 0) {
        val newCol = col - left + right
        val newRow = row - up + down
        setPosition(newCol, newRow)
    }
}