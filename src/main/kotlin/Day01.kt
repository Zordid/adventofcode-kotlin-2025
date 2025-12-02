import kotlin.math.absoluteValue

class Day01 : Day(1, 2025, "Secret Entrance") {
    private val turns = input.lines.map {
        it.replace('L', '-').replace('R', '+').toInt()
    }

    override fun part1(): Int {
        val result = turns.runningFold(50) { pos, turn ->
            log((pos + turn) % 100) { "$pos $turn -> $it" }
        }

        return result.count { it == 0 }
    }

    override fun part2(): Int {
        val result = turns.fold(50 to 0) { (pos, count), turn ->
            val completeTurns = turn.absoluteValue / 100
            val remainingTurns = turn % 100

            val nextPos = (pos + remainingTurns).mod(100)
            val passesZero = pos > 0 && pos + remainingTurns !in 1 until 100
            val pointsAtZero = pos > 0 && nextPos == 0

            log(nextPos to (count + completeTurns + (if (passesZero || pointsAtZero) 1 else 0))) {
                "The dial is rotated $turn to point at ${it.first} (0-counter is ${it.second})"
            }
        }

        return result.second
    }

}

fun main() {
    solve<Day01> {
        """
            L68
            L30
            R48
            L5
            R60
            L55
            L1
            L99
            R14
            L82
        """.trimIndent() part1 3 part2 6
    }
}
