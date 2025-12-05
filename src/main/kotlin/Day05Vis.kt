// ... existing code ...
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.math.max

// Visualization State
enum class State { SHOW_UNSORTED, SORTING, MERGED_WAIT, MERGING, FINISHED }

fun vis(day05: Day05) {
// Assume 'ranges' is available from previous cells
    val ranges = day05.freshIngredientRanges.map { it.first to it.last }
    val unsortedRanges = ranges
// We still keep reference sorted list for speed validation if needed, but we'll sort visually
    val finalSortedRanges = ranges.sortedBy { it.first }

// Backing list for the UI - starts unsorted, gets modified in place
    val displayList = unsortedRanges.toMutableList()

// Global bounds for consistent scaling
    val minVal = ranges.minOf { it.first }
    val maxVal = ranges.maxOf { it.second }
    val rangeSpan = (maxVal - minVal).coerceAtLeast(1)



    var currentState = State.SHOW_UNSORTED
    var message = "Original Ranges (Unsorted)"

// Sorting State Variables
    var sortI = 0 // The boundary of the sorted section (top)
    var sortJ = displayList.size - 1 // The scanning index (moves bottom -> top)

// Merging State Variables
    var mergeIndex = 0
    var mergedList = mutableListOf<Pair<Long, Long>>()

// UI Layout Constants
    val rowHeight = 20
    val padding = 50

    val frame = JFrame("Range Merge Visualization (Animated Sort)")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.setSize(1200, 800)
    frame.layout = BorderLayout()

// --- 1. Top Panel: Scrollable List (Unsorted -> Sorted) ---
    val inputPanel = object : JPanel() {
        init {
            background = Color.WHITE
            isDoubleBuffered = true
        }

        override fun getPreferredSize(): Dimension {
            val listSize = displayList.size
            val h = 50 + (listSize * rowHeight) + 50
            return Dimension(parent?.width ?: 800, h)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val width = width
            val scaleX = (width - 2 * padding).toDouble() / rangeSpan
            fun mapX(value: Long): Int = (padding + (value - minVal) * scaleX).toInt()

            g2d.color = Color.BLACK
            g2d.font = Font("SansSerif", Font.BOLD, 16)
            g2d.drawString(message, 10, 25)

            // Optimization: Culling
            val clip = g.clipBounds ?: Rectangle(0, 0, width, height)
            val startIndex = ((clip.y - 50) / rowHeight).coerceAtLeast(0)
            val endIndex = (((clip.y + clip.height - 50) / rowHeight) + 1).coerceAtMost(displayList.size - 1)

            if (startIndex <= endIndex) {
                for (i in startIndex..endIndex) {
                    val range = displayList[i]
                    val drawY = 50 + i * rowHeight

                    // Color Logic
                    when (currentState) {
                        State.SORTING -> {
                            if (i < sortI) g2d.color = Color(100, 149, 237) // Sorted (Blue)
                            else if (i == sortJ || i == sortJ - 1) g2d.color = Color.ORANGE // Active Compare
                            else g2d.color = Color.LIGHT_GRAY // Unsorted
                        }

                        State.MERGING -> {
                            if (i == mergeIndex) g2d.color = Color.ORANGE // Active Merge
                            else if (i < mergeIndex) g2d.color = Color.LIGHT_GRAY // Processed
                            else g2d.color = Color(100, 149, 237) // Pending
                        }

                        else -> g2d.color = Color(100, 149, 237)
                    }

                    val x1 = mapX(range.first)
                    val x2 = mapX(range.second)
                    val w = (x2 - x1).coerceAtLeast(2)

                    g2d.fillRect(x1, drawY, w, rowHeight - 4)

                    // Highlight border for active items
                    if ((currentState == State.MERGING && i == mergeIndex) ||
                        (currentState == State.SORTING && (i == sortJ || i == sortJ - 1))
                    ) {
                        g2d.color = Color.RED
                        g2d.stroke = BasicStroke(2f)
                        g2d.drawRect(x1 - 1, drawY - 1, w + 2, rowHeight - 2)
                    }
                }
            }
        }
    }

    val scrollPane = JScrollPane(inputPanel)
    scrollPane.verticalScrollBar.unitIncrement = 16
    frame.add(scrollPane, BorderLayout.CENTER)

// --- 2. Bottom Panel: Fixed Merged Result ---
    val mergedPanel = object : JPanel() {
        init {
            preferredSize = Dimension(1200, 120)
            background = Color(245, 245, 245)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val width = width
            val scaleX = (width - 2 * padding).toDouble() / rangeSpan
            fun mapX(value: Long): Int = (padding + (value - minVal) * scaleX).toInt()

            g2d.color = Color.BLACK
            g2d.font = Font("SansSerif", Font.BOLD, 16)
            g2d.drawString("Merged Ranges:", 10, 20)

            val totalSize = mergedList.sumOf { it.second - it.first + 1 }
            g2d.font = Font("Monospaced", Font.PLAIN, 12)
            g2d.drawString("Count: ${mergedList.size}   Total Covered: $totalSize", 10, 40)

            val barY = 60
            val barHeight = 40

            mergedList.forEach { range ->
                g2d.color = Color(50, 205, 50) // Lime Green
                val x1 = mapX(range.first)
                val x2 = mapX(range.second)
                val w = (x2 - x1).coerceAtLeast(2)
                g2d.fillRect(x1, barY, w, barHeight)
                g2d.color = Color(34, 139, 34)
                g2d.stroke = BasicStroke(1f)
                g2d.drawRect(x1, barY, w, barHeight)
            }
        }
    }

    frame.add(mergedPanel, BorderLayout.SOUTH)

// --- 3. Animation Logic ---
// Dynamic speed calculation
    val n = displayList.size
// Sorting complexity is N^2. We want to finish in ~3 seconds (approx 180 ticks at 16ms).
// Ops per tick = (N*N/2) / 180
    val sortOpsTotal = n.toLong() * n / 2
    val sortOpsPerTick = (sortOpsTotal / 180).toInt().coerceIn(1, 50000)

// Merging complexity is N. Finish in ~3 seconds.
    val mergeOpsPerTick = (n / 180).coerceIn(1, 100)

    val timer = Timer(16, null) // ~60 FPS target
    timer.addActionListener {
        var repaintNeeded = false

        when (currentState) {
            State.SHOW_UNSORTED -> {
                if (timer.initialDelay == 0) {
                    currentState = State.SORTING
                    message = "Sorting Ranges (Sinking Sort)..."
                    // Initialize sort variables
                    sortI = 0
                    sortJ = displayList.size - 1
                    repaintNeeded = true
                } else {
                    timer.initialDelay = 0
                }
            }

            State.SORTING -> {
                var ops = 0
                while (ops < sortOpsPerTick && currentState == State.SORTING) {
                    if (sortI < displayList.size - 1) {
                        if (sortJ > sortI) {
                            // Compare adjacent
                            if (displayList[sortJ].first < displayList[sortJ - 1].first) {
                                val tmp = displayList[sortJ]
                                displayList[sortJ] = displayList[sortJ - 1]
                                displayList[sortJ - 1] = tmp
                            }
                            sortJ--
                        } else {
                            // Finished one pass, item at sortI is settled
                            sortI++
                            sortJ = displayList.size - 1
                        }
                        ops++
                    } else {
                        currentState = State.MERGED_WAIT
                        message = "Sorted! Ready to merge."
                        timer.delay = 1000
                        repaintNeeded = true
                    }
                }
                // Scroll to follow the sorted boundary 'sortI'
                if (currentState == State.SORTING) {
                    val targetY = 50 + (sortI * rowHeight)
                    val viewRect = scrollPane.viewport.viewRect
                    if (targetY > viewRect.y + viewRect.height / 2) {
                        val newY = (targetY - viewRect.height / 2).coerceAtLeast(0)
                        scrollPane.viewport.viewPosition = Point(0, newY)
                    }
                }
                repaintNeeded = true
            }

            State.MERGED_WAIT -> {
                currentState = State.MERGING
                message = "Merging..."
                timer.delay = 16 // Back to fast speed
                // Reset scroll to top
                scrollPane.viewport.viewPosition = Point(0, 0)
            }

            State.MERGING -> {
                repeat(mergeOpsPerTick) {
                    if (mergeIndex < displayList.size) {
                        val next = displayList[mergeIndex]
                        val prev = mergedList.lastOrNull()

                        if (prev == null) {
                            mergedList.add(next)
                        } else {
                            when {
                                next.first > prev.second + 1 -> mergedList.add(next)
                                next.second > prev.second -> {
                                    mergedList[mergedList.lastIndex] = prev.first to next.second
                                }
                            }
                        }
                        mergeIndex++
                        repaintNeeded = true
                    } else {
                        currentState = State.FINISHED
                        message = "Finished! Final Count: ${mergedList.size}"
                        timer.stop()
                        repaintNeeded = true
                        return@repeat
                    }
                }

                if (currentState == State.MERGING) {
                    val targetY = 50 + (mergeIndex * rowHeight)
                    val viewRect = scrollPane.viewport.viewRect
                    // Keep active item visible
                    if (targetY > viewRect.y + viewRect.height - 100) {
                        val newY = (targetY - viewRect.height + 100).coerceAtLeast(0)
                        scrollPane.viewport.viewPosition = Point(0, newY)
                    }
                }
            }

            State.FINISHED -> {}
        }

        if (repaintNeeded) {
            inputPanel.revalidate()
            inputPanel.repaint()
            mergedPanel.repaint()
        }
    }

    timer.initialDelay = 1500
    timer.start()

    frame.addWindowListener(object : WindowAdapter() {
        override fun windowClosed(e: WindowEvent?) {
            timer.stop()
        }
    })

    frame.isVisible = true
}

fun main() {
    vis(Day05())
}
