package base

import kotlin.math.ceil

class CollisionResolverTask : LateInitInputRecursiveTask<Set<Location>, Any?, AbstractWorld<AbstractCell>>() {

    override fun requiresFork(): Boolean {
        return input!!.size > 2500
    }

    override fun splitInput(): List<Set<Location>?> {
        val size = ceil(input!!.size / 10f).toInt()
        return input!!.chunked(size).map { it.toSet() }
    }

    override fun process(): Any? {
        input!!.forEach { masterTask!!.world.removeCellNoNextTick(it) }
        return null
    }

    override fun createNewTask(): LateInitInputRecursiveTask<Set<Location>, Any?, AbstractWorld<AbstractCell>> {
        return CollisionResolverTask()
    }
}