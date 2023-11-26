package base

import java.awt.event.KeyEvent
import java.awt.event.KeyListener

abstract class AbstractGameKeyListener : KeyListener {

    abstract fun onGameKeyPressed(gameKey: GameKey)

    final override fun keyTyped(e: KeyEvent?) {
    }

    final override fun keyPressed(e: KeyEvent?) {
        val gameKey = getKey(e?.keyCode ?: -1)
        onGameKeyPressed(gameKey)
    }

    final override fun keyReleased(e: KeyEvent?) {
    }
}