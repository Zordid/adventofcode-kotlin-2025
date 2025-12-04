import utils.*

class Day04 : Day(4, 2025, "Printing Department") {

    private val map = input.grid
    private val area = map.area

    override fun part1() =
        area.allPoints().count { p ->
            map[p] == '@' && p.surroundingNeighbors(area).count { map[it] == '@' } < 4
        }

    override fun part2(): Int {
        val map = map.toMutableGrid()
        var total = 0
        do {
            val canRemove = area.allPoints().filter { p ->
                map[p] == '@' && p.surroundingNeighbors(area).count { map[it] == '@' } < 4
            }.toList()
            canRemove.forEach { map[it] = '.' }
            total += canRemove.size
        } while (canRemove.isNotEmpty())
        return total
    }

}

fun main() {
    solve<Day04> {
        """
            ..@@.@@@@.
            @@@.@.@.@@
            @@@@@.@.@@
            @.@@@@..@.
            @@.@@@@.@@
            .@@@@@@@.@
            .@.@.@.@@@
            @.@@@.@@@@
            .@@@@@@@@.
            @.@.@@@.@.
        """.trimIndent() part1 13 part2 43
    }
}