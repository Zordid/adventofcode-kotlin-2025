package utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe

class GridExtensionsTest : FunSpec({

    val matrix = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))

    context("transpose") {
        test("works for non square grid") {
            val grid = Grid(3, 2) { it }

            grid.transposed() shouldBe Grid(2, 3) { it.second to it.first }

            grid.transposed().transposed() shouldBe grid
        }
    }

    val grid3x3 = """
        123
        456
        789
    """.trimIndent().toGrid()

    context("rotate 90") {
        test("for 3x3 grid") {
            grid3x3.rotate90() shouldBe """
                741
                852
                963
            """.trimIndent().toGrid()

            grid3x3.rotate90().rotate90().rotate90().rotate90() shouldBe grid3x3
        }
        test("works for non square grid") {
            val grid = """
                123
                456
            """.trimIndent().toGrid()

            grid.rotate90() shouldBe """
                41
                52
                63
            """.trimIndent().toGrid()

            grid.rotate90().rotate90().rotate90().rotate90() shouldBe grid
        }
    }

    context("rotate 180") {
        test("works for 3x3 grid") {
            grid3x3.rotate180() shouldBe """
                987
                654
                321
            """.trimIndent().toGrid()

            grid3x3.rotate180().rotate180() shouldBe grid3x3

            grid3x3.rotate180() shouldBe grid3x3.rotate90().rotate90()
        }

        test("works for non square grid") {
            val grid = """
                123
                456
            """.trimIndent().toGrid()

            grid.rotate180() shouldBe """
                654
                321
            """.trimIndent().toGrid()
        }
    }

    context("rotate 270") {
        test("works for 3x3 grid") {
            grid3x3.rotate270() shouldBe """
                369
                258
                147
            """.trimIndent().toGrid()

            grid3x3.rotate270().rotate270().rotate270().rotate270() shouldBe grid3x3
            grid3x3.rotate90().rotate270() shouldBe grid3x3
            grid3x3.rotate270().rotate270() shouldBe grid3x3.rotate180()
        }

        test("works for non square grid") {
            val grid = """
                123
                456
            """.trimIndent().toGrid()

            grid.rotate270() shouldBe """
                36
                25
                14
            """.trimIndent().toGrid()
        }
    }

    test("create and work with Grid") {

        val g = matrix.asGrid()

        g.width shouldBeExactly 3
        g.height shouldBeExactly 3
        g.area shouldBe (origin areaTo (2 to 2))

        g[origin] shouldBeExactly 1
        g[g.lastPoint] shouldBeExactly 9

        g.search(2, 5, 8).toList() shouldBe listOf(1 to 0, 1 to 1, 1 to 2)

        g.toMapGrid().size shouldBe 9
        val mapGrid = g.toMapGrid { it % 2 == 0 }
        mapGrid.size shouldBe 5


        println(Grid(mapOf((2 to 2) to 9), 0).plot())

    }

    test("plotting of tests") {
        val maze = """
        #####
        #..E#   #####
        #...#   #...#
        #.#.#####.#.#####
        #S#.......*....Z# 
        #################
    """.trimIndent().toGrid()

        println(
            maze.plot(
                elementWidth = 3,
                colors = maze.autoColoring()
            )
        )

        println(
            maze.fixed("X").plot(
                colors = maze.autoColoring()
            )
        )
    }

    context("exploding") {
        test("works") {
            val grid = """
                123
                456
                789
            """.trimIndent().toGrid()

            val exploded = grid.explode()
            println(exploded.plot())

            println(grid.explode('*').plot())
        }
    }

})