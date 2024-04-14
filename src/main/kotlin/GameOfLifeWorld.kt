import base.AbstractWorld
import java.util.*
import java.util.concurrent.*

class GameOfLifeWorld : AbstractWorld<Cell>() {



    override fun getRandomizedCell(): Cell {
        return Cell()
    }

    override fun getWorldUpdateTask(): ForkJoinTask<*> {
        return FullAction(this)
    }
}