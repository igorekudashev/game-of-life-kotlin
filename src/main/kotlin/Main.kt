import GameSetting.Companion.loadProperties

fun main(args: Array<String>) {
    loadProperties(args)
    GameWindow(World())
}