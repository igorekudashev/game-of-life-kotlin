import base.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.stream.Collectors

private val executor = Executors.newFixedThreadPool(GameSetting.worldThreads)

class NewTestTask {

//    fun runGPU(world: GameOfLifeWorld, permission: UUID) {
//        val candidates = ConcurrentHashMap<Location, Int>()
//        val locations = world.currentTickGrid.keys.toTypedArray()
//        val kernel = object : Kernel() {
//            override fun run() {
//                val i = globalId
//                val location = locations[i]
//                var aliveNeighbours = 0
//                for (direction in Direction.values()) {
//                    val neighbourLocation = location.getRelative(direction)
//                    val neighbourCell = world.getCellAt(neighbourLocation)
//                    if (neighbourCell != null) {
//                        aliveNeighbours++
//                    } else {
//                        candidates.merge(neighbourLocation, 1, Int::plus)
//                    }
//                }
//                if (aliveNeighbours in GameSetting.neighbourCellsToLive) {
//                    world.nextTickGrid[getIndexByLocation(location)] = world.getCellAt(location)!!
//                }
//            }
//        }
//        val range = Range.create(locations.size)
//        kernel.execute(range)
//
//        val callables2 = mutableListOf<Callable<Unit>>()
//        val locations2 = candidates.keys.toTypedArray()
//        var i2 = 0
//        while (i2 < locations2.size) {
//            callables2.add(getReb(world, locations2, i2, candidates, permission))
//            i2 += s
//        }
//        executor.invokeAll(callables2).map { it.get() }
//    }

//    fun run(world: GameOfLifeWorld, permission: UUID) {
//        val callables = mutableListOf<Callable<Map<Int, Int>>>()
//        var i = 0
//        while (i < world.currentTickGrid.size) {
//            callables.add(getCal1(world, i, permission))
//            i += s
//        }
//        val candidates = executor.invokeAll(callables).map { it.get() }.stream().flatMap {
//            it.entries.stream()
//        }.collect(Collectors.toMap({ it.key }, { it.value }, Int::plus))
//        val callables2 = mutableListOf<Callable<Unit>>()
//        val locations2 = candidates.keys.toTypedArray()
//        var i2 = 0
//        while (i2 < locations2.size) {
//            callables2.add(getCal2(world, locations2, i2, candidates, permission))
//            i2 += s
//        }
//        executor.invokeAll(callables2).map { it.get() }
//    }
//
//    private fun getCal1(world: GameOfLifeWorld, start: Int, permission: UUID) : Callable<Map<Int, Int>> {
//        return Callable {
//            setThreadPermission(permission)
//            val candidatesToReborn = HashMap<Int, Int>()
//            for (i in start until start + s) {
//                if (i >= world.currentTickGrid.size) break
//                var aliveNeighbours = 0
//                for (direction in Direction.values()) {
//                    val neighbourIndex = i + worldWidth * direction.deltaY + direction.deltaX
//                    val neighbourCell = world.currentTickGrid[neighbourIndex]
//                    if (neighbourCell != null) {
//                        aliveNeighbours++
//                    } else {
//                        candidatesToReborn.merge(neighbourIndex, 1, Int::plus)
//                    }
//                }
//                if (aliveNeighbours in GameSetting.neighbourCellsToLive) {
//                    world.addCellNoNextTick(i, Cell())
//                }
//            }
//            setThreadPermission()
//            return@Callable candidatesToReborn
//        }
//    }
//
//    private fun getCal2(world: GameOfLifeWorld, locations: Array<Int>, start: Int, candidates: Map<Int, Int>, permission: UUID) : Callable<Unit> {
//        return Callable {
//            setThreadPermission(permission)
//            for (i in start until start + s) {
//                if (i >= locations.size) break
//                val location = locations[i]
//                if (candidates[location] in GameSetting.neighbourCellsToBeBorn) {
//                    world.addCellNoNextTick(location, Cell())
//                }
//            }
//            setThreadPermission()
//        }
//    }

    fun <CELL> runWithNoPermission(world: AbstractWorld<CELL>) where CELL : AbstractCell {
        val callables1 = mutableListOf<Callable<Map<Int, Int>>>()
        world.currentAliveCellIndices.chunked(1000).forEach {
            callables1.add(getCal1WithNoPermission(world, it))
        }

        val candidates = executor.invokeAll(callables1).map { it.get() }.stream().flatMap {
            it.entries.stream()
        }.collect(Collectors.toMap({ it.key }, { it.value }, Int::plus))

        val callables2 = mutableListOf<Callable<Unit>>()
        candidates.keys.chunked(500).forEach {
            callables2.add(getCal2WithNoPermission(world, it, candidates))
        }
        executor.invokeAll(callables2).map { it.get() }
    }

    private fun <CELL> getCal1WithNoPermission(world: AbstractWorld<CELL>, part: List<Int>) : Callable<Map<Int, Int>> where CELL : AbstractCell {
        return Callable {
            val candidatesToReborn = HashMap<Int, Int>()
            for (i in part) {
                var aliveNeighbours = 0
                for (direction in Direction.entries) {
                    val curX = getXByIndex(i)
                    val curY = getYByIndex(i)
                    val neighbourIndex = getIndexByLocation(curX + direction.deltaX, curY + direction.deltaY)
                    val neighbourCell = world.getCellAt(neighbourIndex)
                    if (neighbourCell != null) {
                        aliveNeighbours++
                    } else {
                        candidatesToReborn.merge(neighbourIndex, 1, Int::plus)
                    }
                }
                if (aliveNeighbours in GameSetting.neighbourCellsToLive) {
                    world.createRandomCellAt(i)
                }
            }
            return@Callable candidatesToReborn
        }
    }

    private fun <CELL> getCal2WithNoPermission(world: AbstractWorld<CELL>, part: List<Int>, candidates: Map<Int, Int>) : Callable<Unit> where CELL : AbstractCell {
        return Callable {
            for (i in part) {
                if (candidates[i] in GameSetting.neighbourCellsToBeBorn) {
                    world.createRandomCellAt(i)
                }
            }
        }
    }
}