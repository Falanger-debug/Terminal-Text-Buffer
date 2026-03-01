package terminalbuffer.models

data class Cell(
    var char: Char = ' ',
    var attributes: TextAttributes = TextAttributes()
)