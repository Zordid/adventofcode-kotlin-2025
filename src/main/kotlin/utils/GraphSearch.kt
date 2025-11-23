package utils

enum class SearchControl { STOP, CONTINUE }
typealias DebugHandler<N> = (level: Int, nodesOnLevel: Collection<N>, nodesVisited: Collection<N>) -> SearchControl

typealias SolutionPredicate<N> = (node: N) -> Boolean

data class SearchResult<N>(val solution: N?, val distance: Map<N, Int>, val prev: Map<N, N>) {
    val success: Boolean get() = solution != null
    val distanceToStart: Int? = solution?.let { distance[it] }
    val steps: Int? by lazy { (path.size - 1).takeIf { it >= 0 } }
    val path by lazy { pathTo(solution) }

    fun pathTo(destination: N?): List<N> {
        val path = ArrayDeque<N>()
        if (destination in distance) {
            var nodeFoundThroughPrevious: N? = destination
            while (nodeFoundThroughPrevious != null) {
                path.addFirst(nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = prev[nodeFoundThroughPrevious]
            }
        }
        return path
    }

    fun distanceTo(destination: N?) = distance[destination]

}

data class MultiSolutionSearchResult<N>(val solutions: Set<N>, val distance: Map<N, Int>, val prev: Map<N, List<N>>) {
    val success: Boolean get() = solutions.isNotEmpty()
    val distanceToStart: Int? = solutions.firstOrNull()?.let { distance[it] }
    val paths by lazy { solutions.flatMap { pathsTo(it) } }

    private val backTrack = DeepRecursiveFunction<Pair<N, List<N>>, List<List<N>>> { (current, path) ->
        val prev = prev[current] ?: return@DeepRecursiveFunction listOf(listOf(current) + path.reversed())
        buildList {
            val pathToCurrent = path + current
            for (predecessor in prev) {
                addAll(callRecursive(predecessor to pathToCurrent))
            }
        }
    }

