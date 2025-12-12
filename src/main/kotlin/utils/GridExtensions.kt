@file:Suppress("unused")

package utils

/**
 * An alias for looking at `List<List<T>>` as a [Grid].
 *
 * Important: Grids are always treated as densely filled. If a Grid has rows with fewer elements, use
 * [fixed] helper function to fix this issue.
 */
typealias Grid<T> = List<List<T>>
typealias MutableGrid<T> = MutableList<MutableList<T>>

typealias MapGrid<T> = Map<Point, T>
typealias MutableMapGrid<T> = MutableMap<Point, T>

fun String.toGrid(): Grid<Char> = split("\n").toGrid()
fun List<String>.toGrid(): Grid<Char> = map { it.toList() }.asGrid()
fun <T> List<List<T>>.asGrid(): Grid<T> = this

val Grid<*>.width: Int get() = firstOrNull()?.size ?: 0
val Grid<*>.height: Int get() = size
val Grid<*>.area: Area get() = Area(origin, lastPoint)
val Grid<*>.maxArea: Area get() = Area(origin, (maxOf { it.lastIndex } to lastIndex))
val Grid<*>.colIndices: IntRange get() = firstOrNull()?.indices ?: IntRange.EMPTY
val Grid<*>.rowIndices: IntRange get() = indices

val Iterable<Point>.area: Area get() = areaOrNull ?: error("No points given")
val Iterable<Point>.areaOrNull: Area? get() = boundingArea()
val MapGrid<*>.area: Area get() = areaOrNull ?: error("No points given in Map")
val MapGrid<*>.areaOrNull: Area? get() = keys.boundingArea()

/**
 * The last (bottom right) point in this [Grid] or `-1 to -1` for an empty Grid.
 */
val Grid<*>.lastPoint: Point get() = (firstOrNull()?.lastIndex ?: -1) to lastIndex

/**
 * Checks whether the given coordinate [p] is within the bounds of this [Grid]
 */
operator fun Grid<*>.contains(p: Point) = p.y in indices && p.x in 0 until width

operator fun <T> Grid<T>.iterator(): Iterator<Pair<Point, T>> = iterator {
    for (row in indices) {
        val r = get(row)
        for (col in r.indices) {
            yield((col to row) to r[col])
        }
    }
}

/**
 * Creates a new [Grid] with the specified [area], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> Grid(area: Area, init: (Point) -> T): Grid<T> = MutableGrid(area, init)

/**
 * Creates a new [Grid] with the specified [width] and [height], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> Grid(width: Int, height: Int, init: (Point) -> T): Grid<T> = MutableGrid(width, height, init)

/**
 * Creates a new [MutableGrid] with the specified [area], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> MutableGrid(area: Area, init: (Point) -> T): MutableGrid<T> {
    area.requireOrigin()
    return MutableGrid(area.width, area.height, init)
}

/**
 * Creates a new [MutableGrid] with the specified [width] and [height], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> MutableGrid(width: Int, height: Int, init: (Point) -> T): MutableGrid<T> {
    require(width >= 0 && height >= 0) { "Given area $width x $height must not be negative" }
    return MutableList(height) { y ->
        MutableList(width) { x -> init(x to y) }
    }
}

fun <T> Grid(map: Map<Point, T>, default: T): Grid<T> = MutableGrid(map, default)
fun <T> MutableGrid(map: Map<Point, T>, default: T): MutableGrid<T> = MutableGrid(map) { default }

inline fun <T> Grid(map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> =
    MutableGrid(map, default)

inline fun <T> MutableGrid(map: Map<Point, T>, crossinline default: (Point) -> T): MutableGrid<T> {
    val (first, last) = map.keys.boundingArea() ?: return mutableListOf()
    require(first.x >= 0 && first.y >= 0) {
        "Given Map contains negative points. Maybe construct using Grid(width, height) { custom translation }"
    }
    val area = origin areaTo last
    return MutableGrid(area, map, default)
}

inline fun <T> Grid(area: Area, map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> =
    MutableGrid(area, map, default)

inline fun <T> MutableGrid(area: Area, map: Map<Point, T>, crossinline default: (Point) -> T): MutableGrid<T> {
    area.requireOrigin()
    return MutableGrid(area.width, area.height, map, default)
}

inline fun <T> Grid(width: Int, height: Int, map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> =
    MutableGrid(width, height, map, default)

inline fun <T> MutableGrid(
    width: Int,
    height: Int,
    map: Map<Point, T>,
    crossinline default: (Point) -> T,
): MutableGrid<T> =
    MutableGrid(width, height) { p -> map.getOrElse(p) { default(p) } }

@PublishedApi
internal fun Area.requireOrigin() =
    require(upperLeft == origin) { "Area for grid must start at origin, but $this was given." }

/**
 * Returns a new [MutableGrid] filled with all elements of this Grid.
 */
