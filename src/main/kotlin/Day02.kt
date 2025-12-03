import utils.pow

class Day02 : Day(2, 2025, "Gift Shop") {

    private val ranges = input.string.split(',').map { it.split('-').map { it.toLong() }.let { (a, b) -> a..b } }

    override fun part1(): Long = ranges.sumOf { it.countInvalid() }

    fun LongRange.countInvalid(): Long {
        log { this@countInvalid }
        return (start.toString().length..endInclusive.toString().length).sumOf { len ->
            if (len % 2 == 0) {
                val partLen = len / 2
                val factor = 10.pow(partLen)
                val begin = ("1" + "0".repeat(partLen - 1)).repeat(2).toLong().coerceAtLeast(start)
                val end = "9".repeat(len).toLong().coerceAtMost(endInclusive)

                (begin..end).sumOf {
                    val upper = it / factor
                    val lower = it - (upper * factor)
                    if (upper == lower) it.also { log { "Invalid: $it" } } else 0
                }
            } else 0
        }
    }

    override fun part2(): Long = ranges.sumOf { it.countInvalid2() }

    fun LongRange.countInvalid2(): Long {
        log { this@countInvalid2 }
        return (start.toString().length..endInclusive.toString().length).flatMap { len ->
            (2..len).flatMap { parts ->
                if (len % parts == 0) {
                    val partLen = len / parts
                    val factor = 10.pow(partLen)
                    val begin = ("1" + "0".repeat(partLen - 1)).repeat(parts).toLong().coerceAtLeast(start)
                    val end = "9".repeat(len).toLong().coerceAtMost(endInclusive)
                    log { "Checking $parts parts of length $partLen ($begin..$end)" }

                    (begin..end).filter {
                        val portion = it % factor
                        "$portion".repeat(parts).toLong() == it
                    }
                } else emptyList()
            }
        }.toSet().sum()
    }

}

fun main() {
    solve<Day02> {
        """
            11-22,95-115,998-1012,1188511880-1188511890,222220-222224,
            1698522-1698528,446443-446449,38593856-38593862,565653-565659,
            824824821-824824827,2121212118-2121212124
        """.trimIndent() part1 1227775554 part2 4174379265
    }
}