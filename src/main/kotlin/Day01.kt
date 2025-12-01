class Day01 : Day(1, 2025, "Secret Entrance") {
    private val turns = input.lines.map {
        it.first() to it.drop(1).toInt()
    }

    override fun part1(): Int {
        val result = turns.runningFold(50) { pos, (d, n) ->
            val next = when (d) {
                'L' -> pos - n
                else -> pos + n
            }
            next % 100.also { log { "$pos $d $n -> $it" } }
        }

        return result.count { it == 0 }
    }

    override fun part2(): Int {
        val result = turns.fold(50 to 0) { (pos, count), (d, n) ->
            val completeTurns = n / 100
            val remainingTurns = n % 100
            if (remainingTurns == 0) return@fold (pos to count + completeTurns)

            val (nextPos, passesZero) = when (d) {
                'L' -> (pos - remainingTurns).mod(100) to (pos > 0 && pos - remainingTurns < 0)
                else -> (pos + remainingTurns).mod(100) to (pos > 0 && pos + remainingTurns > 100)
            }
            val pointsAtZero = nextPos == 0

            (nextPos to (count + completeTurns + (if (passesZero || pointsAtZero) 1 else 0))).also {
                log { "The dial is rotated $d$n to point at ${it.first} (0-counter is ${it.second})" }
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
