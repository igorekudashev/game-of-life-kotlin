package base

fun normalizeInt(value: Int, limit: Int) : Int {
    val normalizedCoordinate = value % limit
    return if (normalizedCoordinate < 0) {
        normalizedCoordinate + limit
    } else {
        normalizedCoordinate
    }
}

class Utils private constructor() {
}