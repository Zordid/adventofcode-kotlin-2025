import utils.*

class Day12 : Day(12, 2025, "Christmas Tree Farm") {

    data class Present(val id: Int, val grid: Grid<Char>) {
        val area = grid.area
        val space = area.allPoints().count { grid[it] == '#' }
        val allRotations =
            listOf(grid, grid.flippedX()).flatMap { listOf(it, it.rotate90(), it.rotate180(), it.rotate270()) }.map { rot ->
                area.allPoints().filter { rot[it] == '#' }.toList()
            }
    }

    val presents: List<Present> = input.sections.dropLast(1).mapIndexed { i, p ->
        Present(i, p.lines.drop(1).toGrid())
    }
    val r = input.sections.last().map {
        it.substringBefore(':').extractAllIntegers().let { (w, h) -> Area(w, h) } to
                it.substringAfter(':').extractAllIntegers()
    }

    override fun part1(): Int {
        var pos = 0
        return r.count { (area, counts) ->
            pos++
            val toFit = counts.withIndex().map {
                presents[it.index] to it.value
            }.filter { it.second > 0 }.toMap()
            val totalSpaceRequired = toFit.entries.sumOf { (p, count) ->
                count * p.space
            }
            val totalSpaceAvailable = area.size.toInt()

            val graph = graph<State>(neighborsOf = { s ->
                log { "Calculating neighbors of" }
                log { s.grid.plot() }
                log { "with ${s.remaining.values.sum()} more pieces to fit" }

                if (s.spaceAvailable < s.spaceRequired) emptyList() else
                    s.remaining.keys.flatMap { next ->
                        val possible = next.allRotations.mapNotNull { rot ->
                            s.grid.fitPresent(area, rot, Char('A'.code + s.placed))
                        }
                        val remainingFit = s.remaining.toMutableMap().apply {
                            val rem = this[next]!! - 1
                            if (rem == 0) this.remove(next) else this[next] = rem
                        }
                        val newSpaceAvailable = s.spaceAvailable - next.space
                        val newSpaceRequired = remainingFit.entries.sumOf { it.key.space + it.value }
                        possible.map {
                            State(it, newSpaceAvailable, newSpaceRequired, s.placed + 1, remainingFit)
                        }
                    }
            })

            val startState = State(Grid(area) { '.' }, totalSpaceAvailable, totalSpaceRequired, 0, toFit)
            val solution = graph.depthFirstSearch(startState) {
                it.remaining.isEmpty()
            }
            solution.isNotEmpty().also {
                alog { "$pos: $it" }
            }
        }
    }

    data class State(
        val grid: Grid<Char>,
        val spaceAvailable: Int,
        val spaceRequired: Int,
        val placed: Int,
        val remaining: Map<Present, Int>,
    )

    fun Grid<Char>.fitPresent(area: Area, present: List<Point>, mark: Char = '#'): Grid<Char>? {
        area.forEach { p ->
            if (present.all { pc -> getOrNull(p + pc) == '.' }) {
                return toMutableGrid().also { m ->
                    present.forEach { pc -> m[p + pc] = mark }
                }
            }
        }
        return null
    }

}

fun main() {
    solve<Day12> {
        """
            0:
            ###
            ##.
            ##.

            1:
            ###
            ##.
            .##

            2:
            .##
            ###
            ##.

            3:
            ##.
            ###
            ##.

            4:
            ###
            #..
            ###

            5:
            ###
            .#.
            ###

            4x4: 0 0 0 0 2 0
            12x5: 1 0 1 0 2 2
            12x5: 1 0 1 0 3 2
        """.trimIndent() part1 2 part2 null
    }
}