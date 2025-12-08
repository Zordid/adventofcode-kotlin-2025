import utils.*
import utils.dim3d.*
import kotlin.collections.plus
import kotlin.math.sqrt

class Day08 : Day(8, 2025, "Playground") {

    val boxes = input.map { it.extractAllIntegers().let { (x, y, z) -> Point3D(x, y, z) } }

    fun Point3D.distanceTo(other: Point3D): Double =
        sqrt(((x - other.x).toDouble() * (x - other.x) + (y - other.y).toDouble() * (y - other.y) + (z - other.z).toDouble() * (z - other.z)))

    override fun part1(): Long {
        val wires = if (testInput) 10 else 1000

        var circuits = boxes.map { setOf(it) }.toSet()
        val byDistance = boxes.combinations(2).sortedBy { (a, b) -> a.distanceTo(b) }
        byDistance.take(wires).forEach { (a, b) ->
            circuits = circuits.joinJunctionBoxes(a, b)
        }
        return circuits.map { it.size }.sortedDescending().take(3).product()
    }

    override fun part2(): Long {
        var circuits = boxes.map { setOf(it) }.toSet()
        val byDistance = boxes.combinations(2).sortedBy { (a, b) -> a.distanceTo(b) }

        for ((a, b) in byDistance) {
            circuits = circuits.joinJunctionBoxes(a, b)
            if (circuits.size == 1) {
                alog { "Last two boxes to join are: $a & $b" }
                return a.x.toLong() * b.x
            }
        }
        error("No solution found")
    }

    fun Set<Set<Point3D>>.joinJunctionBoxes(a: Point3D, b: Point3D): Set<Set<Point3D>> {
        log { "Joining $a & $b with distance ${a.distanceTo(b)}" }
        val ca = single { a in it }
        val cb = single { b in it }
        return if (ca != cb)
            minusElement(ca).minusElement(cb).plusElement(ca + cb)
        else
            this
    }

}

fun main() {
    solve<Day08> {
        """
            162,817,812
            57,618,57
            906,360,560
            592,479,940
            352,342,300
            466,668,158
            542,29,236
            431,825,988
            739,650,466
            52,470,668
            216,146,977
            819,987,18
            117,168,530
            805,96,715
            346,949,466
            970,615,88
            941,993,340
            862,61,35
            984,92,344
            425,690,689
        """.trimIndent() part1 40 part2 25272
    }
}