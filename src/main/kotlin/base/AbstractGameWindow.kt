package base

import base.GameSetting.Companion.getScreenSize
import base.GameSetting.Companion.maxFps
import java.awt.Color
import java.awt.Graphics
import java.awt.image.VolatileImage
import javax.swing.JFrame

abstract class AbstractGameWindow<CELL>(title: String, world: AbstractWorld<CELL>) : JFrame(title) where CELL : AbstractCell {

    protected abstract val keyListener: AbstractGameKeyListener

    protected val world: AbstractWorld<CELL>
    private val repeater = Repeater(0) { repaint() }
    private lateinit var bufferImage: VolatileImage

    var offsetX: Int = 0
    var offsetY: Int = 0

    init {
        this.world = world

        defaultCloseOperation = EXIT_ON_CLOSE
        background = Color.BLACK
        isResizable = false
        isVisible = true
        size = getScreenSize()
    }

    abstract fun paintCell(graphics: Graphics, cell: CELL, x: Int, y: Int)

    fun setFps(fps: Int) {
        repeater.setSpeed(fps.coerceIn(0, maxFps))
    }

    fun start() {
        addKeyListener(keyListener)
        bufferImage = createVolatileImage(width, height)
        repeater.setSpeed(DEFAULT_UPDATE_SPEED)
    }

    final override fun paint(graphics: Graphics) {
        val g2d = bufferImage.graphics

        g2d.fillRect(0, 0, width, height)
        val copy = world.currentTickGrid.copy()
        // TODO: copyOf?
        for (i in 0 until GameSetting.worldWidth) {
            for (j in 0 until GameSetting.worldHeight) {
                val cell = copy[i, j]
                val x = normalizeInt(i + offsetX * 2, GameSetting.worldWidth) * GameSetting.cellSize
                val y = normalizeInt(j + offsetY * 2, GameSetting.worldHeight) * GameSetting.cellSize
                if (cell != null) {
                    paintCell(g2d, cell, x, y)
                }
            }
        }

        graphics.drawImage(bufferImage, 0, 0, this)
        g2d.dispose()
//        println("Repaint..")
    }
}