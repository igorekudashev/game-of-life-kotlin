import base.AbstractWorld
import java.util.*
import java.util.concurrent.*

class GameOfLifeWorld : AbstractWorld<Cell>() {

    override fun getRandomizedCell(): Cell {
        return Cell()
    }

    override fun getWorldUpdateTask(permission: UUID): ForkJoinTask<*> {
        return FullAction(this, currentTickGrid.keys.toList(), permission)
    }
}