package utils

val DIGITS = listOf(
    "zero",
    "one",
    "two",
    "three",
    "four",
    "five",
    "six",
    "seven",
    "eight",
    "nine",
)

fun Int.digitToWord(): String = DIGITS.getOrNull(this) ?: error("$this is not a digit")

fun String.wordToDigit(): Int = DIGITS.indexOf(lowercase()).also {
    if (it == -1) error("$this is not a digit")
}
