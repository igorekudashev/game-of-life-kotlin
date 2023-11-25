package listener

import GameWindow
import World
import getKey
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class GameKeyListener(
    private val gameWindow: GameWindow,
    private val world: World
) : KeyListener {

    override fun keyTyped(e: KeyEvent?) {
    }

    override fun keyPressed(e: KeyEvent?) {
        when (getKey(e?.keyCode ?: -1)) {
            GameKey.RANDOMIZE -> {
                world.randomize(50)
                gameWindow.repaint()
            }
            GameKey.SPEED_UP -> {
                val worldUpdatesPerSecond = world.changeUpdateSpeed(50)
                gameWindow.setFps(worldUpdatesPerSecond)
            }
            GameKey.SPEED_DOWN -> {
                val worldUpdatesPerSecond = world.changeUpdateSpeed(-50)
                gameWindow.setFps(worldUpdatesPerSecond)
            }
            GameKey.CLEAR -> {
                world.clear()
            }
            else -> {}
        }
    }

    override fun keyReleased(e: KeyEvent?) {
    }
}