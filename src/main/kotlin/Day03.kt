import utils.pow

class Day03 : Day(3, 2025, "Lobby") {

    private val banks = input.lines.map { it.map(Char::digitToInt) }

    override fun part1() = banks.sumOf { it.maxJoltage(2) }

    override fun part2() = banks.sumOf { it.maxJoltage(12) }

}

private fun List<Int>.maxJoltage() = dropLast(1).withIndex().maxOf { (idx, v) ->
    v * 10 + drop(idx + 1).max()
}

val cache = mutableMapOf<Pair<List<Int>, Int>, Long>()

private fun List<Int>.maxJoltage(select: Int): Long = when (select) {
    1 -> max().toLong()
    size -> joinToString("").toLong()
    else -> cache.getOrPut(this to select) {
        dropLast(select - 1).withIndex().maxOf { (idx, v) ->
            v * 10.pow(select - 1) + drop(idx + 1).maxJoltage(select - 1)
        }

    }
}

fun main() {
    solve<Day03> {
        """
            987654321111111
            811111111111119
            234234234234278
            818181911112111
        """.trimIndent() part1 357 part2 3121910778619
    }
}