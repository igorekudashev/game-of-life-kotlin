import java.lang.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

private const val maxSpeed: Int = 1000

class RunnableRepeater(
    private var speed: Int,
    private val runnable: Runnable
) {

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private lateinit var future: ScheduledFuture<*>

    init {
        startJob()
    }

    fun setSpeed(newSpeed: Int) : Int {
        speed = newSpeed.coerceIn(0, maxSpeed)
        future.cancel(false)
        startJob()
        println("New Speed $speed")
        return speed
    }

    private fun startJob() {
        if (speed > 0) {
            val delay = 1000L / speed
            future = executor.scheduleAtFixedRate(runnable, 0, delay, TimeUnit.MILLISECONDS)
        }
    }
}