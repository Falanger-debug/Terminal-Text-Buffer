package terminalbuffer.models

data class TextAttributes(
    var foreground: Color = Color.DEFAULT_FG,
    var background: Color = Color.DEFAULT_BG,
    val styles: MutableSet<Style> = mutableSetOf()
)