fun <T> Grid<T>.toMutableGrid(): MutableGrid<T> = MutableList(size) { this[it].toMutableList() }

/**
 * Fixes missing elements in a [Grid] by filling in `null`.
 * @return a completely uniform n x m Grid
 */
fun <T> Grid<T>.fixedWithNull(): Grid<T?> = fixed(null)

/**
 * Fixes missing elements in a [Grid] by filling in [default].
 * @return a completely uniform n x m Grid
 */
fun <T> Grid<T>.fixed(default: T): Grid<T> {
    val (min, max) = minMaxWidths()
    if (min == max) return this
    return map { row ->
        row.takeIf { row.size == max } ?: List(max) { idx -> if (idx <= row.lastIndex) row[idx] else default }
    }
}

fun Grid<*>.isRegular() =
    minMaxWidths().let { it.min == it.max }

@JvmName("isRegularStringGrid")
fun List<String>.isRegular() =
    minMaxWidths().let { it.min == it.max }

/**
 * Checks the given [Grid] for irregularities, i.e., rows that are of different length.
 */
fun <T> Grid<T>.requireIsRegular(): Grid<T> = also {
    require(isRegular()) {
        val maxWidth = maxOf { it.size }
        "Grid is NOT regular. Lines ${indices.filter { this[it].size < maxWidth }} are too short."
    }
}

@JvmName("requireIsRegularStringGrid")
fun List<String>.requireIsRegular(): List<String> = also {
    require(isRegular()) {
        val maxWidth = maxOf { it.length }
        "Grid is NOT regular. Lines ${indices.filter { this[it].length < maxWidth }} are too short."
    }
}

/**
 * Returns a sequence of all position coordinates.
 */
fun Grid<*>.allPoints(): Sequence<Point> = sequence {
    forArea { yield(it) }
}

/**
 * Returns a sequence of all position/element pairs.
 */
fun <T> Grid<T>.allPointsAndValues(): Sequence<Pair<Point, T>> = sequence {
    forArea {
        yield(it to get(it))
    }
}

/**
 * Returns the first occurrences index coordinates or null if no such element can be found.
 */
fun <T> Grid<T>.indexOfOrNull(e: T): Point? = search(e).firstOrNull()

/**
 * Searches the grid from the top-left point left to right, top to bottom for matching predicate.
 */
inline fun <T> Grid<T>.search(crossinline predicate: (T) -> Boolean): Sequence<Point> =
    area.allPoints().filter { predicate(this[it]) }

/**
 * Searches the grid from the top-left point left to right, top to bottom for matching elements.
 */
fun <T> Grid<T>.search(vararg elements: T): Sequence<Point> =
    search { it in elements }

fun Grid<*>.indices(): Sequence<Point> = sequence {
    for (y in this@indices.indices) {
        for (x in this@indices[y].indices)
            yield(x to y)
    }
}

inline fun <T> Grid<T>.forAreaIndexed(f: (p: Point, v: T) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y, this[y][x])
}

inline fun <T> Grid<T>.forArea(f: (p: Point) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y)
}

