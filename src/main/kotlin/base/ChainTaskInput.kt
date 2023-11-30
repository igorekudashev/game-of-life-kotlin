package base

import java.util.*

abstract class ChainTaskInput<WORLD : AbstractWorld<out AbstractCell>, T>(
    val world: WORLD,
    val permission: UUID,
    val payload: T
) {
}