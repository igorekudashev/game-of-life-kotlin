package base

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
        return grid[normalizeInt(index, size())] as CELL?
    }

    operator fun get(x: Int, y: Int) : CELL? {
        return grid[getIndexByLocation(x, y)] as CELL?
    }

    operator fun set(index: Int, value: CELL?) {
        grid[normalizeInt(index, size())] = value
    }

    operator fun set(x: Int, y: Int, value: CELL?) {
        grid[getIndexByLocation(x, y)] = value
    }

    fun getXByIndex(index: Int) : Int {
        return index % width
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

    fun getIndexRelativeTo(index: Int, offsetX: Int, offsetY: Int) : Int {
        val indexWithOffsetY = index + (offsetY * width)
        val x = index % width
        val newX = normalizeInt(x + offsetX, width)
        return indexWithOffsetY - x + newX
    }

    fun clear() {
        // TODO: -5-10 fps
        synchronized(this) {
            grid.fill(null)
        }
    }

    fun size() : Int {
        return grid.size
    }

    fun copy() : CellGrid<CELL?> {
        // TODO: -5-10 fps
        synchronized(this) {
            return CellGrid(height, width, grid.copyOf())
        }
    }

    override fun toString(): String {
        return "CellGrid(grid=${grid.contentToString()})"
    }
}