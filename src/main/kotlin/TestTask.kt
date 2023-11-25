import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask
import java.util.stream.Collectors
import kotlin.math.ceil

private const val min: Int = 2500

class TestTask(
    private val locations: List<Location>,
    private val function: (List<Location>) -> Map<Location, Int>
) : RecursiveTask<Map<Location, Int>>() {

    override fun compute() : Map<Location, Int> {
        return if (locations.size > min) {
            ForkJoinTask.invokeAll(getSubTask()).stream()
                .map { it.join() }
                .flatMap { it.entries.stream() }
                .collect(Collectors.toMap({ entry -> entry.key }, { entry -> entry.value }, Int::plus))
        } else {
            function.invoke(locations)
        }
    }

    private fun getSubTask() : List<TestTask> {
        val size = ceil(locations.size / 10f).toInt()
        return locations.chunked(size).map { TestTask(it, function) }
    }
}