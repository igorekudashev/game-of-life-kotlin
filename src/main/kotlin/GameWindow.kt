import GameSetting.Companion.borderSize
import GameSetting.Companion.cellSize
import GameSetting.Companion.getScreenSize
import GameSetting.Companion.maxFps
import GameSetting.Companion.worldHeight
import GameSetting.Companion.worldWidth
import listener.GameKeyListener
import java.awt.Color
import java.awt.Graphics
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JFrame

class GameWindow(
    private val world: World
) : JFrame() {

    // TODO: Оюновлять рисованием только те клетки которые изменили цвет
    // TODO: Обработка клеток в многопотоке
    // TODO: После старта выставлять фпс равный скорости обновления мира, а то получается если ниче не нажимать то макс фпс просто так

    private val timer: RunnableRepeater = RunnableRepeater(maxFps, this::repaint)
    private val cellRectSize = cellSize - borderSize

    init {
        title = "Game Of Life"
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        isVisible = true
        size = getScreenSize()

        addKeyListener(GameKeyListener(this, world))
    }

    fun setFps(fps: Int) {
        timer.setSpeed(fps.coerceIn(0, maxFps))
    }

    override fun paint(graphics: Graphics) {
        val grid = world.grid.get()
        for (i in 0 until  worldWidth) {
            for (j in 0 until worldHeight) {
                val cell = grid.getCellAt(i, j)
                val x = i * cellSize
                val y = j * cellSize
                graphics.color = if (cell == null) {
                    Color.BLACK
                } else {
                    Color.CYAN
                }
                graphics.fillRect(x, y, cellRectSize, cellRectSize)
            }
        }
//        println("Repaint..")
    }
}