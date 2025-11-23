@file:Suppress("unused")

package utils

import kotlin.math.absoluteValue
import kotlin.math.pow

fun powerOf10(n: Int): Int = powersOf10[n]
fun powerOf10L(n: Int): Long = powersOf10L[n]

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

infix fun Number.pow(power: Number): Double =
    toDouble().pow(power.toDouble())

infix fun Int.pow(power: Int): Long =
    toDouble().pow(power.toDouble()).let {
        require(it <= Long.MAX_VALUE.toDouble()) { "$this to the power of $power exceeds Long range" }
        it.toLong()
    }

infix fun Int.safeTimes(other: Int) = (this * other).also {
    require(other == 0 || it / other == this) { "Integer overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Long) = (this * other).also {
    require(other == 0L || it / other == this) { "Long overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Int) = (this * other).also {
    require(other == 0 || it / other == this) { "Long overflow at $this * $other" }
}

infix fun Int.safeTimes(other: Long) = (this.toLong() * other).also {
    require(other == 0L || it / other == this.toLong()) { "Long overflow at $this * $other" }
}
