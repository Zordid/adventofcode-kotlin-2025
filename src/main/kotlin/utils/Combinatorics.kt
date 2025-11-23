package utils

infix fun Int.choose(k: Int) = comb(this, k)

/**
 * Efficient calculation of combinations of k from n: "n choose k"
 */
@Suppress("LocalVariableName")
fun comb(n: Int, k: Int): Long {
    // binomial coefficient for negative n, see e.g. https://math.stackexchange.com/questions/3377614/n-choose-k-where-n-is-negative
    if (n < 0) return (if (k % 2 == 0) 1 else -1) * comb(-n + k - 1, k)

    if (k < 0 || k > n) return 0
    if (k == 0 || k == n) return 1

    var N = n.toLong()
    val K = if (2L * k > N) N - k else k.toLong()
    var result = 1L
    for (i in 1L..K) {
        result = (result * N--) / i
    }
    return result
}

/**
 * Generates all combinations of the elements of the given [Iterable] for the requested size.
 * Note: combinations do not include all their permutations!
 * @receiver the [Iterable] to take elements from
 * @param size the size of the combinations to create
 * @return a sequence of all combinations
 */
fun <T> Iterable<T>.combinations(size: Int): Sequence<List<T>> =
    toList().combinations(size)

/**
 * Generates all combinations of the elements of the given list for the requested size.
 * The resulting sequence will contain "n choose k" elements.
 * Note: combinations do not include all their permutations!
 * @receiver the list to take elements from
 * @param k the size of the combinations to create
 * @return a sequence of all combinations
 * @see choose
 */
fun <T> List<T>.combinations(k: Int): Sequence<List<T>> {
    val n = this.size
    return when {
        k < 0 || k > n -> emptySequence()
        k == 0 -> sequenceOf(emptyList())
        k == 1 -> asSequence().map { listOf(it) }
        k == n -> sequenceOf(this)
        k > n / 2 -> {
            // use symmetry property: choose n-k elements to exclude
            val complementCombinations = indices.combinations(n - k)
            complementCombinations.map { exclude ->
                ListWithExclusions(this, exclude)
            }
        }

        else -> sequence {
            this@combinations.forEachIndexed { index, element ->
                val head = listOf(element)
                val tail = this@combinations.subList(index + 1, n)
                tail.combinations(k - 1).forEach { tailCombinations ->
                    yield(head + tailCombinations)
                }
            }
        }
    }
}

/**
 * Generates all combinations of the elements of the given [IntRange] for the requested size.
 * Note: combinations do not include all their permutations!
 * @receiver the [IntRange] to take elements from
 * @param k the size of the combinations to create
 * @return a sequence of all combinations
 */
fun IntRange.combinations(k: Int): Sequence<List<Int>> {
    val n = this.size
    return when {
        k < 0 || k > n -> emptySequence()
        k == 0 -> sequenceOf(emptyList())
        k == 1 -> asSequence().map { listOf(it) }
        k == n -> sequenceOf(this.toList())
        else -> sequence {
            for (element in this@combinations) {
                val head = listOf(element)
                val tail = element + 1..this@combinations.last
                tail.combinations(k - 1).forEach {
                    yield(head + it)
                }
            }
        }
    }
}

/**
 * Generates a sequence of all permutations of the given list of elements.
 * @receiver the list of elements for permutation of order
 * @return a sequence of all permutations of the given list
 */
fun <T> Collection<T>.permutations(): Sequence<List<T>> =
    when (size) {
        0 -> emptySequence()
        1 -> sequenceOf(toList())
        else -> {
            val head = first()
            val tail = drop(1)
            tail.permutations().flatMap { perm ->
                (0..perm.size).asSequence().map { perm.withAddedAt(it, head) }
            }
        }
    }

/**
 * Generates a sequence of all permutations of the given numbers in the [IntRange]
 * @receiver the [IntRange] for permutation of order
 * @return a sequence of all permutations of the numbers in the range
 */
fun IntRange.permutations(): Sequence<List<Int>> =
    when {
        first > last -> emptySequence()
        first == last -> sequenceOf(listOf(first))
        else -> {
            val head = first
            val tail = first + 1..last
            tail.permutations().flatMap { perm ->
                (0..perm.size).asSequence().map { perm.withAddedAt(it, head) }
            }
        }
    }

fun String.permutations(): Sequence<String> =
    toList().permutations().map { it.joinToString("") }

fun String.combinations(size: Int): Sequence<String> =
    toList().combinations(size).map { it.joinToString("") }

private fun <T> List<T>.withAddedAt(insertAt: Int, element: T): List<T> =
    List(size + 1) { idx ->
        when {
            idx < insertAt -> this[idx]
            idx == insertAt -> element
            else -> this[idx - 1]
        }
    }

fun <E> List<E>.withRemoved(removeIndices: Set<Int>): List<E> {
    val validIndices = removeIndices.filter { it in this.indices }.sorted()
    return if (validIndices.isEmpty()) this
    else ListWithExclusions(this, validIndices)
}

