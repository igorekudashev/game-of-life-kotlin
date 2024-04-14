import base.AbstractGameKeyListener
import base.AbstractGameWindow
import base.GameSetting.Companion.borderSize
import base.GameSetting.Companion.cellSize
import java.awt.Color
import java.awt.Graphics
import javax.swing.JFrame

class GameOfLifeWindow : AbstractGameWindow<Cell>("Game Of Life", GameOfLifeWorld()) {

    // TODO: Оюновлять рисованием только те клетки которые изменили цвет
    // TODO: После старта выставлять фпс равный скорости обновления мира, а то получается если ниче не нажимать то макс фпс просто так

    override val keyListener: AbstractGameKeyListener = GameOfLifeKeyListener(this, world)
    private val cellRectSize = cellSize - borderSize

    override fun paintCell(graphics: Graphics, cell: Cell, x: Int, y: Int) {
        graphics.color = cell.getColor()
        graphics.fillRect(x, y, cellRectSize, cellRectSize)
    }
}