fun <T> Grid<T>.row(row: Int): List<T> = this[row]
fun <T> Grid<T>.column(col: Int): List<T> = List(height) { row -> this[row][col] }

fun <T> Grid<T>.transposed(): Grid<T> =
    Grid(width = height, height = width) { (x, y) -> this[x][y] }

fun <T> Grid<T>.flippedY(): Grid<T> =
    Grid(width = width, height = height) { (x, y) -> this[height - 1 - y][x] }

fun <T> Grid<T>.flippedX(): Grid<T> =
    Grid(width = width, height = height) { (x, y) -> this[y][width - 1 - x] }

fun <T> Grid<T>.rotate90(): Grid<T> =
    Grid(width = height, height = width) { (x, y) -> this[height - 1 - x][y] }

fun <T> Grid<T>.rotate180(): Grid<T> =
    Grid(width = width, height = height) { (x, y) -> this[height - 1 - y][width - 1 - x] }

fun <T> Grid<T>.rotate270(): Grid<T> =
    Grid(width = height, height = width) { (x, y) -> this[x][width - 1 - y] }

fun <T> Grid<T>.toMapGrid(vararg sparseElements: T): Map<Point, T> =
    toMapGrid { it in sparseElements }

inline fun <T> Grid<T>.toMapGrid(sparsePredicate: (T) -> Boolean): MapGrid<T> =
    buildMap { forAreaIndexed { p, v -> if (!sparsePredicate(v)) put(p, v) } }

fun <T, R> Grid<T>.mapValues(transform: (T) -> R): Grid<R> =
    map { it.map(transform) }

fun <T, R> Grid<T>.mapValuesIndexed(transform: (Point, T) -> R): Grid<R> =
    mapIndexed { y, r -> r.mapIndexed { x, v -> transform(x to y, v) } }

fun <T> Grid<T>.frequencies(): Map<T, Int> = allPointsAndValues().map { it.second }.groupingBy { it }.eachCount()

operator fun <T> Grid<T>.get(v: T): Point =
    requireNotNull(search(v).singleOrNull()) { "Grid does not contain any single value '$v'" }

operator fun <T> Grid<T>.get(p: Point): T =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x]
    else notInGridError(p)

fun <T> Grid<T>.getOrNull(p: Point): T? =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x]
    else null

inline fun <T> Grid<T>.getOrElse(p: Point, default: (Point) -> T): T =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x]
    else default(p)

fun <T> Grid<T>.getOrDefault(p: Point, default: T): T =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x]
    else default

operator fun <T> MutableGrid<T>.set(p: Point, v: T) {
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] = v
    else notInGridError(p)
}

operator fun List<String>.get(p: Point): Char =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x]
    else notInListGridError(p)

fun List<String>.getOrNull(p: Point): Char? =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x]
    else null

fun List<String>.getOrElse(p: Point, default: (Point) -> Char): Char =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x]
    else default(p)

private fun Grid<*>.minMaxWidths(): MinMax<Int> =
    minMaxOfOrNull { it.size } ?: MinMaxResult(0, 0)

@JvmName("minMaxWidthsStringGrid")
private fun List<String>.minMaxWidths(): MinMax<Int> =
    minMaxOfOrNull { it.length } ?: MinMaxResult(0, 0)

private fun Grid<*>.notInGridError(p: Point): Nothing {
    requireIsRegular()
    error("Point $p not in grid of dimensions $width x $height")
}

private fun List<String>.notInListGridError(p: Point): Nothing {
    requireIsRegular()
    error("Point $p not in grid of dimensions ${firstOrNull()?.length ?: 0} x $size")
}

fun <T : Any> Grid<T>.explode(filler: T? = null): Grid<T> =
    MutableGrid(area.scale(3)) { p ->
        val sourcePoint = p / 3
        if (filler == null) this@explode[sourcePoint]
        else if (sourcePoint * 3 + (1 to 1) == p) this@explode[sourcePoint] else filler
    }