private class ListWithExclusions<T>(
    private val backingList: List<T>,
    val excludedIndicesInOrder: List<Int> = emptyList()
) : List<T> {

    private fun effectiveIndex(viewIndex: Int): Int {
        if (viewIndex < 0) {
            throw IndexOutOfBoundsException("Index $viewIndex is out of bounds (negative).")
        }

        // Binary search to find the number of excluded indices up to viewIndex
        var left = 0
        var right = excludedIndicesInOrder.size

        while (left < right) {
            val mid = left + (right - left) / 2
            if (excludedIndicesInOrder[mid] <= viewIndex + mid) {
                left = mid + 1
            } else {
                right = mid
            }
        }

        return viewIndex + left
    }

    override val size: Int
        get() = backingList.size - excludedIndicesInOrder.size

    override fun contains(element: T): Boolean =
        indexOf(element) != -1

    override fun containsAll(elements: Collection<T>): Boolean =
        elements.all { contains(it) }

    override fun get(index: Int): T {
        if (index !in backingList.indices) indexOutOfBounds(index)
        val effectiveIndex = effectiveIndex(index)
        if (effectiveIndex > backingList.lastIndex) indexOutOfBounds(index)
        return backingList[effectiveIndex]
    }

    private fun indexOutOfBounds(index: Int): Nothing =
        throw IndexOutOfBoundsException("Index $index out of bounds for length $size")

    override fun indexOf(element: T): Int {
        var viewIndex = 0
        var excludedIndex = 0
        for (i in 0 until backingList.size) {
            if (excludedIndex < excludedIndicesInOrder.size && i == excludedIndicesInOrder[excludedIndex]) {
                excludedIndex++
                continue // Skip excluded indices
            }
            if (backingList[i] == element) {
                return viewIndex
            }
            viewIndex++
        }
        return -1
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var backingListIndex = 0
        private var viewIndex = 0
        private var excludedIndex = 0

        override fun hasNext(): Boolean {
            advanceToNextValid()
            return viewIndex < size
        }

        override fun next(): T {
            advanceToNextValid()
            if (viewIndex >= size) {
                throw NoSuchElementException()
            }
            viewIndex++
            return backingList[backingListIndex++]
        }

        private fun advanceToNextValid() {
            while (backingListIndex < backingList.size && excludedIndex < excludedIndicesInOrder.size && backingListIndex == excludedIndicesInOrder[excludedIndex]) {
                backingListIndex++
                excludedIndex++
            }
        }
    }

    override fun lastIndexOf(element: T): Int {
        var viewIndex = size - 1
        var excludedIndex = excludedIndicesInOrder.size - 1

        for (i in backingList.size - 1 downTo 0) {
            if (excludedIndex >= 0 && i == excludedIndicesInOrder[excludedIndex]) {
                excludedIndex--
                continue // Skip excluded indices
            }
            if (backingList[i] == element) {
                return viewIndex
            }
            viewIndex--
        }
        return -1
    }

    override fun listIterator(): ListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): ListIterator<T> = object : ListIterator<T> {
        private var backingListIndex = 0
        private var viewIndex = 0
        private var excludedIndex = 0

        init {
            // Initialize backingListIndex and excludedIndex to the correct position
            while (viewIndex < index) {
                if (excludedIndex < excludedIndicesInOrder.size && backingListIndex == excludedIndicesInOrder[excludedIndex]) {
                    excludedIndex++
                } else {
                    viewIndex++
                }
                backingListIndex++
            }
        }

        override fun hasNext(): Boolean {
            advanceToNextValid()
            return viewIndex < size
        }

        override fun hasPrevious(): Boolean {
            advanceToPreviousValid()
            return viewIndex >= 0
        }

        override fun next(): T {
            advanceToNextValid()
            if (viewIndex >= size) {
                throw NoSuchElementException()
            }
            viewIndex++
            return backingList[backingListIndex++]
        }

        override fun nextIndex(): Int = viewIndex

        override fun previous(): T {
            advanceToPreviousValid()
            if (viewIndex < 0) {
                throw NoSuchElementException()
            }
            viewIndex--
            return backingList[--backingListIndex]
        }

        override fun previousIndex(): Int = viewIndex - 1

        private fun advanceToNextValid() {
            while (backingListIndex < backingList.size && excludedIndex < excludedIndicesInOrder.size && backingListIndex == excludedIndicesInOrder[excludedIndex]) {
                backingListIndex++
                excludedIndex++
            }
        }

        private fun advanceToPreviousValid() {
            while (backingListIndex > 0 && excludedIndex > 0 && backingListIndex - 1 == excludedIndicesInOrder[excludedIndex - 1]) {
                backingListIndex--
                excludedIndex--
            }
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
        }

        val fromEffective = effectiveIndex(fromIndex)
        val toEffective = effectiveIndex(toIndex)

        val subExcludedIndices = excludedIndicesInOrder.filter { it in fromEffective until toEffective }
            .map { it - fromEffective + excludedIndicesInOrder.count { holeIndex -> holeIndex < fromEffective } }

        return ListWithExclusions(backingList.subList(fromEffective, toEffective), subExcludedIndices)
    }

    fun withoutHoles(): List<T> {
        return backingList.filterIndexed { index, _ -> index !in excludedIndicesInOrder }
    }
}
