package utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll

class CombinatoricsTest : FunSpec({

    context("choose function") {
        test("anything choose 0 has 1 solution") {
            checkAll(Arb.int(0..Int.MAX_VALUE)) { n ->
                n choose 0 shouldBeExactly 1
            }
        }

        test("anything choose (more) has 0 solution") {
            checkAll(Arb.int(0..1000000), Arb.int(1..10)) { n, delta ->
                n choose (n + delta) shouldBeExactly 0
            }
        }

        test("symmetry test") {
            val nKArb = Arb.nk()
            checkAll(nKArb) { (n, k) ->
                n choose k shouldBeExactly (n choose (n - k))
            }
        }

        test("n choose k") {
            5 choose 3 shouldBeExactly 10
            5 choose 2 shouldBeExactly 10
        }
    }

    test("simple combinations of 2 elements") {
        "a".combinations(2).toList().shouldBeEmpty()
        "ab".combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    "ab"
                )
        "abc".combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    "ab",
                    "ac",
                    "bc",
                )
        "abcd".combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    "ab",
                    "ac",
                    "ad",
                    "bc",
                    "bd",
                    "cd",
                )
        "abcd".combinations(3).toList() shouldContainExactlyInAnyOrder
                listOf("abc", "abd", "acd", "bcd")
        "abcde".combinations(4).toList() shouldContainExactlyInAnyOrder
                listOf("abcd", "abce", "abde", "acde", "bcde")
    }

    test("simple combinations of IntRanges") {
        (1..1).combinations(2).toList().shouldBeEmpty()
        (1..2).combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(1, 2)
                )
        (1..3).combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(1, 2),
                    listOf(1, 3),
                    listOf(2, 3),
                )
        (1..4).combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(1, 2),
                    listOf(1, 3),
                    listOf(1, 4),
                    listOf(2, 3),
                    listOf(2, 4),
                    listOf(3, 4),
                )
    }

    test("test permutations of elements") {
        "".permutations().toList().shouldBeEmpty()

        "a".permutations().toList() shouldContainExactlyInAnyOrder
                listOf("a")

        "ab".permutations().toList() shouldContainExactlyInAnyOrder
                listOf("ab", "ba")

        "aa".permutations().toList() shouldContainExactlyInAnyOrder
                listOf("aa", "aa")

        "abc".permutations().toList() shouldContainExactlyInAnyOrder
                listOf(
                    "abc",
                    "acb",
                    "bac",
                    "cab",
                    "bca",
                    "cba"
                )
    }

    test("test permutations of IntRange") {
        @Suppress("EmptyRange")
        (1..0).permutations().toList().shouldBeEmpty()

        (10..10).permutations().toList() shouldContainExactlyInAnyOrder
                listOf(listOf(10))

        (41..42).permutations().toList() shouldContainExactlyInAnyOrder
                listOf(listOf(41, 42), listOf(42, 41))

        (100..102).permutations().toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(100, 101, 102),
                    listOf(100, 102, 101),
                    listOf(101, 100, 102),
                    listOf(102, 100, 101),
                    listOf(101, 102, 100),
                    listOf(102, 101, 100),
                )
    }

    test("ListWithExcluded") {
        checkAll(Arb.nk(0..100)) { (n, k) ->
            val backing = List(n) { it }
            val exclude = List(k) { Arb.int(backing.indices).bind() }.toSet()
            val expected = buildList {
                backing.indices.forEach { idx -> if (idx !in exclude) add(backing[idx]) }
            }
            val actual = backing.withRemoved(exclude)
            actual shouldBe expected
            @Suppress("RedundantAsSequence")
            actual.asSequence().toList() shouldBe expected
            actual.listIterator()
        }
    }

})

private fun Arb.Companion.nk(nRange: IntRange = 0..1_000_000): Arb<Pair<Int, Int>> = arbitrary {
    val n = Arb.int(nRange).bind()
    val k = Arb.int(0..n).bind()
    n to k
}

fun main() {
    val b = listOf(1, 2, 3, 4, 5)
    val h = b.withRemoved(setOf(1, 3))
    println(h[4])
}