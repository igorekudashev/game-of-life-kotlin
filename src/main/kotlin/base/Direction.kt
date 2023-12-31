package base

enum class Direction(
    val deltaX: Int,
    val deltaY: Int
) {

    UP(0, -1),
    UP_RIGHT(1, -1),
    UP_LEFT(-1, -1),
    RIGHT(1, 0),
    LEFT(-1, 0),
    DOWN(0, 1),
    DOWN_RIGHT(1, 1),
    DOWN_LEFT(-1, 1);
}