    fun pathsTo(destination: N?): List<List<N>> =
        if (destination != null && destination in distance)
            backTrack(destination to listOf())
        else emptyList()

}

class Dijkstra<N>(
    startNode: N,
    val neighborsOf: (node: N) -> Collection<N>,
    val cost: (from: N, to: N) -> Int,
) {

    private class SearchStateSingle<N>(
        val startNode: N,
        val dist: HashMap<N, Int> = HashMap(mapOf(startNode to 0)),
        val prev: HashMap<N, N> = HashMap(),
        val queue: MinPriorityQueue<N, Int> = minPriorityQueueOf(startNode to 0),
    )

    private class SearchStateMulti<N>(
        val startNode: N,
        val dist: HashMap<N, Int> = HashMap<N, Int>(mapOf(startNode to 0)),
        val prev: HashMap<N, MutableList<N>> = HashMap(),
        val queue: MinPriorityQueue<N, Int> = minPriorityQueueOf(startNode to 0),
    )

    private val searchStateSingle = SearchStateSingle(startNode)
    private val searchStateMulti = SearchStateMulti(startNode)

    fun search(endNode: N?): SearchResult<N> {
        with(searchStateSingle) {
            if (endNode in prev) return SearchResult(endNode, dist, prev)
        }
        return searchInternal { it == endNode }
    }

    fun search(predicate: SolutionPredicate<N>): SearchResult<N> {
        with(searchStateSingle) {
            if (prev.isNotEmpty()) {
                // check previous calculated distances for matching predicate
                dist.firstNotNullOfOrNull { (node, _) -> node.takeIf(predicate) }
                    ?.let { return SearchResult(it, dist, prev) }
            }
        }
        return searchInternal(predicate)
    }

    private fun searchInternal(predicate: SolutionPredicate<N>): SearchResult<N> = with(searchStateSingle) {
        while (queue.isNotEmpty()) {
            val u = queue.extractMin()
            if (predicate(u)) {
                return SearchResult(u, dist, prev)
            }
            for (v in neighborsOf(u)) {
                val alt = dist[u]!! + cost(u, v)
                if (alt < dist.getOrDefault(v, Int.MAX_VALUE)) {
                    dist[v] = alt
                    prev[v] = u
                    queue.insertOrUpdate(v, alt)
                }
            }
        }

        // no matching solution found
        return SearchResult(null, dist, prev)
    }

    fun searchAll(endNode: N?): MultiSolutionSearchResult<N> {
        with(searchStateMulti) {
            if (endNode != null && endNode in prev) return MultiSolutionSearchResult(setOf(endNode), dist, prev)
        }
        return searchAllInternal { it == endNode }
    }

    fun searchAll(predicate: SolutionPredicate<N>): MultiSolutionSearchResult<N> {
        with(searchStateMulti) {
            val matches = prev.keys.filter(predicate)
            matches.minOfOrNull { dist[it]!! }?.let { minDistance ->
                return MultiSolutionSearchResult(matches.filter { dist[it] == minDistance }.toSet(), dist, prev)
            }
        }
        return searchAllInternal(predicate)
    }

    private fun searchAllInternal(predicate: SolutionPredicate<N>): MultiSolutionSearchResult<N> =
        with(searchStateMulti) {
            while (queue.isNotEmpty()) {
                val (u, priority) = queue.extractMinWithPriority()
                if (predicate(u)) {
                    // do not forget to ask for more solutions with the same priority
                    val moreSolutions = if (queue.minPriority == priority)
                        queue.extractAllMin().filter { predicate(it) } else emptyList()
                    return MultiSolutionSearchResult(setOf(u) + moreSolutions, dist, prev)
                }
                for (v in neighborsOf(u)) {
                    val alt = dist[u]!! + cost(u, v)
                    val known = dist.getOrDefault(v, Int.MAX_VALUE)
                    when {
                        // relax, if new distance is less than known
                        alt < known -> {
                            dist[v] = alt
                            prev[v] = mutableListOf(u)
                            queue.insertOrUpdate(v, alt)
                        }

                        // add previous if distance is equal to known
                        alt == known -> {
                            prev[v]?.let { it += u }
                        }
                    }
                }
            }

            // no matching solutions found
            return MultiSolutionSearchResult(emptySet(), dist, prev)
        }

}

interface SearchState<N> {
    val next: N?
    val dist: Map<N, Int>
    val prev: Map<N, N>
}

class AStarSearch<N>(
    startNodes: Collection<N>,
    val neighborsOf: (node: N) -> Collection<N>,
    val cost: (from: N, to: N) -> Int,
    val costEstimation: (from: N, to: N) -> Int,
    val onExpand: (SearchState<N>.(N) -> Unit)? = null,
) {
    constructor(
        startNode: N,
        neighborsOf: (node: N) -> Collection<N>,
        cost: (from: N, to: N) -> Int,
        costEstimation: (from: N, to: N) -> Int,
        onExpand: (SearchState<N>.(N) -> Unit)? = null,
    ) : this(listOf(startNode), neighborsOf, cost, costEstimation, onExpand)

    private val dist = HashMap<N, Int>(startNodes.associateWith { 0 })
    private val prev = HashMap<N, N>()
    private val openList = minPriorityQueueOf(startNodes.map { it to 0 })
    private val closedList = HashSet<N>()

    val state = object : SearchState<N> {
        override val next get() = openList.peekOrNull()
        override val dist get() = this@AStarSearch.dist
        override val prev get() = this@AStarSearch.prev
    }

    fun search(destinationNode: N, limitSteps: Int? = null): SearchResult<N> {

        fun expandNode(currentNode: N) {
            onExpand?.invoke(state, currentNode)
            for (successor in neighborsOf(currentNode)) {
                if (successor in closedList)
                    continue

                val tentativeDist = dist[currentNode]!! + cost(currentNode, successor)
                if (successor in openList && tentativeDist >= dist[successor]!!)
                    continue

                prev[successor] = currentNode
                dist[successor] = tentativeDist

                val f = tentativeDist + costEstimation(successor, destinationNode)
                openList.insertOrUpdate(successor, f)
            }
        }

        if (destinationNode in closedList)
            return SearchResult(destinationNode, dist, prev)

        var steps = 0
        while (steps++ != limitSteps && openList.isNotEmpty()) {
            val currentNode = openList.extractMin()
            if (currentNode == destinationNode)
                return SearchResult(destinationNode, dist, prev)

            closedList += currentNode
            expandNode(currentNode)
        }

        return SearchResult(null, dist, prev)
    }
}

class AStarSearch2<N>(
    startNodes: Collection<N>,
    val neighborsOf: (node: N) -> Collection<N>,
    val cost: (from: N, to: N) -> Int,
    val costEstimation: (from: N, to: N?) -> Int,
    val onExpand: (SearchState<N>.(N) -> Unit)? = null,
) {
    constructor(
        startNode: N,
        neighborsOf: (node: N) -> Collection<N>,
        cost: (from: N, to: N) -> Int,
        costEstimation: (from: N, to: N?) -> Int,
        onExpand: (SearchState<N>.(N) -> Unit)? = null,
    ) : this(listOf(startNode), neighborsOf, cost, costEstimation, onExpand)

    private val dist = HashMap<N, Int>(startNodes.associateWith { 0 })
    private val prev = HashMap<N, N>()
    private val openList = minPriorityQueueOf(startNodes.map { it to 0 })
    private val closedList = HashSet<N>()

    val state = object : SearchState<N> {
        override val next get() = openList.peekOrNull()
        override val dist get() = this@AStarSearch2.dist
        override val prev get() = this@AStarSearch2.prev
    }

    fun search(destinationPredicate: (N) -> Boolean, limitSteps: Int? = null): SearchResult<N> {

        fun expandNode(currentNode: N) {
            onExpand?.invoke(state, currentNode)
            for (successor in neighborsOf(currentNode)) {
                if (successor in closedList)
                    continue

                val tentativeDist = dist[currentNode]!! + cost(currentNode, successor)
                if (successor in openList && tentativeDist >= dist[successor]!!)
                    continue

                prev[successor] = currentNode
                dist[successor] = tentativeDist

                val f = tentativeDist + costEstimation(successor, null)
                openList.insertOrUpdate(successor, f)
            }
        }

//        if (destinationNode in closedList)
//            return SearchResult(destinationNode, dist, prev)

        var steps = 0
        while (steps++ != limitSteps && openList.isNotEmpty()) {
            val currentNode = openList.extractMin()
            if (destinationPredicate(currentNode))
                return SearchResult(currentNode, dist, prev)

            closedList += currentNode
            expandNode(currentNode)
        }

        return SearchResult(null, dist, prev)
    }
}

//class DepthSearch<N, E>(
//    val startNode: N,
//    private val edgesOfNode: (N) -> Iterable<E>,
//    private val walkEdge: (N, E) -> N,
//) {
//    private val nodesVisited = mutableSetOf<N>(startNode)
//    private val nodesDiscoveredThrough = mutableMapOf<N, N>()
//
//    fun search(predicate: SolutionPredicate<N>): SearchResult<N> {
//        if (predicate(startNode))
//            return SearchResult(startNode, emptyMap(), nodesDiscoveredThrough)
//
//        val edges = edgesOfNode(startNode)
//        for (edge in edges) {
//            val nextNode = walkEdge(node, edge)
//            if (!nodesVisited.contains(nextNode)) {
//                nodesDiscoveredThrough[nextNode] = node
//                val found = searchFrom(nextNode, isSolution)
//                if (found != null)
//                    return found
//            }
//        }
//        return null
//    }
//}

interface EdgeGraph<N, E> {
    fun edgesOfNode(node: N): Iterable<E>
    fun walkEdge(node: N, edge: E): N
}

abstract class UninformedSearch<N, E>(val graph: EdgeGraph<N, E>) : EdgeGraph<N, E> by graph {

