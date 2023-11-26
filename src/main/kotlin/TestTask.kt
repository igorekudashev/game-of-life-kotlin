import base.Direction
import base.GameSetting.Companion.neighbourCellsToBeBorn
import base.GameSetting.Companion.neighbourCellsToLive
import base.Location
import base.setThreadPermission
import java.util.*
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.math.ceil

private const val min: Int = 2500

class TestTask(
    private val world: GameOfLifeWorld,
    private val locations: List<Location>,
    private val permission: UUID
) : RecursiveTask<Map<Location, Int>>() {

    override fun compute() : Map<Location, Int> {
        return if (locations.size > min) {
            ForkJoinTask.invokeAll(getSubTask()).stream()
                .map { it.join() }
                .flatMap { it.entries.stream() }
                .collect(Collectors.toMap({ entry -> entry.key }, { entry -> entry.value }, Int::plus))
        } else {
            processGridRegionUpdate(world, locations)
        }
    }

    private fun getSubTask() : List<TestTask> {
        val size = ceil(locations.size / 10f).toInt()
        return locations.chunked(size).map { TestTask(world, it, permission) }
    }

    private fun processGridRegionUpdate(world: GameOfLifeWorld, region: List<Location>) : Map<Location, Int> {
        setThreadPermission(permission)
        val candidatesToReborn = HashMap<Location, Int>()
        region.forEach { location ->
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
//            if (neighbourCellsToLive.contains(aliveNeighbours)) {
            if (neighbourCellsToLive[aliveNeighbours]) {
                world.addCellNoNextTick(location, world.getCellAt(location)!!)
            }
        }
        setThreadPermission()
        return candidatesToReborn
    }
}

class TestAction(
    private val world: GameOfLifeWorld,
    private val map: Map<Location, Int>,
    private val locations: List<Location>,
    private val permission: UUID
) : RecursiveAction() {

    override fun compute() {
        if (locations.size > min) {
            ForkJoinTask.invokeAll(getSubTask()).forEach { it.join() }
        } else {
            setThreadPermission(permission)
            locations.stream()
                .map { it to map[it] }
                .filter { (_, aliveNeighbours) -> neighbourCellsToBeBorn[aliveNeighbours!!] }
                .forEach { (location, _) ->
                    world.addCellNoNextTick(location, Cell())
                }
            setThreadPermission()
        }
    }

    private fun getSubTask() : List<TestAction> {
        val size = ceil(locations.size / 10f).toInt()
        return locations.chunked(size).map { TestAction(world, map, it, permission) }
    }
}

class FullAction(
    private val world: GameOfLifeWorld,
    private val locations: List<Location>,
    private val permission: UUID
) : RecursiveAction() {

    override fun compute() {
        val output = TestTask(world, locations, permission).fork().join()
        TestAction(world, output, output.keys.toList(), permission).fork().join()
    }
}