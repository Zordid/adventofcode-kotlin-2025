@file:Suppress("unused")

package utils

import kotlin.math.*

typealias Point = Pair<Int, Int>

val Point.x: Int get() = first
val Point.y: Int get() = second

data class Area(val upperLeft: Point, val lowerRight: Point) {

    constructor(size: Int) : this(origin, (size - 1) to (size - 1))

    fun Area.isEmpty() = size == 0
    fun Area.isNotEmpty() = !isEmpty()

    val width: Int get() = (lowerRight.x - upperLeft.x + 1).coerceAtLeast(0)
    val height: Int get() = (lowerRight.y - upperLeft.y + 1).coerceAtLeast(0)
    val size: Int get() = width * height

    val upperRight: Point get() = lowerRight.x to upperLeft.y
    val lowerLeft: Point get() = upperLeft.x to lowerRight.y
    val left: Int get() = upperLeft.x
    val right: Int get() = lowerRight.x
    val top: Int get() = upperLeft.y
    val bottom: Int get() = lowerRight.y
    val topToBottom: IntRange get() = top..bottom
    val leftToRight: IntRange get() = left..right

    fun isValid(): Boolean = left <= right && top <= bottom
    fun fixed(): Area = if (isValid()) this else of(upperLeft, lowerRight)

    operator fun plus(other: Area) =
        listOf(upperLeft, lowerRight, other.upperLeft, other.lowerRight).boundingArea()!!

    operator fun contains(p: Point) = p.x in leftToRight && p.y in topToBottom

    operator fun iterator(): Iterator<Point> = iterator { forEach { yield(it) } }

    fun isBorder(p: Point) = p in this && p.y == top || p.y == bottom || p.x == left || p.x == right

    inline fun forEach(f: (p: Point) -> Unit) {
        for (y in topToBottom) {
            for (x in leftToRight) {
                f(x to y)
            }
        }
    }

    inline fun forEachReversed(f: (p: Point) -> Unit) {
        for (y in topToBottom.reversed()) {
            for (x in leftToRight.reversed()) {
                f(x to y)
            }
        }
    }

    inline fun forBorder(f: (p: Point) -> Unit) {
        for (y in topToBottom) {
            when (y) {
                top, bottom -> for (x in leftToRight) {
                    f(x to y)
                }

                else -> {
                    f(left to y)
                    f(right to y)
                }
            }
        }
    }

    companion object {
        val EMPTY = Area(origin, -1 to -1)

        fun of(a: Point, b: Point) =
            Area(min(a.x, b.x) to min(a.y, b.y), max(a.x, b.x) to max(a.y, b.y))
    }
}

infix fun Point.areaTo(end: Point) = Area(this, end)

val Point.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue

infix fun Point.manhattanDistanceTo(other: Point) = (this - other).manhattanDistance

val Point.right: Point get() = x + 1 to y
fun Point.right(steps: Int = 1) = x + steps to y
val Point.left: Point get() = x - 1 to y
fun Point.left(steps: Int = 1) = x - steps to y
val Point.up: Point get() = x to y - 1
fun Point.up(steps: Int = 1) = x to y - steps
val Point.down: Point get() = x to y + 1
fun Point.down(steps: Int = 1) = x to y + steps

fun Point.walkWhile(direction: Direction, steps: Int = 1, predicate: (Point) -> Boolean): Point {
    var current = this
    var next = neighbor(direction, steps)
    while (predicate(next)) {
        current = next
        next = current.neighbor(direction, steps)
    }
    return current
}

fun Point.rightWhile(steps: Int = 1, predicate: (Point) -> Boolean) = walkWhile(Direction4.RIGHT, steps, predicate)
fun Point.leftWhile(steps: Int = 1, predicate: (Point) -> Boolean) = walkWhile(Direction4.LEFT, steps, predicate)

fun Point.neighbor(direction: Direction, steps: Int = 1) = this + (direction.vector * steps)

infix fun Point.isDirectNeighborOf(other: Point): Boolean =
    (this - other).manhattanDistance == 1

/**
 * calculates the list of the four direct neighbors of the point.
 */
fun Point.directNeighbors(): List<Point> = Direction4.allVectors.map { this + it }

/**
 * calculates the list of the four direct neighbors of the point, but removes the ones outside the given [area].
 */
fun Point.directNeighbors(area: Area): List<Point> =
    Direction4.allVectors.mapNotNull { (this + it).takeIf { it in area } }

/**
 * calculates the list of the eight direct neighbors of the point.
 */
fun Point.surroundingNeighbors(): List<Point> = Direction8.allVectors.map { this + it }

/**
 * calculates the list of the eight direct neighbors of the point, but removes the ones outside the given [area].
 */
fun Point.surroundingNeighbors(area: Area): List<Point> =
    Direction8.allVectors.mapNotNull { (this + it).takeIf { it in area } }

