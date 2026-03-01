package terminalbuffer.models

data class Cell(
    var char: Char = ' ',
    var attributes: TextAttributes = TextAttributes()
) {
    fun update(newChar: Char, newAttributes: TextAttributes) {
        this.char = newChar
        this.attributes = newAttributes.copy(
            styles = newAttributes.styles.toMutableSet()
        )
    }
}