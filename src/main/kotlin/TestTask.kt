import base.*
import base.GameSetting.Companion.neighbourCellsToBeBorn
import base.GameSetting.Companion.neighbourCellsToLive
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask
import java.util.stream.Collectors
import kotlin.math.ceil

private const val min: Int = 2500

private fun <K, V> splitMap(map: Map<K, V>, size: Int) : List<Map<K, V>> {
    val result = mutableListOf<Map<K, V>>()
    var current = HashMap<K, V>()
    map.forEach { (k, v) ->
        current[k] = v
        if (current.size == size) {
            result.add(current)
            current = HashMap()
        }
    }
    if (current.isNotEmpty()) {
        result.add(current)
    }
    return result
}

class TestTask : LateInitInputRecursiveTask<Map<Location, Cell>, Map<Location, Int>, GameOfLifeWorld>() {

    override fun requiresFork(): Boolean {
        return input.size > min
    }

    override fun getSubTasks(): List<TestTask> {
        return splitMap(input, 500).map {
            val task = TestTask()
            task.prepare(context, it)
            task
        }
    }

    override fun process(): Map<Location, Int> {
        val candidatesToReborn = HashMap<Location, Int>()
        input.forEach { (location, cell) ->
            var aliveNeighbours = 0
            for (direction in Direction.values()) {
                val neighbourLocation = location.getRelative(direction)
                val neighbourCell = context.world.getCellAt(neighbourLocation)
                if (neighbourCell != null) {
                    aliveNeighbours++
                } else {
                    candidatesToReborn.merge(neighbourLocation, 1, Int::plus)
                }
            }
//            if (neighbourCellsToLive.contains(aliveNeighbours)) {
            if (aliveNeighbours in neighbourCellsToLive) {
                context.world.addCellNoNextTick(location, cell)
            }
        }
        return candidatesToReborn
    }

    override fun merge(outputs: List<Map<Location, Int>>): Map<Location, Int> {
        return outputs.stream().flatMap {
            it.entries.stream()
        }.collect(Collectors.toMap({ it.key }, { it.value }, Int::plus))
    }
}

class TestAction: LateInitInputRecursiveAction<Map<Location, Int>, GameOfLifeWorld>() {
    override fun requiresFork(): Boolean {
        return input.size > min
    }

    override fun getSubTasks(): List<TestAction> {
        return splitMap(input, 500).map {
            val task = TestAction()
            task.prepare(context, it)
            task
        }
    }

    override fun process() {
        input.entries.stream()
            .filter { (_, aliveNeighbours) -> aliveNeighbours in neighbourCellsToBeBorn }
            .forEach { (location, _) ->
                context.world.addCellNoNextTick(location, Cell())
            }
    }
}