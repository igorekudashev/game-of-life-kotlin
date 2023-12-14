package base

import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask

val DUMMY = Any()
private val pool: ForkJoinPool = ForkJoinPool(GameSetting.worldThreads)

class ChainedWorldUpdateAction<WORLD>(
    world: WORLD,
    permission: UUID
) where WORLD : AbstractWorld<out AbstractCell>{

    private val context: ForkJoinContext<WORLD> = ForkJoinContext(world, permission)

    private val taskChain: MutableList<LateInitInputRecursive<out Any, out Any?, WORLD>> = mutableListOf()
    private val inputs: MutableList<Any?> = mutableListOf()

    fun addTask(task: LateInitInputRecursive<out Any, out Any?, WORLD>, input: Any? = null) : ChainedWorldUpdateAction<WORLD> {
        taskChain.add(task)
        inputs.add(input)
        return this
    }

    fun update() {
        var temp: Any? = null
        for (i in 0 until taskChain.size) {
            val task = taskChain[i]
            val input = inputs[i]
            task.prepare(context, input ?: temp)
            temp = pool.invoke(task as ForkJoinTask<*>)
//            temp = task.invoke()
        }
    }
}

abstract class LateInitInputRecursive<INPUT : Any, OUTPUT, WORLD>(
    private val canMerge: Boolean
) : RecursiveTask<OUTPUT>() where WORLD : AbstractWorld<out AbstractCell> {

    protected lateinit var context: ForkJoinContext<WORLD>
    protected lateinit var input: INPUT

    abstract fun merge(outputs: List<OUTPUT>) : OUTPUT

    abstract fun requiresFork() : Boolean

    abstract fun getSubTasks() : List<ForkJoinTask<OUTPUT>>

    abstract fun process() : OUTPUT

    final override fun compute(): OUTPUT {
        return if (requiresFork()) {
            val result = ForkJoinTask.invokeAll(getSubTasks()).map { it.join() }
            merge(result)
        } else {
            setThreadPermission(context.permission)
            val result = process()
            setThreadPermission()
            result
        }
    }

    fun prepare(context: ForkJoinContext<WORLD>, input: Any?) {
        this.context = context
        if (input != null) {
            this.input = input as INPUT
        }
    }
}

abstract class LateInitInputRecursiveTask<INPUT : Any, OUTPUT, WORLD> : LateInitInputRecursive<INPUT, OUTPUT, WORLD>(true) where WORLD : AbstractWorld<out AbstractCell> {
}

abstract class LateInitInputRecursiveAction<INPUT : Any, WORLD> : LateInitInputRecursive<INPUT, Any, WORLD>(false) where WORLD : AbstractWorld<out AbstractCell> {

    final override fun merge(outputs: List<Any>): Any {
        return DUMMY
    }
}



class ForkJoinContext<WORLD>(
    val world: WORLD,
    val permission: UUID
) where WORLD : AbstractWorld<out AbstractCell> {

}