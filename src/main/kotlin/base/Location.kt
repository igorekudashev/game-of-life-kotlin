package base

import base.GameSetting.Companion.worldHeight
import base.GameSetting.Companion.worldWidth

class Location(
    val x: Int,
    val y: Int
) {

    fun getRelative(direction: Direction) : Location {
        return Location(x + direction.deltaX, y + direction.deltaY)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Location) return false

        if (normalizeX(x) != normalizeX(other.x)) return false
        if (normalizeY(y) != normalizeY(other.y)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = normalizeX(x)
        result = 31 * result + normalizeY(y)
        return result
    }

    private fun normalizeX(x: Int) : Int {
        return normalizeInt(x, worldWidth)
    }

    private fun normalizeY(y: Int) : Int {
        return normalizeInt(y, worldHeight)
    }
}