val origin: Point = 0 to 0
val emptyArea: Area = Area.EMPTY

infix operator fun Point.plus(other: Point): Point = x + other.x to y + other.y
infix operator fun Point.minus(other: Point): Point = x - other.x to y - other.y
infix operator fun Point.times(factor: Int): Point = when (factor) {
    0 -> origin
    1 -> this
    else -> x * factor to y * factor
}

infix operator fun Point.div(factor: Int) = when (factor) {
    1 -> this
    else -> x / factor to y / factor
}

infix operator fun Point.div(factor: Point) = when (factor) {
    1 to 1 -> this
    else -> x / factor.x to y / factor.y
}

infix operator fun Point.rem(factor: Int): Point = x % factor to y % factor
infix operator fun Point.rem(factor: Point): Point = x % factor.x to y % factor.y
infix fun Point.mod(factor: Point): Point = x.mod(factor.x) to y.mod(factor.y)
infix fun Point.mod(factor: Int): Point = x.mod(factor) to y.mod(factor)

operator fun Point.unaryMinus(): Point = -x to -y

val Point.length: Double get() = sqrt(x.toDouble() * x + y.toDouble() * y)
val Point.absoluteValue: Point get() = x.absoluteValue to y.absoluteValue
val Point.sign: Point get() = x.sign to y.sign

fun Point.rotateLeft90(times: Int = 1): Point = when (times.mod(4)) {
    1 -> y to -x
    2 -> -x to -y
    3 -> -y to x
    else -> this
}

fun Point.rotateRight90(times: Int = 1): Point = when (times.mod(4)) {
    1 -> -y to x
    2 -> -x to -y
    3 -> y to -x
    else -> this
}

//operator fun Point.compareTo(other: Point): Int =
//    if (y == other.y) x.compareTo(other.x) else y.compareTo(other.y)

fun Point.toArea(): Area = Area(this, this)

operator fun Point.rangeTo(other: Point): Sequence<Point> = when (other) {
    this -> sequenceOf(this)
    else -> sequence {
        val d = Direction8.ofVector(this@rangeTo, other) ?: error("not a usable direction vector")
        var p = this@rangeTo
        while (p != other) {
            yield(p)
            p += d
        }
        yield(other)
    }
}


fun Area.grow(by: Int = 1): Area =
    Area(upperLeft + Direction8.NORTHWEST * by, lowerRight + Direction8.SOUTHEAST * by)

fun Area.growWidth(by: Int = 1): Area =
    Area(upperLeft + Direction8.WEST * by, lowerRight + Direction8.EAST * by)

fun Area.growHeight(by: Int = 1): Area =
    Area(upperLeft + Direction8.NORTH * by, lowerRight + Direction8.SOUTH * by)

fun Area.growTop(by: Int = 1) = Area(upperLeft + Direction8.NORTH * by, lowerRight)
fun Area.growLeft(by: Int = 1) = Area(upperLeft + Direction8.WEST * by, lowerRight)
fun Area.growRight(by: Int = 1) = Area(upperLeft, lowerRight + Direction8.EAST * by)
fun Area.growBottom(by: Int = 1) = Area(upperLeft, lowerRight + Direction8.SOUTH * by)
fun Area.shrink(by: Int = 1) = Area(upperLeft + Direction8.SOUTHEAST * by, lowerRight + Direction8.NORTHWEST * by)

fun Area.scale(by: Int): Area = Area(upperLeft, upperLeft + (width * by - 1 to height * by - 1))

fun allPointsInArea(from: Point, to: Point): Sequence<Point> =
    Area.of(from, to).allPoints()

fun Iterable<Point>.withIn(area: Area) = filter { it in area }
fun Sequence<Point>.withIn(area: Area) = filter { it in area }

private val areaRegex = ".*?(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+).*".toRegex()

fun areaFromString(s: String): Area? =
    areaRegex.matchEntire(s)?.groupValues
        ?.let { Area(it[1].toInt() to it[2].toInt(), it[3].toInt() to it[4].toInt()) }

fun Area.allPoints(): Sequence<Point> = sequence { forEach { yield(it) } }
fun Area.allPointsReversed(): Sequence<Point> = sequence { forEachReversed { yield(it) } }
fun Area.border(): Sequence<Point> = sequence { forBorder { yield(it) } }
fun Area.corners(): Sequence<Point> =
    if (isEmpty())
        emptySequence()
    else
        listOf(upperLeft, upperRight, lowerRight, lowerLeft).distinct().asSequence()


operator fun Area.plus(amount: Int) = grow(by = amount)
operator fun Area.minus(amount: Int) = shrink(by = amount)


fun Area.overlaps(other: Area): Boolean =
    max(left, other.left) <= min(right, other.right) && max(top, other.top) <= min(bottom, other.bottom)

