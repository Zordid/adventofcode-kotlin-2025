import utils.*

class Day05 : Day(5, 2025, "Cafeteria") {

    val freshIngredientRanges = input.sections[0].map {
         it.split('-').let { (a, b) -> a.toLong()..b.toLong() }
    }

    val ingredients = input.sections[1].map { it.toLong() }

    override fun part1() = ingredients.count { i ->
        freshIngredientRanges.any { r -> i in r }
    }

    override fun part2(): Long {
        val mergedRanges = freshIngredientRanges.sortedBy { it.first }.fold(emptyList<LongRange>()) { acc, next ->
            val prev = acc.lastOrNull() ?: return@fold listOf(next)

            when { // A: prev, B: next
                // (A----) [B~~~~]  not touching at all
                next.first > prev.last + 1 -> acc.plusElement(next)
                // (A---[B==)~~~~]  overlapping or touching
                next.last > prev.last -> acc.dropLast(1).plusElement(prev.first..next.last)
                // (A---[B==]---A)  completely overlapping
                else -> acc
            }
        }
        return mergedRanges.sumOf { it.size }
    }

}

fun main() {
    solve<Day05> {
        """
            3-5
            10-14
            16-20
            12-18

            1
            5
            8
            11
            17
            32
        """.trimIndent() part1 3 part2 14
    }
}