import base.*
import com.aparapi.Kernel
import com.aparapi.Range
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.stream.Collectors

private val executor = Executors.newFixedThreadPool(GameSetting.worldThreads)
private const val s = 2500

class NewTestTask {

    fun runGPU(world: GameOfLifeWorld, permission: UUID) {
        val candidates = ConcurrentHashMap<Location, Int>()
        val locations = world.currentTickGrid.keys.toTypedArray()
        val kernel = object : Kernel() {
            override fun run() {
                val i = globalId
                val location = locations[i]
                var aliveNeighbours = 0
                for (direction in Direction.values()) {
                    val neighbourLocation = location.getRelative(direction)
                    val neighbourCell = world.getCellAt(neighbourLocation)
                    if (neighbourCell != null) {
                        aliveNeighbours++
                    } else {
                        candidates.merge(neighbourLocation, 1, Int::plus)
                    }
                }
                if (aliveNeighbours in GameSetting.neighbourCellsToLive) {
                    world.poprikol()[location] = world.getCellAt(location)!!
                }
            }
        }
        val range = Range.create(locations.size)
        kernel.execute(range)

        val callables2 = mutableListOf<Callable<Unit>>()
        val locations2 = candidates.keys.toTypedArray()
        var i2 = 0
        while (i2 < locations2.size) {
            callables2.add(getReb(world, locations2, i2, candidates, permission))
            i2 += s
        }
        executor.invokeAll(callables2).map { it.get() }
    }

    fun run(world: GameOfLifeWorld, permission: UUID) {
        val callables = mutableListOf<Callable<Map<Location, Int>>>()
        val locations = world.currentTickGrid.keys.toTypedArray()
        var i = 0
        while (i < locations.size) {
            callables.add(getReg(world, locations, i, permission))
            i += s
        }
        val candidates = executor.invokeAll(callables).map { it.get() }.stream().flatMap {
            it.entries.stream()
        }.collect(Collectors.toMap({ it.key }, { it.value }, Int::plus))
        val callables2 = mutableListOf<Callable<Unit>>()
        val locations2 = candidates.keys.toTypedArray()
        var i2 = 0
        while (i2 < locations2.size) {
            callables2.add(getReb(world, locations2, i2, candidates, permission))
            i2 += s
        }
        executor.invokeAll(callables2).map { it.get() }
    }

    private fun getReg(world: GameOfLifeWorld, locations: Array<Location>, start: Int, permission: UUID) : Callable<Map<Location, Int>> {
        return Callable {
            setThreadPermission(permission)
            val candidatesToReborn = HashMap<Location, Int>()
            for (i in start until start + s) {
                if (i >= locations.size) break
                val location = locations[i]
                var aliveNeighbours = 0
                for (direction in Direction.values()) {
                    val neighbourLocation = location.getRelative(direction)
                    val neighbourCell = world.getCellAt(neighbourLocation)
                    if (neighbourCell != null) {
                        aliveNeighbours++
                    } else {
                        candidatesToReborn.merge(neighbourLocation, 1, Int::plus)
                    }
                }
                if (aliveNeighbours in GameSetting.neighbourCellsToLive) {
                    world.addCellNoNextTick(location, world.getCellAt(location)!!)
                }
            }
            setThreadPermission()
            return@Callable candidatesToReborn
        }
    }

    private fun getReb(world: GameOfLifeWorld, locations: Array<Location>, start: Int, candidates: Map<Location, Int>, permission: UUID) : Callable<Unit> {
        return Callable {
            setThreadPermission(permission)
            for (i in start until start + s) {
                if (i >= locations.size) break
                val location = locations[i]
                if (candidates[location] in GameSetting.neighbourCellsToBeBorn) {
                    world.addCellNoNextTick(location, Cell())
                }
            }
            setThreadPermission()
        }
    }
}