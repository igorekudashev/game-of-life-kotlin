class IntSetImitator(
    vararg values: Int
) {

    private var imitator: Int = 0

    init {
        values.forEach {
            val prepared = prepare(it)
            imitator = imitator.or(prepared)
        }
    }

    fun contains(value: Int) : Boolean {
        val prepared = prepare(value)
        return imitator.and(prepared) == prepared
    }

    private fun prepare(value: Int) : Int {
        return 0b0001 shl (value % 32)
    }
}