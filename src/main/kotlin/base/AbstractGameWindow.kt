package base

import base.GameSetting.Companion.getScreenSize
import base.GameSetting.Companion.maxFps
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.VolatileImage
import javax.swing.JFrame

abstract class AbstractGameWindow<CELL>(title: String, world: AbstractWorld<CELL>) : JFrame(title) where CELL : AbstractCell {

    protected val world: AbstractWorld<CELL>
    private val repeater: Repeater
    private val image = BufferedImage(GameSetting.worldWidth * GameSetting.cellSize, GameSetting.worldHeight * GameSetting.cellSize, TYPE_INT_ARGB)

    init {
        this.world = world

        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        isVisible = true
        size = getScreenSize()
        addKeyListener(getGameKeyListener())
        repeater = Repeater(maxFps) { repaint() }
    }

    abstract fun paintCell(graphics: Graphics, cell: CELL, x: Int, y: Int)

    abstract fun getGameKeyListener() : AbstractGameKeyListener

    fun setFps(fps: Int) {
        repeater.setSpeed(fps.coerceIn(0, maxFps))
    }

    final override fun paint(graphics: Graphics) {
        val g2d = image.graphics
        g2d.color = Color.BLACK

        g2d.fillRect(0, 0, width, height)
        val grid = world.getCurrentTickGrid()
        for (i in 0 until GameSetting.worldWidth) {
            for (j in 0 until GameSetting.worldHeight) {
                val cell = grid[getIndexByLocation(i, j)]
                val x = i * GameSetting.cellSize
                val y = j * GameSetting.cellSize
                if (cell != null) {
                    paintCell(g2d, cell, x, y)
                }
            }
        }

//        graphics.drawImage(bufferImage, 0, 0, this)
        graphics.drawImage(image, 0, 0, this)
        g2d.dispose()
//        println("Repaint..")
    }
}