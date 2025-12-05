// ... existing code ...
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.math.max
import kotlin.math.ceil

enum class State { SHOW_UNSORTED, SORTING, MERGED_WAIT, MERGING, FINISHED }

data class FallingRange(
    val range: Pair<Long, Long>,
    var currentY: Double,
    val targetY: Double,
    val startX: Int,
    val width: Int,
    val color: Color
)

fun visualize(inputRanges: List<LongRange>) {
    val ranges = inputRanges.map { it.first to it.last }
    val unsortedRanges = ranges
    val displayList = unsortedRanges.toMutableList()

    val minVal = ranges.minOf { it.first }
    val maxVal = ranges.maxOf { it.second }
    val rangeSpan = (maxVal - minVal).coerceAtLeast(1)

    var currentState = State.SHOW_UNSORTED
    var message = "Original Ranges (Unsorted)"

    var sortI = 0
    var sortJ = displayList.size - 1

    var mergeIndex = 0
    var logicalMergedList = mutableListOf<Pair<Long, Long>>()
    var visualMergedList = mutableListOf<Pair<Long, Long>>()
    val fallingItems = mutableListOf<FallingRange>()

    // Speed Control
    // 1 = Slow, 100 = Fast
    var speedFactor = 5

    val frame = JFrame("Range Merge Visualization (Gravity)")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.setSize(1200, 800)

    val layeredPane = JLayeredPane()
    frame.contentPane.add(layeredPane, BorderLayout.CENTER)

    // Layout constants
    val topPanelY = 40
    val topPanelH = 550
    val bottomPanelY = 600
    val bottomPanelH = 150
    val headerH = 50
    val padding = 50

    // --- Control Panel ---
    val controlPanel = JPanel(FlowLayout(FlowLayout.LEFT))
    val restartButton = JButton("Restart")

    val speedLabel = JLabel("Speed: $speedFactor")
    val speedSlider = JSlider(JSlider.HORIZONTAL, 1, 100, speedFactor)
    speedSlider.preferredSize = Dimension(200, 30)
    speedSlider.addChangeListener {
        speedFactor = speedSlider.value
        speedLabel.text = "Speed: $speedFactor"
    }

    controlPanel.add(restartButton)
    controlPanel.add(Box.createHorizontalStrut(20))
    controlPanel.add(JLabel("Slow"))
    controlPanel.add(speedSlider)
    controlPanel.add(JLabel("Fast"))
    controlPanel.add(Box.createHorizontalStrut(10))
    controlPanel.add(speedLabel)

    controlPanel.setBounds(0, 0, 1200, 40)
    layeredPane.add(controlPanel, 300 as Integer)

    // --- 1. Input Panel ---
    val inputPanel = object : JPanel() {
        init {
            background = Color.WHITE
            isDoubleBuffered = true
            setBounds(0, topPanelY, 1200, topPanelH)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val width = width
            val availHeight = (height - headerH - 10).coerceAtLeast(1)
            val count = displayList.size
            val rowH = if (count > 0) availHeight.toDouble() / count else 10.0
            val scaleX = (width - 2 * padding).toDouble() / rangeSpan
            fun mapX(value: Long): Int = (padding + (value - minVal) * scaleX).toInt()

            g2d.color = Color.BLACK
            g2d.font = Font("SansSerif", Font.BOLD, 16)
            g2d.drawString(message, 10, 25)

            for (i in displayList.indices) {
                val range = displayList[i]
                val yStart = headerH + (i * rowH).toInt()
                val h = ((i + 1) * rowH - i * rowH).toInt().coerceAtLeast(1)
                val x1 = mapX(range.first)
                val x2 = mapX(range.second)
                val w = (x2 - x1).coerceAtLeast(2)

                when (currentState) {
                    State.SORTING -> {
                        if (i < sortI) g2d.color = Color(100, 149, 237)
                        else if (i == sortJ || i == sortJ - 1) g2d.color = Color.ORANGE
                        else g2d.color = Color.LIGHT_GRAY
                    }
                    State.MERGING -> {
                        if (i == mergeIndex) g2d.color = Color.ORANGE
                        else if (i < mergeIndex) g2d.color = Color(240, 240, 240)
                        else g2d.color = Color(100, 149, 237)
                    }
                    else -> g2d.color = Color(100, 149, 237)
                }

                g2d.fillRect(x1, yStart, w, h)

                if ((currentState == State.MERGING && i == mergeIndex) ||
                    (currentState == State.SORTING && (i == sortJ || i == sortJ - 1))) {
                    g2d.color = Color.RED
                    if (h > 3) {
                        g2d.stroke = BasicStroke(2f)
                        g2d.drawRect(x1 - 1, yStart - 1, w + 2, h)
                    }
                }
            }
        }
    }
    layeredPane.add(inputPanel, Integer(0))

    // --- 2. Merged Panel ---
    val mergedPanel = object : JPanel() {
        init {
            background = Color(245, 245, 245)
            border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY)
            setBounds(0, bottomPanelY, 1200, bottomPanelH)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val scaleX = (width - 2 * 50).toDouble() / rangeSpan
            fun mapX(value: Long): Int = (50 + (value - minVal) * scaleX).toInt()

            g2d.color = Color.BLACK
            g2d.font = Font("SansSerif", Font.BOLD, 16)
            g2d.drawString("Merged Result:", 10, 20)

            val totalSize = visualMergedList.sumOf { it.second - it.first + 1 }
            g2d.font = Font("Monospaced", Font.PLAIN, 12)
            g2d.drawString("Count: ${visualMergedList.size}   Total Covered: $totalSize", 10, 40)

            val barY = 60
            val barHeight = 40

            val listCopy = ArrayList(visualMergedList)
            listCopy.forEach { range ->
                g2d.color = Color(50, 205, 50)
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
    layeredPane.add(mergedPanel, Integer(0))

    // --- 3. Animation Layer ---
    val animPanel = object : JPanel() {
        init {
            isOpaque = false
            setBounds(0, 0, 1200, 800)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val items = ArrayList(fallingItems)
            items.forEach { item ->
                g2d.color = item.color
                g2d.fillRect(item.startX, item.currentY.toInt(), item.width, 10)
                g2d.color = item.color.darker()
                g2d.drawRect(item.startX, item.currentY.toInt(), item.width, 10)
            }
        }
    }
    layeredPane.add(animPanel, Integer(200))

    // --- Timer & Logic ---
    val timer = Timer(16, null)
    var tickCounter = 0

    fun resetAnimation() {
        timer.stop()
        displayList.clear()
        displayList.addAll(unsortedRanges)
        logicalMergedList.clear()
        visualMergedList.clear()
        fallingItems.clear()

        currentState = State.SHOW_UNSORTED
        message = "Original Ranges (Unsorted)"
        sortI = 0
        sortJ = displayList.size - 1
        mergeIndex = 0

        timer.initialDelay = 500
        timer.start()
        frame.repaint()
    }

    restartButton.addActionListener { resetAnimation() }

    timer.addActionListener {
        tickCounter++
        var fullRepaintNeeded = false

        // Physics Update (Constant speed, or boosted by speedFactor for fast-forward)
        if (fallingItems.isNotEmpty()) {
            val gravity = 20.0 + (speedFactor / 5.0) // Fall slightly faster at high speed
            val iter = fallingItems.iterator()
            while (iter.hasNext()) {
                val item = iter.next()
                item.currentY += gravity
                if (item.currentY >= item.targetY) {
                    iter.remove()
                    visualMergedList.clear()
                    visualMergedList.addAll(logicalMergedList)
                    mergedPanel.repaint()
                }
            }
            animPanel.repaint()
        }

        // Logic Throttling based on Speed Slider
        // Calculate ops per tick based on speedFactor
        // 1 -> very slow
        // 50 -> medium
        // 100 -> instant

        val opsPerTick = when {
            speedFactor < 10 -> 1
            speedFactor < 50 -> speedFactor / 2
            else -> speedFactor * 2
        }

        // Throttle ticks if speed is very low
        val shouldRunLogic = if (speedFactor < 5) (tickCounter % (6 - speedFactor) == 0) else true

        if (shouldRunLogic) {
            when (currentState) {
                State.SHOW_UNSORTED -> {
                    if (timer.initialDelay == 0) {
                        currentState = State.SORTING
                        message = "Sorting Ranges..."
                        fullRepaintNeeded = true
                    } else timer.initialDelay = 0
                }

                State.SORTING -> {
                    repeat(opsPerTick) {
                        if (sortI < displayList.size - 1) {
                            if (sortJ > sortI) {
                                if (displayList[sortJ].first < displayList[sortJ-1].first) {
                                    val tmp = displayList[sortJ]
                                    displayList[sortJ] = displayList[sortJ-1]
                                    displayList[sortJ-1] = tmp
                                }
                                sortJ--
                            } else {
                                sortI++
                                sortJ = displayList.size - 1
                            }
                        } else {
                            currentState = State.MERGED_WAIT
                            message = "Sorted! Ready to merge."
                            fullRepaintNeeded = true
                            return@repeat
                        }
                    }
                    fullRepaintNeeded = true
                }

                State.MERGED_WAIT -> {
                    currentState = State.MERGING
                    message = "Merging..."
                }

                State.MERGING -> {
                    // Merging speed also scaled by speedFactor
                    val mergeOps = if (speedFactor > 80) speedFactor / 10 else 1

                    repeat(mergeOps) {
                        if (mergeIndex < displayList.size) {
                            val next = displayList[mergeIndex]
                            val prev = logicalMergedList.lastOrNull()

                            // Logic
                            if (prev == null) {
                                logicalMergedList.add(next)
                            } else {
                                when {
                                    next.first > prev.second + 1 -> logicalMergedList.add(next)
                                    next.second > prev.second -> {
                                        logicalMergedList[logicalMergedList.lastIndex] = prev.first to next.second
                                    }
                                }
                            }

                            // Visuals
                            val scaleX = (inputPanel.width - 2 * 50).toDouble() / rangeSpan
                            fun mapX(value: Long): Int = (50 + (value - minVal) * scaleX).toInt()
                            val startX = mapX(next.first)
                            val width = (mapX(next.second) - startX).coerceAtLeast(4)
                            val count = displayList.size
                            val rowH = if (count > 0) (inputPanel.height - 50.0) / count else 10.0
                            val startY = topPanelY + headerH + (mergeIndex * rowH)
                            val targetY = bottomPanelY + 60.0

                            fallingItems.add(FallingRange(next, startY, targetY, startX, width, Color.ORANGE))
                            mergeIndex++
                            fullRepaintNeeded = true
                        } else {
                            if (fallingItems.isEmpty()) {
                                currentState = State.FINISHED
                                message = "Finished! Final Count: ${logicalMergedList.size}"
                                fullRepaintNeeded = true
                            }
                        }
                    }
                }
                State.FINISHED -> {}
            }
        }

        if (fullRepaintNeeded) {
            inputPanel.repaint()
        }
    }

    frame.addComponentListener(object: java.awt.event.ComponentAdapter() {
        override fun componentResized(e: java.awt.event.ComponentEvent?) {
            val w = frame.width
            val h = frame.height
            controlPanel.setBounds(0, 0, w, 40)
            inputPanel.setBounds(0, topPanelY, w, topPanelH)
            mergedPanel.setBounds(0, bottomPanelY, w, bottomPanelH)
            animPanel.setBounds(0, 0, w, h)
        }
    })

    timer.initialDelay = 1000
    timer.start()
    frame.addWindowListener(object : WindowAdapter() { override fun windowClosed(e: WindowEvent?) { timer.stop() } })
    frame.isVisible = true
}
// ... existing code ...
// Usage:
// visualize(ranges.map { it.first..it.second })
// ... existing code ...
fun main() {
    visualize(Day05().freshIngredientRanges)
}
