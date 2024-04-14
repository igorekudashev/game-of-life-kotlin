package base

import base.GameSetting.Companion.worldHeight
import base.GameSetting.Companion.worldThreads
import base.GameSetting.Companion.worldWidth
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

const val DEFAULT_UPDATE_SPEED: Int = 1

abstract class AbstractWorld<CELL : AbstractCell> {

    var currentTickGrid = CellGrid<CELL?>(worldHeight, worldWidth); private set
    var nextTickGrid = CellGrid<CELL?>(worldHeight, worldWidth); private set
    var updatesPerSecond: Int = DEFAULT_UPDATE_SPEED; private set

    private val lock: PermissionLock = PermissionLock()
    private val repeater = Repeater(0) { update() }
    private val pool: ForkJoinPool = ForkJoinPool(worldThreads)
    private var count: AtomicInteger = AtomicInteger(0)

    init {
        randomize(50)

        repeater.setSpeed(updatesPerSecond)

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            println("Method was called $count times in the last second")
            count.set(0)
        }, 0, 1, TimeUnit.SECONDS)
    }

    fun changeUpdateSpeed(deltaUpdateSpeed: Int) : Int {
        updatesPerSecond = repeater.setSpeed(updatesPerSecond + deltaUpdateSpeed)
        return updatesPerSecond
    }

    abstract fun getRandomizedCell() : CELL

    fun randomize(aliveCellChance: Int) {
        nextRound {
            val normalizedChance = aliveCellChance.coerceIn(0, 100)
            for (x in 0 until worldWidth) {
                for (y in 0 until worldHeight) {
                    if (normalizedChance > Random.nextInt(100)) {
                        nextTickGrid[x, y] = getRandomizedCell()
                    }
                }
            }
        }
    }

    fun clear() {
        nextRound {
            nextTickGrid.clear()
        }
    }

    abstract fun getWorldUpdateTask() : ForkJoinTask<*>

    private fun update() {
        nextRound {
            val task = getWorldUpdateTask()
            pool.invoke(task)
            count.incrementAndGet()
        }
    }

    private fun nextRound(action: () -> Unit) {
        synchronized(this) {
            action.invoke()
            val temp = currentTickGrid
            currentTickGrid = nextTickGrid
            nextTickGrid = temp
            nextTickGrid.clear()
        }
    }
}