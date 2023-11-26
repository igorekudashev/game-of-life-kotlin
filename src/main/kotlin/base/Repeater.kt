package base

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

private const val MAX_SPEED: Int = 1000

class Repeater(
    private var speed: Int,
    private val task: () -> Unit
) {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private lateinit var future: ScheduledFuture<*>

    init {
        startJob()
    }

    fun setSpeed(newSpeed: Int) : Int {
        speed = newSpeed.coerceIn(0, MAX_SPEED)
        future.cancel(false)
        startJob()
        println("New Speed $speed")
        return speed
    }

    private fun startJob() {
        if (speed > 0) {
            val delay = 1000L / speed
            future = executor.scheduleAtFixedRate(task, 0, delay, TimeUnit.MILLISECONDS)
        }
    }
}