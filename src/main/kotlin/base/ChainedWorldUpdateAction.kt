package base

import java.util.*
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask

class ChainedWorldUpdateAction<WORLD>(
    val world: WORLD,
    val permission: UUID,
    val input: Any?
) : RecursiveAction() where WORLD : AbstractWorld<out AbstractCell>{

    private val taskChain: MutableList<LateInitInputRecursiveTask<*, *, WORLD>> = mutableListOf()

    fun addTask(task: LateInitInputRecursiveTask<*, *, *>) : ChainedWorldUpdateAction<WORLD> {
        taskChain.add(task as LateInitInputRecursiveTask<*, *, WORLD>)
        return this
    }

    override fun compute() {
        var subOutput = input
        for (task in taskChain) {
            task.delegate(this, subOutput)
            subOutput = task.fork().join()
        }
    }
}

abstract class LateInitInputRecursiveTask<INPUT, OUTPUT, WORLD>(
    protected var input: INPUT? = null
) : RecursiveTask<OUTPUT>() where WORLD : AbstractWorld<out AbstractCell> {

    protected var masterTask: ChainedWorldUpdateAction<WORLD>? = null

    open fun merge(outputs: List<OUTPUT?>) : OUTPUT? {
        return null
    }

    abstract fun requiresFork() : Boolean

    abstract fun splitInput() : List<INPUT?>

    abstract fun process() : OUTPUT?

    abstract fun createNewTask() : LateInitInputRecursiveTask<INPUT, OUTPUT, WORLD>

    final override fun compute(): OUTPUT? {
        return if (requiresFork()) {
            val subTasks = splitInput().map { createNewTask().delegate(masterTask!!, it!!) }
            val result = ForkJoinTask.invokeAll(subTasks).map { it.join() }
            merge(result)
        } else {
            setThreadPermission(masterTask!!.permission)
            val result = process()
            setThreadPermission()
            result
        }
    }

    fun delegate(masterTask: ChainedWorldUpdateAction<WORLD>, input: Any?) : LateInitInputRecursiveTask<INPUT, OUTPUT, WORLD> {
        if (this.masterTask == null) this.masterTask = masterTask
        return withInput(input as INPUT?)
    }

    fun withInput(input: INPUT?) : LateInitInputRecursiveTask<INPUT, OUTPUT, WORLD> {
        if (this.input == null) this.input = input
        return this
    }
}