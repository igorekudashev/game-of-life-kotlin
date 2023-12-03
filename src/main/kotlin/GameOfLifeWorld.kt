import base.AbstractWorld
import base.ChainedWorldUpdateAction
import java.util.*
import java.util.concurrent.*

class GameOfLifeWorld : AbstractWorld<Cell>() {

    override fun getRandomizedCell(): Cell {
        return Cell()
    }

    override fun getWorldUpdateTask(permission: UUID): ChainedWorldUpdateAction<GameOfLifeWorld> {
        return ChainedWorldUpdateAction(this, permission)
            .addTask(TestTask(), currentTickGrid)
            .addTask(TestAction())
    }
}