    data class Result<N, E>(val node: N, val prev: Map<N, Pair<N, E>>, val visited: Set<N>)

    fun search(start: N, destination: N) = search(start) { it == destination }
    open fun search(start: N, solutionPredicate: SolutionPredicate<N>): Result<N, E>? =
        traverse(start).firstOrNull { solutionPredicate(it.node) }

    abstract fun traverse(start: N): Sequence<Result<N, E>>

    class BFS<N, E>(graph: EdgeGraph<N, E>) : UninformedSearch<N, E>(graph) {
        override fun traverse(start: N): Sequence<Result<N, E>> = sequence {
            val nodesVisited = HashSet<N>()
            val nodesDiscoveredThrough = HashMap<N, Pair<N, E>>()
            val queue = ArrayDeque<N>()
            queue += start
            nodesVisited += start
            yield(Result(start, nodesDiscoveredThrough, nodesVisited))
            while (queue.isNotEmpty()) {
                val currentNode = queue.removeFirst()
                nodesVisited += currentNode
                edgesOfNode(currentNode).forEach { edge ->
                    val neighbor = walkEdge(currentNode, edge)
                    if (neighbor !in nodesVisited) {
                        nodesDiscoveredThrough[neighbor] = currentNode to edge
                        queue.addLast(neighbor)
                        yield(Result(neighbor, nodesDiscoveredThrough, nodesVisited))
                    }
                }
            }
        }
    }


}

fun <N, E> SearchEngineWithEdges<N, E>.bfsSequence(startNode: N): Sequence<N> = sequence {
    val nodesVisited = mutableSetOf<N>()
    val nodesDiscoveredThrough = mutableMapOf<N, N>()
    val queue = ArrayDeque<N>()
    queue += startNode
    yield(startNode)
    while (queue.isNotEmpty()) {
        val currentNode = queue.removeFirst()
        nodesVisited += currentNode
        edgesOfNode(currentNode).forEach { edge ->
            val neighbor = walkEdge(currentNode, edge)
            if (neighbor !in nodesVisited) {
                nodesDiscoveredThrough[neighbor] = currentNode
                queue.addLast(neighbor)
                yield(neighbor)
            }
        }
    }
}


open class SearchEngineWithEdges<N, E>(
    val edgesOfNode: (N) -> Iterable<E>,
    val walkEdge: (N, E) -> N,
) {

    var debugHandler: DebugHandler<N>? = null

    inner class BfsSearch(val startNode: N, val isSolution: SolutionPredicate<N>) {
        val solution: N?
        val nodesVisited = mutableSetOf<N>()
        val nodesDiscoveredThrough = mutableMapOf<N, N>()

        private tailrec fun searchLevel(nodesOnLevel: Set<N>, level: Int = 0): N? {
            if (debugHandler?.invoke(level, nodesOnLevel, nodesVisited) == SearchControl.STOP)
                return null
            val nodesOnNextLevel = mutableSetOf<N>()
            nodesOnLevel.forEach { currentNode ->
                nodesVisited.add(currentNode)
                edgesOfNode(currentNode).forEach { edge ->
                    val node = walkEdge(currentNode, edge)
                    if (node !in nodesVisited && node !in nodesOnLevel) {
                        nodesDiscoveredThrough[node] = currentNode
                        if (isSolution(node))
                            return node
                        else
                            nodesOnNextLevel.add(node)
                    }
                }
            }
            return if (nodesOnNextLevel.isEmpty())
                null
            else
                searchLevel(nodesOnNextLevel, level + 1)
        }

        private fun buildStack(node: N?): List<N> {
            //println("Building stack for solution node $node")
            val pathStack = ArrayDeque<N>()
            var nodeFoundThroughPrevious = node
            while (nodeFoundThroughPrevious != null) {
                pathStack.addFirst(nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = nodesDiscoveredThrough[nodeFoundThroughPrevious]
            }
            return pathStack
        }

        init {
            solution = if (isSolution(startNode)) startNode else searchLevel(setOf(startNode))
        }

        fun path(): List<N> {
            return buildStack(solution)
        }

    }

    private inner class DepthSearch(val startNode: N, val isSolution: SolutionPredicate<N>) {

        private val nodesVisited = mutableSetOf<N>()
        private val nodesDiscoveredThrough = mutableMapOf<N, N>()

        private fun searchFrom(node: N, isSolution: SolutionPredicate<N>): N? {
            if (isSolution(node))
                return node
            nodesVisited.add(node)
            val edges = edgesOfNode(node)
            for (edge in edges) {
                val nextNode = walkEdge(node, edge)
                if (!nodesVisited.contains(nextNode)) {
                    nodesDiscoveredThrough[nextNode] = node
                    val found = searchFrom(nextNode, isSolution)
                    if (found != null)
                        return found
                }
            }
            return null
        }

        private fun buildStack(node: N?): ArrayDeque<N> {
            //println("Building stack for solution node $node")
            val pathStack = ArrayDeque<N>()
            var nodeFoundThroughPrevious = node
            while (nodeFoundThroughPrevious != null) {
                pathStack.addFirst(nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = nodesDiscoveredThrough[nodeFoundThroughPrevious]
            }
            return pathStack
        }

        fun search() = buildStack(searchFrom(startNode, isSolution))

        fun findBest(): Pair<ArrayDeque<N>, Set<N>> {
            return buildStack(searchFrom(startNode, isSolution)) to nodesVisited
        }

    }

    fun bfsSearch(startNode: N, isSolution: SolutionPredicate<N>) =
        BfsSearch(startNode, isSolution)

    fun depthFirstSearch(startNode: N, isSolution: SolutionPredicate<N>): ArrayDeque<N> {
        return DepthSearch(startNode, isSolution).search()
    }

    fun depthFirstSearchWithNodes(startNode: N, isSolution: SolutionPredicate<N>): Pair<ArrayDeque<N>, Set<N>> {
        return DepthSearch(startNode, isSolution).findBest()
    }

    fun completeAcyclicTraverse(startNode: N): Sequence<AcyclicTraverseLevel<N>> =
        sequence {
            var nodesOnPreviousLevel: Set<N>
            var nodesOnLevel = setOf<N>()
            var nodesOnNextLevel = setOf(startNode)
            var level = 0

            while (nodesOnNextLevel.isNotEmpty()) {
                nodesOnPreviousLevel = nodesOnLevel
                nodesOnLevel = nodesOnNextLevel
                yield(AcyclicTraverseLevel(level++, nodesOnLevel, nodesOnPreviousLevel))
                nodesOnNextLevel = mutableSetOf()
                nodesOnLevel.forEach { node ->
                    nodesOnNextLevel.addAll(
                        edgesOfNode(node).map { e -> walkEdge(node, e) }
                            .filter { neighbor ->
                                neighbor !in nodesOnLevel && neighbor !in nodesOnPreviousLevel
                            }
                    )
                }
            }
        }

}

data class AcyclicTraverseLevel<N>(val level: Int, val nodesOnLevel: Set<N>, val nodesOnPreviousLevel: Set<N>) :
    Collection<N> by nodesOnLevel

data class SearchLevel<N>(val level: Int, val nodesOnLevel: Collection<N>, val visited: Set<N>)

class SearchEngineWithNodes<N>(neighborsOf: (N) -> Collection<N>) :
    SearchEngineWithEdges<N, N>(neighborsOf, { _, edge -> edge })

fun <N, E> breadthFirstSearch(
    startNode: N,
    edgesOf: (N) -> Collection<E>,
    walkEdge: (N, E) -> N,
    isSolution: SolutionPredicate<N>,
) =
    SearchEngineWithEdges(edgesOf, walkEdge).bfsSearch(startNode, isSolution)

fun <N> breadthFirstSearch(
    startNode: N,
    neighborsOf: (N) -> Collection<N>,
    isSolution: SolutionPredicate<N>,
) =
    SearchEngineWithNodes(neighborsOf).bfsSearch(startNode, isSolution)

fun <N> depthFirstSearch(
    startNode: N,
    neighborsOf: (N) -> Collection<N>,
    isSolution: SolutionPredicate<N>,
): ArrayDeque<N> =
    SearchEngineWithNodes(neighborsOf).depthFirstSearch(startNode, isSolution)

fun <N> loggingDebugger(): DebugHandler<N> = { level: Int, nodesOnLevel: Collection<N>, nodesVisited: Collection<N> ->
    println("I am on level $level, searching through ${nodesOnLevel.size}. Visited so far: ${nodesVisited.size}")
    SearchControl.CONTINUE
}
