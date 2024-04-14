package base

private val codeToKeyMap: MutableMap<Int, GameKey> = HashMap()

fun getKey(code: Int) : GameKey {
    println("map $code $codeToKeyMap")
    return codeToKeyMap[code] ?: GameKey.ANOTHER
}

enum class GameKey(code: Int) {

    R(82),
    PLUS(61),
    MINUS(45),
    C(67),
    ARROW_LEFT(37),
    ARROW_UP(38),
    ARROW_RIGHT(39),
    ARROW_DOWN(40),
    P(80),
    ANOTHER(-1);

    init {
        codeToKeyMap[code] = this
    }
}