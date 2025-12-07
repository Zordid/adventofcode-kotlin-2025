import utils.*
import kotlin.collections.flatMapTo

class Day07 : Day(7, 2025, "Laboratories") {

    val teleporter = input.grid
    val area = teleporter.area
    val start = teleporter.search('S').single()
    val splitters = teleporter.search('^').toSet()

    override fun part1() =
        area.topToBottom.fold(setOf(start) to 0) { (activeBeams, count), _ ->
            val (onSplitter, noSplitter) = activeBeams.map { it.down }.partition { it in splitters }
            onSplitter.flatMapTo(mutableSetOf()) { listOf(it.left, it.right) } + noSplitter to (count + onSplitter.size)
        }.second

    override fun part2(): Long {
        // initially, without any splitters, every point is reachable from the bottom by a single beam
        val g = MutableGrid<Long>(area) { 1 }
        // now work your way upwards and sum the possible ways to get to each point obeying the splitters
        (area.lowerLeft.y - 1 downTo area.upperRight.y).forEach { y ->
            area.leftToRight.forEach { x ->
                val p = Point(x, y)
                g[p] = if (p in splitters)
                    g[p.down.left] + g[p.down.right]
                else
                    g[p.down]
            }
        }
        // how many ways to get to the start from any point at the bottom?
        return g[start]
    }

}

fun main() {
    solve<Day07> {
        """
            .......S.......
            ...............
            .......^.......
            ...............
            ......^.^......
            ...............
            .....^.^.^.....
            ...............
            ....^.^...^....
            ...............
            ...^.^...^.^...
            ...............
            ..^...^.....^..
            ...............
            .^.^.^.^.^...^.
            ...............
        """.trimIndent() part1 21 part2 40
    }
}

