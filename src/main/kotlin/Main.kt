import base.GameSetting.Companion.loadProperties

fun main(args: Array<String>) {
    loadProperties(args)
    GameOfLifeWindow()
}