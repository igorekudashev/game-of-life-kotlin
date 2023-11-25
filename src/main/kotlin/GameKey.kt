private val codeToKeyMap: MutableMap<Int, GameKey> = HashMap()

fun getKey(code: Int) : GameKey {
    return codeToKeyMap[code] ?: GameKey.ANOTHER
}

enum class GameKey(private val code: Int) {

    RANDOMIZE(82),
    SPEED_UP(61),
    SPEED_DOWN(45),
    CLEAR(67),
    ANOTHER(-1);

    init {
        codeToKeyMap[code] = this
    }
}