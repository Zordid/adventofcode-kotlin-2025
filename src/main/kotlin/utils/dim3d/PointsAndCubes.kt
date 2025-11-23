package utils.dim3d

import com.github.ajalt.mordant.rendering.TextColors
import utils.areaTo
import utils.minMax
import utils.plot
import utils.transposed
import utils.x
import utils.y
import kotlin.math.absoluteValue
import kotlin.math.sign

typealias Point3D = Triple<Int, Int, Int>
typealias Cube = Pair<Point3D, Point3D>
typealias Matrix3D = List<Point3D>

val Point3D.x: Int get() = first
val Point3D.y: Int get() = second
val Point3D.z: Int get() = third

operator fun Point3D.unaryMinus() = Point3D(-x, -y, -z)

operator fun Point3D.plus(other: Point3D) =
    Point3D(x + other.x, y + other.y, z + other.z)

operator fun Point3D.minus(other: Point3D) =
    Point3D(x - other.x, y - other.y, z - other.z)

operator fun Point3D.times(n: Int) =
    Point3D(x * n, y * n, z * n)

operator fun Point3D.div(n: Int) =
    Point3D(x / n, y / n, z / n)

infix fun Point3D.x(other: Point3D): Point3D = Point3D(
    second * other.third - third * other.second,
    third * other.first - first * other.third,
    first * other.second - second * other.first
)

val Point3D.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue + z.absoluteValue

infix fun Point3D.manhattanDistanceTo(other: Point3D) =
    (x - other.x).absoluteValue + (y - other.y).absoluteValue + (z - other.z).absoluteValue

val Point3D.sign: Point3D get() = Triple(x.sign, y.sign, z.sign)

fun Point3D.toList() = listOf(x, y, z)

fun Iterable<Int>.asPoint3D(): Point3D {
    val l = (this as? List<Int>) ?: toList()
    require(l.size == 3) { "Should exactly contain 3 values for x,y and z, but has ${l.size} values!" }
    return Point3D(l[0], l[1], l[2])
}

fun Iterable<Point3D>.boundingCube(): Cube {
    val xr = asSequence().map { it.x }.minMax()
    val yr = asSequence().map { it.y }.minMax()
    val zr = asSequence().map { it.z }.minMax()
    return Cube(Point3D(xr.min, yr.min, zr.min), Point3D(xr.max, yr.max, zr.max))
}

fun Iterable<Point3D>.allOrientations() = sequence {
    var copy = this@allOrientations.toList()
    repeat(2) {
        repeat(3) {
            repeat(4) {
                yield(copy)
                copy = copy.rotateY(1)
            }
            copy = copy.rotateX(1)
        }
        copy = copy.rotateZ(2)
    }
}.take(24)

operator fun Cube.contains(p: Point3D) =
    p.x in first.x..second.x && p.y in first.y..second.y && p.z in first.z..second.z

inline fun Cube.forEach(block: (Point3D) -> Unit) {
    for (z in first.z..second.z)
        for (y in first.y..second.y)
            for (x in first.x..second.x)
                block(Point3D(x, y, z))
}

fun Cube.allPoints(): Sequence<Point3D> = sequence { forEach { yield(it) } }

val origin3D = Point3D(0, 0, 0)
val unitVecX = Point3D(1, 0, 0)
val unitVecY = Point3D(0, 1, 0)
val unitVecZ = Point3D(0, 0, 1)

private val neighbors = listOf(unitVecZ, -unitVecZ, unitVecY, -unitVecY, unitVecX, -unitVecX)

fun Point3D.directNeighbors(): List<Point3D> = neighbors.map { this + it }
fun Point3D.directNeighbors(cube: Cube): List<Point3D> = neighbors.mapNotNull { (this + it).takeIf { it in cube } }

@Suppress("UnusedUnaryOperator")
fun Point3D.surroundingNeighbors(): List<Point3D> = buildList {
    for (dz in -1..+1)
        for (dy in -1..+1)
            for (dx in -1..+1)
                if (dz != 0 || dy != 0 || dx != 0)
                    add(this@surroundingNeighbors + Point3D(dx, dy, dz))
}

@Suppress("UnusedUnaryOperator")
fun Point3D.surroundingNeighbors(cube: Cube): List<Point3D> = buildList {
    for (dz in -1..+1)
        for (dy in -1..+1)
            for (dx in -1..+1)
                if (dz != 0 || dy != 0 || dx != 0) {
                    val p = this@surroundingNeighbors + Point3D(dx, dy, dz)
                    if (p in cube) add(p)
                }
}

/**
 * Rotate the given point around an arbitrary vector by 90 degrees.
 * Important: the given axis vector must be a unit vector or else the length will be messed up.
 *
 * From: https://stackoverflow.com/a/6721649/273456
 */
