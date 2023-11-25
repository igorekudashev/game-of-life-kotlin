import GameSetting.Companion.neighbourCellsToBeBorn
import GameSetting.Companion.neighbourCellsToLive
import GameSetting.Companion.worldHeight
import GameSetting.Companion.worldThreads
import GameSetting.Companion.worldWidth
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors
import kotlin.random.Random

class World {

    var grid: AtomicReference<CellGrid> = AtomicReference(CellGrid())
    private var updatesPerSecond: Int = 1
    private val repeater: RunnableRepeater = RunnableRepeater(updatesPerSecond, this::update)
    private var count: AtomicInteger = AtomicInteger(0)
    private val pool: ForkJoinPool = ForkJoinPool(worldThreads)

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

    fun randomize(aliveCellChance: Int) {
        val randomizedGrid = CellGrid()
        val normalizedChance = aliveCellChance.coerceIn(0, 100)
        for (x in 0 until worldWidth) {
            for (y in 0 until worldHeight) {
                val location = Location(x, y)
                if (normalizedChance > Random.nextInt(100)) {
                    val cell = Cell(CellState.ALIVE)
                    randomizedGrid.addCell(location, cell)
                }
            }
        }
        grid.set(randomizedGrid)
    }

    fun clear() {
        grid.set(CellGrid())
    }

    private fun update() {
        val gridPointer = grid.get()
        val updatedGrid = CellGrid()
        val task = TestTask(gridPointer.getAliveCellLocations().toList()) { part -> processGridRegionUpdate(gridPointer, updatedGrid, part) }
        pool.invoke(task)
//            .filter { (_, aliveNeighbours) -> neighbourCellsToBeBorn.contains(aliveNeighbours) }
            .filter { (_, aliveNeighbours) -> neighbourCellsToBeBorn[aliveNeighbours] }
            .forEach { (location, _) -> updatedGrid.addCell(location, Cell(CellState.ALIVE)) }
        if (grid.get() === gridPointer) {
            grid.set(updatedGrid)
        }
        count.incrementAndGet()
    }

    private fun processGridRegionUpdate(oldGrid: CellGrid, updatedGrid: CellGrid, region: List<Location>) : Map<Location, Int> {
        val candidatesToReborn = HashMap<Location, Int>()
        region.forEach { location ->
            var aliveNeighbours = 0
            for (direction in Direction.values()) {
                val neighbourLocation = oldGrid.getNormalizedLocation(location.x + direction.deltaX, location.y + direction.deltaY)
                val neighbourCell = oldGrid.getCellAt(neighbourLocation)
                if (neighbourCell != null) {
                    aliveNeighbours++
                } else {
                    candidatesToReborn.merge(neighbourLocation, 1, Int::plus)
                }
            }
//            if (neighbourCellsToLive.contains(aliveNeighbours)) {
            if (neighbourCellsToLive[aliveNeighbours]) {
                updatedGrid.addCell(location, oldGrid.getCellAt(location)!!)
            }
        }
        return candidatesToReborn
    }
}