package utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class PointsAndAreasTest : FunSpec({

    context("orientation of points in a plane") {
        withData(
            nameFn = { "clockwise orientation should be detected" },
            """
                A.
                .B
                C.
            """,
            """
                ...B.
                ....C
                A....
            """,
            """
                ....C
                B....
                ....A
            """,
            """
                .......
                ....A..
                .......
                .C.....
                ....B..
                .......
            """,
        ) { s ->
            parseGrid(s).let { (a, b, c) ->
                orientation(a, b, c) shouldBe Orientation.CLOCKWISE
            }
        }

        withData(
            nameFn = { "counterclockwise orientation should be detected" },
            """
                .A
                B.
                .C
            """,
            """
                ...C.
                ....B
                A....
            """,
            """
                ....B
                C....
                ....A
            """,
            """
                ..............
                ....A.........
                ..............
                .B............
                ....C.........
                ..............
            """,
        ) { s ->
            parseGrid(s).let { (a, b, c) ->
                orientation(a, b, c) shouldBe Orientation.COUNTERCLOCKWISE
            }
        }

        withData(
            nameFn = { "collinear orientation should be detected" },
            """
                ABC
            """,
            """
                A
                B
                C
            """,
            """
                ....A....
                .........
                ....B....
                .........
                ....C....
            """,
            """
                A..
                .B.
                ..C
            """,
            """
                A....
                ..B..
                ....C
            """,
            """
                ....C
                ..B..
                A....
            """,
        ) { s ->
            parseGrid(s).let { (a, b, c) ->
                orientation(a, b, c) shouldBe Orientation.COLLINEAR
            }
        }

    }


})

private fun parseGrid(s: String): List<Point> {
    val grid = s.trimIndent().toGrid()
    val map = grid.toMapGrid(' ')
    @Suppress("UNCHECKED_CAST")
    return map.values.toSet().sorted().mapNotNull { grid.search(it).singleOrNull() }
}
