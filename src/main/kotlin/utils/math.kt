@file:Suppress("unused")

package utils

import kotlin.experimental.ExperimentalTypeInference
import kotlin.math.absoluteValue

/**
 * Euclid's algorithm for finding the greatest common divisor of a and b.
 */
fun gcd(a: Int, b: Int): Int = if (b == 0) a.absoluteValue else gcd(b, a % b)
fun gcd(f: Int, vararg n: Int): Int = n.fold(f, ::gcd)
fun Iterable<Int>.gcd(): Int = reduce(::gcd)

/**
 * Euclid's algorithm for finding the greatest common divisor of a and b.
 */
fun gcd(a: Long, b: Long): Long = if (b == 0L) a.absoluteValue else gcd(b, a % b)
fun gcd(f: Long, vararg n: Long): Long = n.fold(f, ::gcd)
fun Iterable<Long>.gcd(): Long = reduce(::gcd)

/**
 * Find the least common multiple of a and b using the gcd of a and b.
 */
fun lcm(a: Int, b: Int) = (a safeTimes b) / gcd(a, b)
fun lcm(f: Int, vararg n: Int): Long = n.map { it.toLong() }.fold(f.toLong(), ::lcm)

@JvmName("lcmForInt")
fun Iterable<Int>.lcm(): Long = map { it.toLong() }.reduce(::lcm)

/**
 * Find the least common multiple of a and b using the gcd of a and b.
 */
fun lcm(a: Long, b: Long) = (a safeTimes b) / gcd(a, b)
fun lcm(f: Long, vararg n: Long): Long = n.fold(f, ::lcm)
fun Iterable<Long>.lcm(): Long = reduce(::lcm)

/**
 * Simple algorithm to find the primes of the given Int.
 */
fun Int.primes(): Sequence<Int> = sequence {
    var n = this@primes
    var j = 2
    while (j * j <= n) {
        while (n % j == 0) {
            yield(j)
            n /= j
        }
        j++
    }
    if (n > 1)
        yield(n)
}

/**
 * Simple algorithm to find the primes of the given Long.
 */
fun Long.primes(): Sequence<Long> = sequence {
    var n = this@primes
    var j = 2L
    while (j * j <= n) {
        while (n % j == 0L) {
            yield(j)
            n /= j
        }
        j++
    }
    if (n > 1)
        yield(n)
}

infix fun Int.pow(power: Int): Long {
    require(power >= 0) { "Power must be non-negative" }
    if (power == 0) return 1L
    if (this == 10) return powersOf10L[power]

    var p = power
    var b = this.toLong()
    var res = 1L
    while (p > 0) {
        if (p % 2 == 1) res = res safeTimes b
        p /= 2
        if (p > 0) b = b safeTimes b
    }
    return res
}

fun powerOf10(n: Int): Int = powersOf10[n]
fun powerOf10L(n: Int): Long = powersOf10L[n]

fun Iterable<Long>.product(): Long = reduce(Long::safeTimes)

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun <T> Iterable<T>.productOf(selector: (T) -> Long): Long = fold(1L) { p, n -> p safeTimes selector(n) }

fun Sequence<Long>.product(): Long = reduce(Long::safeTimes)

@JvmName("intProduct")
fun Iterable<Int>.product(): Long = map { it.toLong() }.reduce(Long::safeTimes)

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("intProductOf")
inline fun <T> Iterable<T>.productOf(selector: (T) -> Int): Long = fold(1L) { p, n -> p safeTimes selector(n) }

@JvmName("intProduct")
fun Sequence<Int>.product(): Long = fold(1L, Long::safeTimes)

infix fun Int.safeTimes(other: Int) = (this * other).also { check ->
    require(other == 0 || check / other == this) { "Integer overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Long) = (this * other).also { check ->
    require(other == 0L || check / other == this) { "Long overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Int) = (this * other).also { check ->
    require(other == 0 || check / other == this) { "Long overflow at $this * $other" }
}

infix fun Int.safeTimes(other: Long) = (this.toLong() * other).also { check ->
    require(other == 0L || check / other == this.toLong()) { "Long overflow at $this * $other" }
}

private val powersOf10: Array<Int> =
    arrayOf(1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000)

private val powersOf10L: Array<Long> =
    arrayOf(
        1,
        10,
        100,
        1_000,
        10_000,
        100_000,
        1_000_000,
        10_000_000,
        100_000_000,
        1_000_000_000,
        10_000_000_000,
        100_000_000_000,
        1_000_000_000_000,
        10_000_000_000_000,
        100_000_000_000_000,
        1_000_000_000_000_000,
        10_000_000_000_000_000,
        100_000_000_000_000_000,
        1_000_000_000_000_000_000,
    )

