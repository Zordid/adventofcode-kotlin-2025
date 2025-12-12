import com.google.ortools.sat.CpModel
import com.google.ortools.sat.CpSolver
import com.google.ortools.sat.CpSolverStatus
import com.google.ortools.sat.LinearExpr
import utils.*

class Day10 : Day(10, 2025, "Factory") {

    val machines = input.map { s ->
        val indicatorsString = s.substringAfter('[').substringBefore(']')
        val buttonsStrings = s.substringAfter('(').substringBeforeLast(')').split(") (")
        val joltageString = s.substringAfter('{').substringBefore('}')

        val size = indicatorsString.length
        val indicators = indicatorsString.indices.filter { indicatorsString[it] == '#' }.toSet()
        val buttons = buttonsStrings.map { it.extractAllIntegers().toSet() }
        val joltage = joltageString.extractAllIntegers()

        Machine(size, indicators, buttons, joltage)
    }

    override fun part1() = machines.sumOf { machine ->
        val buttons = machine.buttons
        val goal = machine.indicators
        val queue = minPriorityQueueOf(emptySet<Int>() to 0)
        val visited = mutableSetOf<Set<Int>>()
        val dist = mutableMapOf(emptySet<Int>() to 0)
        while (queue.isNotEmpty()) {
            val current = queue.extractMin()
            visited += current
            if (current == goal) return@sumOf (dist[goal]!!)
            val neighbors = buttons.map { flip ->
                buildSet {
                    (0 until machine.size).forEach { idx ->
                        if (idx in flip != idx in current) add(idx)
                    }
                }
            }.filter { it !in visited }
            val tentativeDist = dist[current]!! + 1
            neighbors.forEach { n ->
                if (tentativeDist < (dist[n] ?: Int.MAX_VALUE)) {
                    dist[n] = tentativeDist
                    queue.insertOrUpdate(n, tentativeDist)
                }
            }
        }
        error("no path found")
    }

    override fun part2() = machines.sumOf { machine ->
        solveCompositionProblem(machine.joltage, machine.buttons.map { b ->
            List(machine.size) { idx -> if (idx in b) 1 else 0 }
        })!!
    }

    data class Machine(val size: Int, val indicators: Set<Int>, val buttons: List<Set<Int>>, val joltage: List<Int>)

}

fun solveCompositionProblem(target: List<Int>, compositions: List<List<Int>>): Int? {
    com.google.ortools.Loader.loadNativeLibraries()
    val model = CpModel()

    // 1. Define Decision Variables with a realistic upper bound
    val x = compositions.mapIndexed { idx, composition ->
        val ub = composition.withIndex().filter { it.value == 1 }.minOf { (idx, _) ->
            target[idx]
        }
        model.newIntVar(0, ub.toLong(), "x_$idx")
    }

    // 2. Define Constraints
    for (j in target.indices) {
        val constraintExpr = LinearExpr.newBuilder().apply {
            x.indices.forEach { i ->
                // Add (composition[i][j] * x[i]) to the expression
                addTerm(x[i], compositions[i][j].toLong())
            }
        }.build()
        // The expression must equal the target value for this dimension
        model.addEquality(constraintExpr, target[j].toLong())
    }

    // 3. Define Objective Function (Minimize the total number of parts used)
    val objective = LinearExpr.newBuilder().apply {
        x.forEach { addTerm(it, 1) }
    }.build()
    model.minimize(objective)

    // 4. Solve the model
    val solver = CpSolver()
    val status = solver.solve(model)

    return if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
        solver.objectiveValue().toInt()
    } else {
        println("Solver status: $status")
        if (status == CpSolverStatus.MODEL_INVALID) {
            // Use model.validate() to get the specific reason for invalidity if possible in Java/Kotlin bindings
            println("The model definition is invalid. Check constraints.")
        } else if (status == CpSolverStatus.INFEASIBLE) {
            println("The problem is infeasible: Target cannot be reached with given compositions.")
        }
        null
    }
}

fun main() {
    solve<Day10> {
        """
            [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
            [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
            [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
        """.trimIndent() part1 7 part2 33
    }
}