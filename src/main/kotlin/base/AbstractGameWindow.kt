package base

import base.GameSetting.Companion.getScreenSize
import base.GameSetting.Companion.maxFps
import java.awt.Color
import java.awt.Graphics
import java.awt.image.VolatileImage
import javax.swing.JFrame

abstract class AbstractGameWindow<CELL>(title: String, world: AbstractWorld<CELL>) : JFrame(title) where CELL : AbstractCell {

    protected val world: AbstractWorld<CELL>
    private val repeater = Repeater(maxFps) { repaint() }
    private val bufferImage: VolatileImage

    init {
        this.world = world

        defaultCloseOperation = EXIT_ON_CLOSE
        background = Color.BLACK
        isResizable = false
        isVisible = true
        size = getScreenSize()
        addKeyListener(getGameKeyListener())
        bufferImage = createVolatileImage(width, height)
    }

    abstract fun paintCell(graphics: Graphics, cell: CELL, x: Int, y: Int)

    abstract fun getGameKeyListener() : AbstractGameKeyListener

    fun setFps(fps: Int) {
        repeater.setSpeed(fps.coerceIn(0, maxFps))
    }

    final override fun paint(graphics: Graphics) {
        val g2d = bufferImage.graphics

        g2d.fillRect(0, 0, width, height)
        val grid = world.currentTickGrid
        for (i in 0 until GameSetting.worldWidth) {
            for (j in 0 until GameSetting.worldHeight) {
                val cell = grid[Location(i, j)]
                val x = i * GameSetting.cellSize
                val y = j * GameSetting.cellSize
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