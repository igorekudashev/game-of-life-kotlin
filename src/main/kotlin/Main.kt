import base.GameSetting.Companion.loadProperties

fun main(args: Array<String>) {
    loadProperties(args)
    GameOfLifeWindow().start()
}

// 195 не рекурсивное деление по 2500 16 потоков
// 195 не рекурсивное деление по 2500 32 потоков
// 150 не рекурсивное деление по 500 16 потоков
// 205 не рекурсивное деление по 5000 16 потоков
// 220 не рекурсивное деление по 10000 16 потоков
// 205 не рекурсивное деление по 10000 8 потоков
// 210 не рекурсивное деление по 20000 16 потоков
// 215 не рекурсивное деление по 16000 16 потоков
// 125 рекурсивное деление по 10000 16 потоков
// 133 рекурсивное деление по 16000 16 потоков