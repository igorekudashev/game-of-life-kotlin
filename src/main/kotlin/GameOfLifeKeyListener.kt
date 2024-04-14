import base.AbstractGameKeyListener
import base.AbstractGameWindow
import base.AbstractWorld
import base.GameKey

class GameOfLifeKeyListener(
    private val gameWindow: AbstractGameWindow<*>,
    private val world: AbstractWorld<*>
) : AbstractGameKeyListener() {

    private var isPaused: Boolean = false
    private var worldUpdateSpeed: Int = 0

    override fun onGameKeyPressed(gameKey: GameKey) {
        println("in $gameKey")
        when (gameKey) {
            GameKey.R -> {
                world.randomize(50)
//                gameWindow.repaint()
            }
            GameKey.PLUS -> {
                if (isPaused) return
                val worldUpdatesPerSecond = world.changeUpdateSpeed(50)
                gameWindow.setFps(worldUpdatesPerSecond)
            }
            GameKey.MINUS -> {
                if (isPaused) return
                val worldUpdatesPerSecond = world.changeUpdateSpeed(-50)
                gameWindow.setFps(worldUpdatesPerSecond)
            }
            GameKey.C -> {
                world.clear()
            }
            GameKey.ARROW_LEFT -> {
                gameWindow.offsetX++
            }
            GameKey.ARROW_UP -> {
                gameWindow.offsetY++
            }
            GameKey.ARROW_RIGHT -> {
                gameWindow.offsetX--
            }
            GameKey.ARROW_DOWN -> {
                gameWindow.offsetY--
            }
            GameKey.P -> {
                if (isPaused) {
                    world.changeUpdateSpeed(worldUpdateSpeed)
                } else {
                    worldUpdateSpeed = world.updatesPerSecond
                    println(worldUpdateSpeed)
                    println(world.changeUpdateSpeed(-worldUpdateSpeed))
                }
                isPaused = !isPaused
            }
            else -> {}
        }
    }
}