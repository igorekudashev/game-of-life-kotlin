package base

import java.awt.Dimension
import kotlin.reflect.KMutableProperty

class GameSetting private constructor() {

    companion object {
        private val propertyNameToField: Map<String, KMutableProperty<*>> = mapOf(
            "width" to Companion::worldWidth,
            "height" to Companion::worldHeight,
            "cellSize" to Companion::cellSize,
            "borderSize" to Companion::borderSize,
            "neighbourCellsToLive" to ::neighbourCellsToLive,
            "neighbourCellsToBeBorn" to ::neighbourCellsToBeBorn,
            "lifespan" to Companion::lifespan,
            "maxFps" to Companion::maxFps,
            "worldThreads" to Companion::worldThreads
        )

        var worldWidth: Int = 800; private set
        var worldHeight: Int = 400; private set
        var cellSize: Int = 2; private set
        var borderSize: Int = 0; private set
//        var neighbourCellsToLive: IntSetImitator = IntSetImitator(2, 3, 4, 5, 6, 7); private set
//        var neighbourCellsToBeBorn: IntSetImitator = IntSetImitator(3); private set
        var neighbourCellsToLive: List<Int> = listOf(2, 3); private set
        var neighbourCellsToBeBorn: List<Int> = listOf(3); private set
//        var neighbourCellsToLive: Array<Boolean> = arrayOf(false, false, true, true, true, true, true, true, false); private set
//        var neighbourCellsToBeBorn: Array<Boolean> = arrayOf(false, false, false, true, false, false, false, false, false); private set
        var lifespan: Int = 0; private set
        var maxFps: Int = 144; private set
        var worldThreads: Int = 16; private set

        fun loadProperties(args: Array<String>) {
            args.map { it.split("=".toRegex(), 2) }
                .map { Pair(it[0], it.getOrNull(1)) }
                .forEach { resolve(it) }
        }

        fun getScreenSize() : Dimension {
            return Dimension(worldWidth * cellSize, worldHeight * cellSize)
        }

        private fun resolve(pair: Pair<String, String?>) {
            val setter = propertyNameToField[pair.first]?.setter ?: throw IllegalArgumentException("No such property ${pair.first}")
            val sValue = pair.second
            val value = when (setter.property.returnType) {
                Int::class -> sValue?.toInt()
                Array<Int>::class -> {
                    sValue?.split(",")?.map { it.toInt() }
                }
                else -> null
            } ?: throw IllegalArgumentException("Property ${pair.first} is empty")
            setter.call(value)
        }
    }
}