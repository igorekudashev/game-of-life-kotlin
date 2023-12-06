import base.AbstractGameKeyListener
import base.AbstractGameWindow
import base.AbstractWorld
import base.GameKey

class GameOfLifeKeyListener(
    private val gameWindow: AbstractGameWindow<*>,
    private val world: AbstractWorld<*>
) : AbstractGameKeyListener() {

    override fun onGameKeyPressed(gameKey: GameKey) {
        when (gameKey) {
            GameKey.KEY_R -> {
                world.randomize(50)
                gameWindow.repaint()
            }
            GameKey.KEY_PLUS -> {
                val worldUpdatesPerSecond = world.changeUpdateSpeed(10)
                gameWindow.setFps(worldUpdatesPerSecond)
            }
            GameKey.KEY_MINUS -> {
                val worldUpdatesPerSecond = world.changeUpdateSpeed(-10)
                gameWindow.setFps(worldUpdatesPerSecond)
            }
            GameKey.KEY_C -> {
                world.clear()
            }
            else -> {}
        }
    }
}