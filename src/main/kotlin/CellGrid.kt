import GameSetting.Companion.worldHeight
import GameSetting.Companion.worldWidth
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

class CellGrid(
    private val cells: MutableMap<Location, Cell> = ConcurrentHashMap()
) {

    fun addCell(location: Location, cell: Cell) {
        val normalizedLocation = getNormalizedLocation(location.x, location.y)
        cells[normalizedLocation] = cell
    }

    fun getCellAt(location: Location) : Cell? {
        return getCellAt(location.x, location.y)
    }

    fun getCellAt(x: Int, y: Int) : Cell? {
        return cells[getNormalizedLocation(x, y)]
    }

    fun getCellsLocationInPartitions(partitionsAmount: Int) : List<List<Location>> {
        return if (cells.isEmpty()) {
            emptyList()
        } else {
            val partitionSize = ceil(cells.size / partitionsAmount.toFloat()).toInt()
            cells.keys.chunked(partitionSize)
        }
    }

    fun getAliveCellLocations() : Set<Location> {
        return cells.keys
    }

    fun getNormalizedLocation(x: Int, y: Int) : Location {
        var normalizedX = x % worldWidth
        var normalizedY = y % worldHeight
        if (normalizedX < 0) normalizedX += worldWidth
        if (normalizedY < 0) normalizedY += worldHeight
        return Location(normalizedX, normalizedY)
    }
}