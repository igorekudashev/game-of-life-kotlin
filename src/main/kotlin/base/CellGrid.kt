package base

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

//fun getIndexByLocation(x: Int, y: Int) : Int {
//    return normalizeY(y) * GameSetting.worldWidth + normalizeX(x)
//}
//
//fun getXByIndex(index: Int) : Int {
//    val y = getYByIndex(index)
//    return index - y * GameSetting.worldWidth
//}
//
//fun getYByIndex(index: Int) : Int {
//    return index / GameSetting.worldWidth
//}
//
//private fun normalizeX(x: Int) : Int {
//    return normalizeInt(x, GameSetting.worldWidth)
//}
//
//private fun normalizeY(y: Int) : Int {
//    return normalizeInt(y, GameSetting.worldHeight)
//}

class CellGrid<CELL : AbstractCell?> {

    val height: Int
    val width: Int
    private val grid: Array<Any?>

    constructor(height: Int, width: Int) {
        this.height = height
        this.width = width
        this.grid = Array(height * width) { null }
    }

    private constructor(height: Int, width: Int, grid: Array<Any?>) {
        this.height = height
        this.width = width
        this.grid = grid
    }

    operator fun get(index: Int): CELL? {
        return grid[index] as CELL?
    }

    operator fun get(x: Int, y: Int) : CELL? {
        return this[getIndexByLocation(x, y)]
    }

    operator fun set(index: Int, value: CELL?) {
        grid[index] = value
    }

    operator fun set(x: Int, y: Int, value: CELL?) {
        this[getIndexByLocation(x, y)] = value
    }

    fun getXByIndex(index: Int) : Int {
        val y = getYByIndex(index)
        return index - y * width
    }

    fun getYByIndex(index: Int) : Int {
        return index / width
    }

    fun getIndexByLocation(x: Int, y: Int) : Int {
        return normalizeY(y) * width + normalizeX(x)
    }

    fun normalizeX(x: Int) : Int {
        return normalizeInt(x, width)
    }

    fun normalizeY(y: Int) : Int {
        return normalizeInt(y, height)
    }

    fun clear() {
        grid.fill(null)
    }

    fun size() : Int {
        return grid.size
    }

    fun copy() : CellGrid<CELL?> {
        return CellGrid(height, width, grid.copyOf())
    }

    override fun toString(): String {
        return "CellGrid(grid=${grid.contentToString()})"
    }
}