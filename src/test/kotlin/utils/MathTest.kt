package utils

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import java.lang.StrictMath.pow

class MathTest : FunSpec({

    context("Int.pow(Int): Long") {

        test("should return correct results for small powers") {
            (2 pow 0) shouldBeExactly 1L
            (2 pow 1) shouldBeExactly 2L
            (2 pow 2) shouldBeExactly 4L
            (2 pow 3) shouldBeExactly 8L
            (2 pow 10) shouldBeExactly 1024L
            (3 pow 3) shouldBeExactly 27L
            (5 pow 3) shouldBeExactly 125L
        }

        test("should handle 0 and 1 bases correctly") {
            (0 pow 1) shouldBeExactly 0L
            (0 pow 5) shouldBeExactly 0L
            (1 pow 0) shouldBeExactly 1L
            (1 pow 100) shouldBeExactly 1L
            // 0^0 is conventionally 1 in this implementation
            (0 pow 0) shouldBeExactly 1L
        }

        test("should calculate large powers within Long range correctly") {
            // 2^62 is the largest power of 2 that fits in a positive Long
            (2 pow 62) shouldBeExactly 4611686018427387904L

            // 3^39 fits in Long
            (3 pow 39) shouldBeExactly 4052555153018976267L

            // Powers of 10
            (10 pow 18) shouldBeExactly 1_000_000_000_000_000_000L
        }

        test("should throw exception on overflow") {
            shouldThrow<IllegalArgumentException> {
                2 pow 63 // Overflows positive Long
            }.message shouldBe "Long overflow at 2147483648 * 4294967296"

            shouldThrow<IllegalArgumentException> {
                10 pow 19 // Overflows Long
            }

            shouldNotThrow<IllegalArgumentException> {
                (Int.MAX_VALUE pow 2) shouldBe (Int.MAX_VALUE.toBigInteger() * Int.MAX_VALUE.toBigInteger()).longValueExact()// Overflows Long (2147483647^2)
            }
        }

        test("should throw exception for negative power") {
            shouldThrow<IllegalArgumentException> {
                2 pow -1
            }.message shouldBe "Power must be non-negative"
        }

        test("should return correct results for powers of 10") {
            var expected = 1L
            (0..18).toList().forAll {
                10 pow it shouldBeExactly expected
                expected *= 10
            }
        }
    }

    test("powerOf10") {

        for (n in 0..9) {
            powerOf10(n) shouldBeExactly pow(10.0, n.toDouble()).toInt()
        }

        for (n in 0..18) {
            powerOf10L(n) shouldBeExactly pow(10.0, n.toDouble()).toLong()
        }

    }
})
