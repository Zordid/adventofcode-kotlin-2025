@file:Suppress("unused")

package utils

import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import java.io.File

/**
 * A general interface to describe graphs with nodes described by neighborhood.
 */
interface Graph<N> {
    fun neighborsOf(node: N): Collection<N>

    fun cost(from: N, to: N): Int = 1
}

interface GraphWithCostEstimation<N> : Graph<N> {
    fun costEstimation(from: N, to: N): Int
}

/**
 * An adjacency map stores the neighbors of each node [N] in a [Set] of direct neighbors.
 */
typealias AdjacencyMap<N> = Map<N, Set<N>>

/**
 * Converts the given [AdjacencyMap] into a generic [Graph]
 */
fun <N> AdjacencyMap<N>.toGraph(): Graph<N> = object : Graph<N> {
    override fun neighborsOf(node: N) = this@toGraph[node] ?: emptyList()
}

inline fun <N> graph(
    crossinline neighborsOf: (N) -> Collection<N>,
    crossinline cost: (N, N) -> Int = { _, _ -> 1 },
): Graph<N> =
    object : Graph<N> {
        override fun neighborsOf(node: N) = neighborsOf(node)
        override fun cost(from: N, to: N) = cost(from, to)
    }

inline fun <N> graph(
    crossinline neighborsOf: (N) -> Collection<N>,
    crossinline cost: (N, N) -> Int = { _, _ -> 1 },
    crossinline costEstimation: (N, N) -> Int,
): GraphWithCostEstimation<N> =
    object : GraphWithCostEstimation<N> {
        override fun neighborsOf(node: N) = neighborsOf(node)
        override fun cost(from: N, to: N) = cost(from, to)
        override fun costEstimation(from: N, to: N) = costEstimation(from, to)
    }

inline fun <N> Graph<N>.withCostEstimation(
    crossinline costEstimation: (N, N) -> Int,
): GraphWithCostEstimation<N> = object : GraphWithCostEstimation<N> {
    override fun neighborsOf(node: N): Collection<N> = neighborsOf(node)
    override fun cost(from: N, to: N): Int = cost(from, to)
    override fun costEstimation(from: N, to: N): Int = costEstimation(from, to)
}

fun <N> Graph<N>.depthFirstSearch(start: N, destinationPredicate: (N) -> Boolean): ArrayDeque<N> =
    depthFirstSearch(start, ::neighborsOf, destinationPredicate)

fun <N> Graph<N>.depthFirstSearch(start: N, destination: N): ArrayDeque<N> =
    depthFirstSearch(start, ::neighborsOf) { it == destination }

fun <N> Graph<N>.completeAcyclicTraverse(start: N) =
    SearchEngineWithNodes(::neighborsOf).completeAcyclicTraverse(start)

fun <N> Graph<N>.completeAcyclicTraverse(start: N, visit: AcyclicTraverseLevel<N>.() -> Unit) =
    SearchEngineWithNodes(::neighborsOf).completeAcyclicTraverse(start).forEach { it.visit() }

fun <N> Graph<N>.breadthFirstSearch(start: N, predicate: SolutionPredicate<N>) =
    SearchEngineWithNodes(::neighborsOf).bfsSearch(start, predicate)

fun <N> GraphWithCostEstimation<N>.aStarSearch(start: N, destination: N) =
    AStarSearch(
        start,
        neighborsOf = ::neighborsOf,
        cost = ::cost,
        costEstimation = ::costEstimation
    ).search(destination)

fun <N> Graph<N>.dijkstraSearch(start: N, destination: N?) =
    Dijkstra(start, ::neighborsOf, ::cost).search(destination)

fun <N> Graph<N>.dijkstraSearch(start: N, destinationPredicate: (N) -> Boolean) =
    Dijkstra(start, ::neighborsOf, ::cost).search(destinationPredicate)

fun <N> Graph<N>.dijkstraSearchAll(start: N, destination: N?) =
    Dijkstra(start, ::neighborsOf, ::cost).searchAll(destination)

fun <N> Graph<N>.dijkstraSearchAll(start: N, destinationPredicate: (N) -> Boolean) =
    Dijkstra(start, ::neighborsOf, ::cost).searchAll(destinationPredicate)

class GraphContext<N>(val graph: MutableGraph, val nodes: Map<N, MutableNode>)

fun <N> Graph<N>.viz(
    startNodes: Collection<N>,
    format: Format = Format.SVG,
    model: GraphContext<N>.() -> Unit = {}
): String {
    val g = graph("Demo", directed = true)
    val nodes = mutableMapOf<N, MutableNode>()
    startNodes.forEach { startNode ->
        completeAcyclicTraverse(startNode) {
            nodesOnLevel.forEach { node ->
                nodes.getOrPut(node) { mutNode(node.toString()).also { g.add(it) } }
            }
        }
    }
    nodes.forEach { (node, mutNode) ->
        mutNode.addLink(neighborsOf(node).map { nodes[it] })
    }
    GraphContext<N>(g, nodes).model()
    val file = Graphviz.fromGraph(g).render(Format.SVG).toFile(File("out"))
    return "file://${file.absolutePath}"
}
