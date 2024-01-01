package base

import GameOfLifeWorld
import NewTestTask
import base.GameSetting.Companion.worldHeight
import base.GameSetting.Companion.worldThreads
import base.GameSetting.Companion.worldWidth
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private const val DEFAULT_UPDATE_SPEED: Int = 1

fun getIndexByLocation(x: Int, y: Int) : Int {
    return normalizeY(y) * worldWidth + normalizeX(x)
}

fun getXByIndex(index: Int) : Int {
    val y = getYByIndex(index)
    return index - y * worldWidth
}

fun getYByIndex(index: Int) : Int {
    return index / worldWidth
}

private fun normalizeX(x: Int) : Int {
    return normalizeInt(x, worldWidth)
}

private fun normalizeY(y: Int) : Int {
    return normalizeInt(y, worldHeight)
}

abstract class AbstractWorld<CELL> where CELL : AbstractCell {

    private var currentTickGrid: Array<CELL?>
    private var nextTickGrid: Array<CELL?>

    private val dummy: Any = Any()
    private val lock: PermissionLock = PermissionLock()
    private val repeater: Repeater
    private val pool: ForkJoinPool = ForkJoinPool(worldThreads)
    private var updatesPerSecond: Int = DEFAULT_UPDATE_SPEED
    private var count: AtomicInteger = AtomicInteger(0)
    private val uuu: NewTestTask = NewTestTask()
    var currentAliveCellIndices = ConcurrentLinkedQueue<Int>()
    var nextAliveCellIndices = ConcurrentLinkedQueue<Int>()

    init {
        currentTickGrid = getArray()
        nextTickGrid = getArray()

        randomize(50)
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            println("Method was called $count times in the last second")
            count.set(0)
        }, 0, 1, TimeUnit.SECONDS)
        repeater = Repeater(DEFAULT_UPDATE_SPEED) { update() }
    }

    fun changeUpdateSpeed(deltaUpdateSpeed: Int) : Int {
        updatesPerSecond = repeater.setSpeed(updatesPerSecond + deltaUpdateSpeed)
        return updatesPerSecond
    }

    abstract fun getRandomizedCell() : CELL

    abstract fun getArray() : Array<CELL?>

    fun randomize(aliveCellChance: Int) {
        val normalizedChance = aliveCellChance.coerceIn(0, 100)
        for (x in 0 until worldWidth) {
            for (y in 0 until worldHeight) {
                if (normalizedChance > Random.nextInt(100)) {
                    createRandomCellAt(getIndexByLocation(x, y))
                }
            }
        }
//        prepare { next, permission ->
//            for (x in 0 until worldWidth) {
//                for (y in 0 until worldHeight) {
//                    if (normalizedChance > Random.nextInt(100)) {
//                        println("try")
//                        next[getIndexByLocation(x, y)] = getRandomizedCell()
//                    }
//                }
//            }
//        }
    }

    fun createRandomCellAt(index: Int) {
        addCellNoNextTick(index, getRandomizedCell())
    }

    fun addCellNoNextTick(x: Int, y: Int, cell: CELL) {
        addCellNoNextTick(getIndexByLocation(x, y), cell)
    }

    fun addCellNoNextTick(index: Int, cell: CELL) {
        nextTickGrid[index] = cell
        nextAliveCellIndices.add(index)
//        lock.run {
//            nextTickGrid[index] = cell
////            if (nextTickGrid.containsKey(location)) {
////                collisions[location] = dummy // TODO: выяснить почему если ставишь нулл то не работает
////            } else {
////                (nextTickGrid as MutableMap)[location] = cell
////            }
//        }
    }

    fun getCellAt(index: Int) : CELL? {
        return currentTickGrid[index]
    }

    fun getCurrentTickGrid() : Array<CELL?> {
        return currentTickGrid
    }

    fun clear() {
        nextTickGrid.fill(null)
//        prepare { next, permission ->
//            next.fill(null)
//        }
    }

    abstract fun getWorldUpdateTask(permission: UUID) : ChainedWorldUpdateAction<out AbstractWorld<out CELL>>

    private fun update() {
        uuu.runWithNoPermission(this)
        currentTickGrid = nextTickGrid
        nextTickGrid = getArray()
        currentAliveCellIndices = nextAliveCellIndices
        nextAliveCellIndices = ConcurrentLinkedQueue<Int>()
        count.incrementAndGet()
//        prepare { next, permission ->
////            val task = getWorldUpdateTask(permission)
//            // .addTask(CollisionResolverTask().withInput(collisions.keys))
////            pool.invoke(task)
////            task.update()
//            uuu.run(this as GameOfLifeWorld, permission)
////            uuu.runGPU(this as GameOfLifeWorld, permission)
//            count.incrementAndGet()
//        }
    }

//    private fun prepare(action: (Array<CELL?>, UUID) -> Unit) {
//        lock.prepareLock { permission ->
//            action.invoke(nextTickGrid, permission)
//            currentTickGrid = nextTickGrid
//            nextTickGrid.fill(null)
//        }
//    }
}