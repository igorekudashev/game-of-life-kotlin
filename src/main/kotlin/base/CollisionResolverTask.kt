//package base
//
//import kotlin.math.ceil
//
//class CollisionResolverAction : LateInitInputRecursiveAction<Set<Location>, AbstractWorld<AbstractCell>>() {
//
//    override fun requiresFork(): Boolean {
//        return input.size > 2500
//    }
//
//    override fun getSubTasks(): List<CollisionResolverAction> {
//        val size = ceil(input.size / 10f).toInt()
//        return input.chunked(size).map {
//            val task = CollisionResolverAction()
//            task.prepare(context, it)
//            task
//        }
//    }
//
//    override fun process() {
////        input.forEach { context.world.removeCellNoNextTick(it) }
//    }
//}