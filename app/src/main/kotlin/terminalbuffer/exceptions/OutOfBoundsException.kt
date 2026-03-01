package terminalbuffer.exceptions

class OutOfBoundsException(col: Int, row: Int, width: Int, height: Int) : TerminalBufferException(
    "Position ($col, $row) is out of bounds for terminal buffer of size ($width, $height)"
)