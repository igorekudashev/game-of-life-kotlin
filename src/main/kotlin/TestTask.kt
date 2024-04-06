import base.*
import base.GameSetting.Companion.neighbourCellsToBeBorn
import base.GameSetting.Companion.neighbourCellsToLive
import java.util.*
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.ceil

private const val min: Int = 2500

class TestTask(
        private val world: GameOfLifeWorld,
        private val from: Int,
        private val to: Int
) : RecursiveTask<Map<Int, Int>>() {

    override fun compute() : Map<Int, Int> {
        // TODO: попробовать вариант с разделением на таски за раз и вариант с рекурсивным разделением
        return if (to - from > min) {
            ForkJoinTask.invokeAll(getSubTask()).stream()
                .map { it.join() }
                .flatMap { it.entries.stream() }
                .collect(Collectors.toMap({ entry -> entry.key }, { entry -> entry.value }, Int::plus))
        } else {
            processGridRegionUpdate()
        }
    }

    private fun getSubTask() : List<TestTask> {
        val subTasks = ArrayList<TestTask>()
        for (i in from until to step min) {
            subTasks.add(TestTask(world, i, i + min))
        }
        return subTasks
    }

    private fun processGridRegionUpdate() : Map<Int, Int> {
        val candidatesToReborn = HashMap<Int, Int>()
        for (i in from until to) {
            if (world.currentTickGrid[i] == null) continue
            var aliveNeighbours = 0
            val curX = world.currentTickGrid.getXByIndex(i)
            val curY = world.currentTickGrid.getYByIndex(i)
            for (direction in Direction.entries) {
                val neighbourIndex = world.currentTickGrid.getIndexByLocation(curX + direction.deltaX, curY + direction.deltaY)
                val neighbourCell = world.currentTickGrid[neighbourIndex]
                if (neighbourCell != null) {
                    aliveNeighbours++
                } else {
                    candidatesToReborn.merge(neighbourIndex, 1, Int::plus)
                }
            }
            if (aliveNeighbours in neighbourCellsToLive) {
                world.nextTickGrid[i] = world.currentTickGrid[i]
            }
        }
        return candidatesToReborn
    }
}

class TestAction(
        private val world: GameOfLifeWorld,
        private val neighboursCounter: Map<Int, Int>,
        private val countedCellIndices: List<Int>
) : RecursiveAction() {

    override fun compute() {
        if (countedCellIndices.size > min) {
            ForkJoinTask.invokeAll(getSubTask()).forEach { it.join() }
        } else {
            countedCellIndices.stream()
                    .map { it to neighboursCounter[it] }
                    .filter { (_, aliveNeighbours) -> aliveNeighbours in neighbourCellsToBeBorn }
                    .forEach { (i, _) -> world.nextTickGrid[i] = Cell() }
        }
    }

    private fun getSubTask() : List<TestAction> {
        val size = ceil(countedCellIndices.size / 10f).toInt()
        return countedCellIndices.chunked(size).map { TestAction(world, neighboursCounter, it) }
    }
}

class FullAction(
        private val world: GameOfLifeWorld
) : RecursiveAction() {

    override fun compute() {
        val output = TestTask(world,0, world.currentTickGrid.size()).fork().join()
        TestAction(world, output, output.keys.toList()).fork().join()
    }
}