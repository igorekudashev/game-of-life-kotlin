import base.AbstractCell
import java.awt.Color

class Cell : AbstractCell() {

    override fun getColor(): Color {
        return Color.CYAN
    }
}