import utils.*

class Day05 : Day(5, 2025, "Cafeteria") {

    val freshIngredientRanges = input.sections[0].lines
        .map { it.split('-').let { (a, b) -> a.toLong()..b.toLong() } }

    val ingredients = input.sections[1].lines.map { it.toLong() }

    override fun part1(): Any? {
        return ingredients.count { i ->
            val isFresh = freshIngredientRanges.any { r -> i in r }
            log { "$i is fresh: $isFresh" }
            isFresh
        }
    }

    override fun part2(): Any? {
        val ranges = freshIngredientRanges.sortedBy { it.first }.toMutableList()
        var merged: Boolean
        do {
            ranges.withIndex().zipWithNext().forEach { (r1i, r2i) ->
                val (idx, r1) = r1i
                val (_, r2) = r2i

                // complete within
                if (r2.first in r1 && r2.last in r1) {
                    ranges[idx + 1] = 0L..-1
                }
                // direct attach
                else if (r2.first == r1.last + 1) {
                    ranges[idx] = r1.first..r2.last
                    ranges[idx + 1] = 0L..-1
                } else if (r2.first in r1 && r2.last !in r1) {
                    ranges[idx] = r1.first..r2.last
                    ranges[idx + 1] = 0L..-1
                }
            }
            val before = ranges.size
            ranges.removeIf { it.size == 0L }
            merged = ranges.size != before
        } while (merged)

        log { ranges.joinToString("\n") }

        return ranges.sumOf { it.size }
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