infix fun Point3D.rotateAround(u: Point3D): Point3D =
    listOf(
        Point3D(
            COS_90 + u.x * u.x * (1 - COS_90),
            u.x * u.y * (1 - COS_90) - u.z * SIN_90,
            u.x * u.z * (1 - COS_90) + u.y * SIN_90
        ),
        Point3D(
            u.y * u.x * (1 - COS_90) + u.z * SIN_90,
            COS_90 + u.y * u.y * (1 - COS_90),
            u.y * u.z * (1 - COS_90) - u.x * SIN_90
        ),
        Point3D(
            u.z * u.x * (1 - COS_90) - u.y * SIN_90,
            u.z * u.y * (1 - COS_90) + u.x * SIN_90,
            COS_90 + u.z * u.z * (1 - COS_90),
        ),
    ) * this


fun Iterable<Point3D>.rotateX(times: Int): List<Point3D> = mapTo(mutableListOf()) { it.rotateX(times) }
fun Iterable<Point3D>.rotateY(times: Int): List<Point3D> = mapTo(mutableListOf()) { it.rotateY(times) }
fun Iterable<Point3D>.rotateZ(times: Int): List<Point3D> = mapTo(mutableListOf()) { it.rotateZ(times) }

fun Point3D.rotateX(times: Int = 1) = rotXM[times.mod(4)] * this
fun Point3D.rotateY(times: Int = 1) = rotYM[times.mod(4)] * this
fun Point3D.rotateZ(times: Int = 1) = rotZM[times.mod(4)] * this

operator fun Matrix3D.times(p: Point3D): Point3D =
    Point3D(
        p.x * this[0].x + p.y * this[0].y + p.z * this[0].z,
        p.x * this[1].x + p.y * this[1].y + p.z * this[1].z,
        p.x * this[2].x + p.y * this[2].y + p.z * this[2].z,
    )

private const val COS_0 = 1
private const val COS_180 = -1
private const val COS_90 = 0
private const val COS_270 = 0
private const val SIN_0 = 0
private const val SIN_90 = 1
private const val SIN_180 = 0
private const val SIN_270 = -1

val identityMatrix3D: Matrix3D = listOf(
    Point3D(1, 0, 0),
    Point3D(0, 1, 0),
    Point3D(0, 0, 1),
)

val rotXM: List<Matrix3D> = listOf(
    identityMatrix3D,
    listOf(
        Point3D(1, 0, 0),
        Point3D(0, COS_90, -SIN_90),
        Point3D(0, SIN_90, COS_90),
    ),
    listOf(
        Point3D(1, 0, 0),
        Point3D(0, COS_180, -SIN_180),
        Point3D(0, SIN_180, COS_180),
    ),
    listOf(
        Point3D(1, 0, 0),
        Point3D(0, COS_270, -SIN_270),
        Point3D(0, SIN_270, COS_270),
    ),
)

val rotYM: List<Matrix3D> = listOf(
    identityMatrix3D,
    listOf(
        Point3D(COS_90, 0, SIN_90),
        Point3D(0, 1, 0),
        Point3D(-SIN_90, 0, COS_90),
    ),
    listOf(
        Point3D(COS_180, 0, SIN_180),
        Point3D(0, 1, 0),
        Point3D(-SIN_180, 0, COS_180),
    ),
    listOf(
        Point3D(COS_270, 0, SIN_270),
        Point3D(0, 1, 0),
        Point3D(-SIN_270, 0, COS_270),
    ),
)

val rotZM: List<Matrix3D> = listOf(
    identityMatrix3D,
    listOf(
        Point3D(COS_90, -SIN_90, 0),
        Point3D(SIN_90, COS_90, 0),
        Point3D(0, 0, 1),
    ),
    listOf(
        Point3D(COS_180, -SIN_180, 0),
        Point3D(SIN_180, COS_180, 0),
        Point3D(0, 0, 1),
    ),
    listOf(
        Point3D(COS_270, -SIN_270, 0),
        Point3D(SIN_270, COS_270, 0),
        Point3D(0, 0, 1),
    ),
)

fun <T:Any> Map<Point3D, T>.plot(
    bounds: Cube = keys.boundingCube(),
    filler: (Point3D) -> String = { " " },
    transform: (Point3D, T) -> Any = { _, value -> value },
): String {
    val planeArea = bounds.first.x to bounds.first.y areaTo (bounds.second.x to bounds.second.y)
    val planes = (bounds.first.z .. bounds.second.z).map { z ->
        val plane = this.entries.filter { it.key.z == z }.associate { (it.key.x to it.key.y) to it.value }
        plane.plot(planeArea, filler={ p -> filler(Point3D(p.x, p.y, z))}) { p, v ->
            transform(Point3D(p.x, p.y, z), v)
        }.lines().let {
            val width = planeArea.width + 6
            listOf(TextColors.yellow(" z = $z".padEnd(width))) + it.map{ "  $it  "}.dropLast(1)
        }
    }
    return planes.transposed().joinToString("\n") { it.joinToString("|", prefix = "|", postfix = "|") }
}
