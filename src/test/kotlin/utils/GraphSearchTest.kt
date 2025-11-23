package utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class GraphSearchTest : FunSpec({

    val maze = """
        #####
        #..E#   #####
        #...#   #...#
        #.#.#####.#.#####
        #S#.......*....Z#
        #################
    """.trimIndent().toGrid().fixed('#')

    val start = maze['S']
    val end = maze['E']
    val z = maze['Z']

    val graph = graph<Point>(
        { it.directNeighbors(maze.area).filter { maze[it] != '#' } },
        { f, t -> if (maze[t] == '*') 10 else 1 }
    )

    context("Dijkstra") {
        test("finds shortest path from S to E") {
            val forward = graph.dijkstraSearch(start, end)
            val backward = graph.dijkstraSearch(end, start)
            forward should {
                it.success.shouldBeTrue()
                it.distanceToStart shouldBe 5
                it.path shouldHaveSize 6
            }
            forward.pathTo(start) shouldBe listOf(start)
            forward.pathTo(z).shouldBeEmpty()
            backward should {
                it.success.shouldBeTrue()
                it.distanceToStart shouldBe 5
                it.path shouldHaveSize 6
            }
        }

        test("can calculate all distances by search null") {
            val result = graph.dijkstraSearch(start, null)
            result should {
                it.success.shouldBeFalse()
                it.distanceToStart.shouldBeNull()
                it.path.shouldBeEmpty()

                it.pathTo(end) shouldHaveSize 6
                it.pathTo(z) shouldHaveSize 23
                it.distanceTo(z) shouldBe 22
            }

            println(result.distance.plot(elementWidth = result.distance.values.maxOf { "$it".length }))
        }

        test("finds all shortest paths from S to E") {
            val forward = graph.dijkstraSearchAll(start, end)
            val backward = graph.dijkstraSearchAll(end, start)
            forward should {
                it.success.shouldBeTrue()
                it.distanceToStart shouldBe 5
                it.paths shouldHaveSize 3
                it.paths.forAll { it shouldHaveSize 6 }
            }
            backward should {
                it.success.shouldBeTrue()
                it.distanceToStart shouldBe 5
                it.paths.forAll { it shouldHaveSize 6 }
            }
        }
    }

})