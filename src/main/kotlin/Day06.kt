import utils.*
import kotlin.text.getOrElse

open class Day06 : Day(6, 2025, "Trash Compactor") {

    private val problemNotes = input.lines

    override fun part1() =
        problemNotes.last().withIndex().filter { (_, c) -> c != ' ' }.flatMap { (idx, _) ->
            problemNotes.map { it.drop(idx).trim().takeWhile { it != ' ' } }
        }.solveProblems()

    override fun part2() =
        problemNotes.transposed().reversed().solveProblems()

    private fun List<String>.solveProblems(): Long =
        fold(listOf(0L)) { total, statement ->
            log { statement }
            val (digits, nonDigits) = statement.partition { it.isDigit() }
            val number = digits.toLongOrNull()
            val op = nonDigits.trim().takeIf { it.isNotEmpty() }

            (total + listOfNotNull(number)).let {
                when (op) {
                    null -> it
                    "+" -> listOf(it.first() + it.drop(1).sum())
                    "*" -> listOf(it.first() + it.drop(1).product())
                    else -> error(statement)
                }
            }.also { log { "=> $it" } }
        }.single()

}

fun main() {
    solve<Day06> {
        """
            123 328  51 64 
             45 64  387 23 
              6 98  215 314
            *   +   *   +  
        """.trimIndent() part1 4277556 part2 3263827
    }
}

/**
 * Transposes a list of strings, treating them as rows in a 2D grid, where each character corresponds to a column.
 * Each resulting string in the list represents a column in the original grid.
 * If rows have unequal lengths, shorter rows are padded with spaces to match the longest row.
 *
 * @return A new list of strings, where each string represents a transposed column from the original list.
 */
fun List<String>.transposed(): List<String> =
    (0 until (maxOfOrNull { it.length } ?: 0)).map { colIdx ->
        buildString(size) {
            this@transposed.forEach { append(it.getOrElse(colIdx) { ' ' }) }
        }
    }
