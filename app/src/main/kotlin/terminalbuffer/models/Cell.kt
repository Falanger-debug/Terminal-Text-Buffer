package terminalbuffer.models

data class Cell(
    var codePoint: Int = ' '.code, var attributes: TextAttributes = TextAttributes()
) {
    fun update(newCodePoint: Int, newAttributes: TextAttributes) {
        this.codePoint = newCodePoint
        this.attributes = newAttributes.copy(
            styles = newAttributes.styles.toMutableSet()
        )
    }
}