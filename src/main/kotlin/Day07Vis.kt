import utils.*
import java.awt.Color
import kotlin.math.max

class Day07Vis : KPixelGameEngine("Day 7 - Laboratories") {

    private lateinit var splitters: Set<Point>
    private lateinit var area: Area
    private var activeBeams = setOf<Point>()
    private var finished = false

    private var totalSplits = 0
    private var lastBeamCount = 0
    private var offsetX = 0
    private var offsetY = 0
    private val headerHeight = 12

    override fun onCreate() {
        try {
            // Attempt to load from the actual Day07 solution class
            val day = Day07()
            splitters = day.splitters
            area = day.area
            activeBeams = setOf(day.start)
        } catch (e: Exception) {
            println("Falling back to test input due to: ${e.message}")
            // Fallback to the example input if file loading fails
            val inputStr = """
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
            """.trimIndent()
            val lines = inputStr.lines()
            val height = lines.size
            val width = lines.maxOf { it.length }
            area = Area(0 to 0, width - 1 to height - 1)
            splitters = mutableSetOf()
            var start: Point = 0 to 0
            lines.forEachIndexed { y, line ->
                line.forEachIndexed { x, c ->
                    val p = x to y
                    if (c == '^') (splitters as MutableSet).add(p)
                    if (c == 'S') start = p
                }
            }
            activeBeams = setOf(start)
        }

        lastBeamCount = activeBeams.size

        // Layout calculations
        // Ensure minimum width to fit text "Splits: XXXX" and "Beams: XXXX"
        // Approx 20 chars total width needed if side by side, but we put them in corners.
        // "Splits: 1000000" -> ~15 chars -> 120px. 
        // We need a minimum width of around 250px to prevent overlap with large numbers.
        val minWidth = 250
        val gridWidth = area.width
        val gridHeight = area.height

        val displayWidth = max(gridWidth, minWidth)
        val displayHeight = gridHeight + headerHeight

        // Center the grid horizontally
        offsetX = (displayWidth - gridWidth) / 2
        offsetY = headerHeight

        // Calculate scale to fit the window (target ~800px)
        val scale = (800 / displayHeight).coerceAtMost(800 / displayWidth).coerceAtLeast(4)
        construct(displayWidth, displayHeight, scale, scale)

        limitFps = 15 // Slow enough to follow with eyes

        // Initial Draw
        clear(Color.BLACK)

        // Draw Header background and initial stats
        drawStats()

        // Draw initial grid state
        splitters.forEach { drawGrid(it, Color.GREEN) }
        drawGrid(activeBeams.first(), Color.YELLOW)
    }

    private fun drawGrid(p: Point, color: Color) {
        draw(p.x + offsetX, p.y + offsetY, color)
    }

    private fun drawStats() {
        // Clear header area
        fillRect(0, 0, screenWidth, headerHeight, Color.BLACK)

        // Draw Left Stat (Splits)
        val splitsText = "Splits: $totalSplits"
        drawString(2, 2, splitsText, Color.WHITE)

        // Draw Right Stat (Beams)
        val beamsText = "Beams: $lastBeamCount"
        val beamsTextWidth = beamsText.length * 8 // Standard font is 8px wide
        drawString(screenWidth - beamsTextWidth - 2, 2, beamsText, Color.WHITE)

        // Separator line
        drawLine(0, headerHeight - 1, screenWidth, headerHeight - 1, Color.DARK_GRAY)
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        if (elapsedTime < 10000) return
        if (finished) return

        if (activeBeams.isEmpty()) {
            finished = true
            return
        }

        // Fade old beams slightly (optional, but here we just overwrite with trace color)
        activeBeams.forEach { drawGrid(it, Color.DARK_GRAY) }

        // Logic from Day07 Part 1:
        // 1. Move everything down
        // 2. Partition into those that hit a splitter and those that don't
        val (onSplitter, noSplitter) = activeBeams.map { it.down }.partition { it in splitters }

        // Update stats
        totalSplits += onSplitter.size

        // 3. Those on splitter branch left and right (at the same Y level as the splitter)
        // 4. Those not on splitter continue falling
        val nextBeams = (onSplitter.flatMap { listOf(it.left, it.right) } + noSplitter).toSet()

        // Filter beams that left the area
        val nextActiveBeams = nextBeams.filter { it in area }.toSet()

        if (nextActiveBeams.isNotEmpty()) {
            activeBeams = nextActiveBeams
            lastBeamCount = activeBeams.size
        } else {
            activeBeams = emptySet()
            finished = true
        }

        // Update stats display
        drawStats()

        if (activeBeams.isNotEmpty()) {
            // Draw new active beams
            activeBeams.forEach { p ->
                // If it's a splitter location, keep it green, otherwise white beam head
                if (p in splitters) {
                    drawGrid(p, Color.CYAN) // Flash color for hit
                } else {
                    drawGrid(p, Color.WHITE)
                }
            }
            // Redraw splitters that aren't currently being hit so they don't disappear under trails
            splitters.forEach { if (it !in activeBeams) drawGrid(it, Color.GREEN) }
        }
    }
}

fun main() {
    val vis = Day07Vis()
    vis.start()
}