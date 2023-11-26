package base

private val codeToKeyMap: MutableMap<Int, GameKey> = HashMap()

fun getKey(code: Int) : GameKey {
    return codeToKeyMap[code] ?: GameKey.ANOTHER
}

enum class GameKey(code: Int) {

    KEY_R(82),
    KEY_PLUS(61),
    KEY_MINUS(45),
    KEY_C(67),
    ANOTHER(-1);

    init {
        codeToKeyMap[code] = this
    }
}