fun Iterable<Point>.boundingArea(): Area? {
    val (minX, maxX) = minMaxByOrNull { it.x } ?: return null
    val (minY, maxY) = minMaxByOrNull { it.y }!!
    return Area(minX.x to minY.y, maxX.x to maxY.y)
}

/**
 * Turns a list of exactly 2 Int values into a Point, useful for map(::asPoint)
 */
fun asPoint(l: List<Int>): Point {
    require(l.size == 2) { "List should exactly contain 2 values for x and y, but has ${l.size} values!" }
    return Point(l[0], l[1])
}

interface Direction {
    val name: String
    val right: Direction
    val left: Direction
    val opposite: Direction
    val vector: Point
    val symbol: Char
}

operator fun Direction.times(n: Int): Point = vector * n
operator fun Point.plus(direction: Direction): Point = this + direction.vector
operator fun Point.minus(direction: Direction): Point = this - direction.vector

const val LEFT_ARROW = '\u2190'
const val UP_ARROW = '\u2191'
const val RIGHT_ARROW = '\u2192'
const val DOWN_ARROW = '\u2193'
const val NW_ARROW = '\u2196'
const val NE_ARROW = '\u2197'
const val SE_ARROW = '\u2198'
const val SW_ARROW = '\u2199'

enum class Direction4(override val vector: Point, override val symbol: Char) : Direction {
    NORTH(0 to -1, UP_ARROW),
    EAST(1 to 0, RIGHT_ARROW),
    SOUTH(0 to 1, DOWN_ARROW),
    WEST(-1 to 0, LEFT_ARROW);

    override val right by lazy { entries[(ordinal + 1).mod(entries.size)] }
    override val left by lazy { entries[(ordinal - 1).mod(entries.size)] }
    override val opposite by lazy { entries[(ordinal + entries.size / 2).mod(entries.size)] }

    companion object {
        val all = entries
        val allVectors = all.map { it.vector }

        val UP = NORTH
        val RIGHT = EAST
        val DOWN = SOUTH
        val LEFT = WEST

        fun ofVector(p1: Point, p2: Point): Direction4? = ofVector(p2 - p1)

        fun ofVector(v: Point) =
            all.firstOrNull { it.vector.x == v.x.sign && it.vector.y == v.y.sign }

        fun ofSymbol(c: Char, nesw: String = "^>v<"): Direction4? =
            nesw.indexOf(c).takeUnless { it == -1 }?.let { entries[it] }

        fun interpret(s: Any): Direction4 = interpretOrNull(s) ?: error("What direction should '$s' indicate?")

        fun interpretOrNull(s: Any): Direction4? = when ("$s".uppercase()) {
            NORTH.name, "N", "UP", "U", "^" -> NORTH
            EAST.name, "E", "RIGHT", "R", ">" -> EAST
            SOUTH.name, "S", "DOWN", "D", "V" -> SOUTH
            WEST.name, "W", "LEFT", "L", "<" -> WEST
            else -> null
        }

        inline fun forEach(action: (Direction) -> Unit) {
            all.forEach(action)
        }

        inline fun <T> map(f: (p: Direction4) -> T) = all.map(f)
    }
}

enum class Direction8(override val vector: Point, override val symbol: Char) : Direction {
    NORTH(0 to -1, UP_ARROW),
    NORTHEAST(1 to -1, NE_ARROW),
    EAST(1 to 0, RIGHT_ARROW),
    SOUTHEAST(1 to 1, SE_ARROW),
    SOUTH(0 to 1, DOWN_ARROW),
    SOUTHWEST(-1 to 1, SW_ARROW),
    WEST(-1 to 0, LEFT_ARROW),
    NORTHWEST(-1 to -1, NW_ARROW);

    override val right by lazy { entries[(ordinal + 1).mod(entries.size)] }
    override val left by lazy { entries[(ordinal - 1).mod(entries.size)] }
    override val opposite by lazy { entries[(ordinal + entries.size / 2).mod(entries.size)] }

    companion object {
        val all = entries
        val allVectors = all.map { it.vector }

        val UP = NORTH
        val UP_LEFT = NORTHWEST
        val UP_RIGHT = NORTHEAST
        val RIGHT = EAST
        val DOWN = SOUTH
        val DOWN_LEFT = SOUTHWEST
        val DOWN_RIGHT = SOUTHEAST
        val LEFT = WEST

        fun ofVector(p1: Point, p2: Point) = ofVector(p2 - p1)

        fun ofVector(v: Point) =
            all.firstOrNull { it.vector.x == v.x.sign && it.vector.y == v.y.sign }

        inline fun forEach(action: (Direction) -> Unit) {
            all.forEach(action)
        }

        inline fun <T> map(f: (p: Direction8) -> T) = all.map(f)
    }
}
