import base.Direction
import base.GameSetting.Companion.neighbourCellsToBeBorn
import base.GameSetting.Companion.neighbourCellsToLive
import base.LateInitInputRecursiveTask
import base.Location
import base.setThreadPermission
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.math.ceil

private const val min: Int = 2500

private fun <K, V> splitMap(map: Map<K, V>, size: Int) : List<Map<K, V>> {
    val result = mutableListOf<Map<K, V>>()
    var current = ConcurrentHashMap<K, V>()
    var count = 1;
    for ((k, v) in map.entries) {
        val a = count % size
        if (a == 0) {
            result.add(current)
            current = ConcurrentHashMap()
        } else {
            current[k] = v
        }
        count++
    }
    return result
}

class TestTask : LateInitInputRecursiveTask<Map<Location, Cell>, Map<Location, Int>, GameOfLifeWorld>() {

    override fun requiresFork(): Boolean {
        return input!!.size > min
    }

    override fun splitInput(): List<Map<Location, Cell>?> {
        return splitMap(input!!, 10)
    }

    override fun process(): Map<Location, Int> {
        val world = masterTask!!.world
        val candidatesToReborn = HashMap<Location, Int>()
        input!!.forEach { location, cell ->
            var aliveNeighbours = 0
            for (direction in Direction.values()) {
                val neighbourLocation = location.getRelative(direction)
                val neighbourCell = input!![neighbourLocation]
                if (neighbourCell != null) {
                    aliveNeighbours++
                } else {
                    candidatesToReborn.merge(neighbourLocation, 1, Int::plus)
                }
            }
//            if (neighbourCellsToLive.contains(aliveNeighbours)) {
            if (aliveNeighbours in neighbourCellsToLive) {
                world.addCellNoNextTick(location, cell)
            }
        }
        return candidatesToReborn
    }

    override fun createNewTask(): LateInitInputRecursiveTask<Map<Location, Cell>, Map<Location, Int>, GameOfLifeWorld> {
        return TestTask()
    }

    override fun merge(outputs: List<Map<Location, Int>?>): Map<Location, Int>? {
        return outputs.stream().flatMap {
            it!!.entries.stream()
        }.collect(Collectors.toMap({ entry -> entry.key }, { entry -> entry.value }, Int::plus, { ConcurrentHashMap() }))
    }
}

class TestAction: LateInitInputRecursiveTask<Map<Location, Int>, Any?, GameOfLifeWorld>() {
    override fun requiresFork(): Boolean {
        return input!!.size > min
    }

    override fun splitInput(): List<Map<Location, Int>?> {
        return splitMap(input!!, 10)
    }

    override fun process(): Any? {
        input!!.entries.stream()
            .filter { (_, aliveNeighbours) -> aliveNeighbours in neighbourCellsToBeBorn }
            .forEach { (location, _) ->
                masterTask!!.world.addCellNoNextTick(location, Cell())
            }
        return null;
    }

    override fun createNewTask(): LateInitInputRecursiveTask<Map<Location, Int>, Any?, GameOfLifeWorld> {
        return TestAction()
    }
}