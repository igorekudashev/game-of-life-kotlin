package base

import GameOfLifeWorld
import NewTestTask
import base.GameSetting.Companion.worldHeight
import base.GameSetting.Companion.worldThreads
import base.GameSetting.Companion.worldWidth
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.HashMap
import kotlin.random.Random

private const val DEFAULT_UPDATE_SPEED: Int = 1

abstract class AbstractWorld<CELL> where CELL : AbstractCell {

    var currentTickGrid: Map<Location, CELL> = HashMap(); private set

    private val dummy: Any = Any()
    private val collisions: MutableMap<Location, Any?> = ConcurrentHashMap()
    private val lock: PermissionLock = PermissionLock()
    private val repeater = Repeater(DEFAULT_UPDATE_SPEED) { update() }
    private val pool: ForkJoinPool = ForkJoinPool(worldThreads)
    private var nextTickGrid: Map<Location, CELL> = getNewNextTickGrid()
    private var updatesPerSecond: Int = DEFAULT_UPDATE_SPEED
    private var count: AtomicInteger = AtomicInteger(0)
    private val uuu: NewTestTask = NewTestTask()

    init {
        randomize(50)

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
        val normalizedChance = aliveCellChance.coerceIn(0, 100)
        prepare { next, permission ->
            for (x in 0 until worldWidth) {
                for (y in 0 until worldHeight) {
                    val location = Location(x, y)
                    if (normalizedChance > Random.nextInt(100)) {
                        next[location] = getRandomizedCell()
                    }
                }
            }
        }
    }

    fun poprikol() : MutableMap<Location, CELL> {
        return nextTickGrid as MutableMap<Location, CELL>
    }

    fun removeCellNoNextTick(location: Location) {
        lock.run {
            (nextTickGrid as MutableMap).remove(location)
        }
    }

    fun addCell(location: Location, cell: CELL) { // TODO: Добавить коллизии
        prepare { next, permission ->
            next.putAll(currentTickGrid)
            next[location] = cell
        }
    }

    fun addCellNoNextTick(location: Location, cell: CELL) {
        lock.run {
            (nextTickGrid as MutableMap)[location] = cell
//            if (nextTickGrid.containsKey(location)) {
//                collisions[location] = dummy // TODO: выяснить почему если ставишь нулл то не работает
//            } else {
//                (nextTickGrid as MutableMap)[location] = cell
//            }
        }
    }

    fun getCellAt(location: Location) : CELL? {
        return currentTickGrid[location]
    }

    fun clear() {
        prepare { next, permission ->
            next.clear()
        }
    }

    abstract fun getWorldUpdateTask(permission: UUID) : ChainedWorldUpdateAction<out AbstractWorld<out CELL>>

    private fun update() {
        prepare { next, permission ->
//            val task = getWorldUpdateTask(permission)
            // .addTask(CollisionResolverTask().withInput(collisions.keys))
//            pool.invoke(task)
//            task.update()
            uuu.run(this as GameOfLifeWorld, permission)
//            uuu.runGPU(this as GameOfLifeWorld, permission)
            count.incrementAndGet()
        }
    }

    private fun prepare(action: (MutableMap<Location, CELL>, UUID) -> Unit) {
        lock.prepareLock { permission ->
            action.invoke(nextTickGrid as MutableMap, permission)
            currentTickGrid = nextTickGrid
            nextTickGrid = getNewNextTickGrid()
            collisions.clear()
        }
    }

    private fun getNewNextTickGrid(content: Map<Location, CELL>? = null) : MutableMap<Location, CELL> {
        return if (content == null) {
            ConcurrentHashMap()
        } else {
            ConcurrentHashMap(content)
        }
    }
}