package utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import java.lang.StrictMath.pow

class MathTest : FunSpec({

    test("powerOf10") {

        for (n in 0..9) {
            powerOf10(n) shouldBeExactly pow(10.0, n.toDouble()).toInt()
        }

        for (n in 0..18) {
            powerOf10L(n) shouldBeExactly pow(10.0, n.toDouble()).toLong()
        }

    }
})
