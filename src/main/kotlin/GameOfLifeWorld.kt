import base.AbstractWorld
import base.ChainedWorldUpdateAction
import base.GameSetting
import java.util.*
import java.util.concurrent.*

class GameOfLifeWorld : AbstractWorld<Cell>() {

    override fun getRandomizedCell(): Cell {
        return Cell()
    }

    override fun getArray(): Array<Cell?> {
        return arrayOfNulls(GameSetting.worldWidth * GameSetting.worldHeight)
    }

    override fun getWorldUpdateTask(permission: UUID): ChainedWorldUpdateAction<GameOfLifeWorld> {
        return ChainedWorldUpdateAction(this, permission)
//            .addTask(TestTask(), currentTickGrid.keys.toList())
//            .addTask(TestAction())
    }
}