package utils

interface Memo2<A, B, R> {
    fun recurse(a: A, b: B): R
}

abstract class Memoized2<A, B, R> {
    private val cache = mutableMapOf<Pair<A, B>, R>()
    private val memo = object : Memo2<A, B, R> {
        override fun recurse(a: A, b: B): R = cache.getOrPut(a to b) {
            function(a, b)
        }
    }

    protected abstract fun Memo2<A, B, R>.function(a: A, b: B): R

    operator fun invoke(a: A, b: B): R = memo.recurse(a, b)
}

fun <A, B, R> (Memo2<A, B, R>.(A, B) -> R).memoize(): (A, B) -> R {
    val memoized = object : Memoized2<A, B, R>() {
        override fun Memo2<A, B, R>.function(a: A, b: B): R = this@memoize(a, b)
    }
    return { a, b -> memoized(a, b) }
}
