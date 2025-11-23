package utils

fun String.findMatchingClosingBracket(startIndex: Int = 0, open: Char = '[', close: Char = ']'): Int {
    require(get(startIndex) == open) {
        "Given string does not start with opening bracket $open: ${this.drop(startIndex)}!"
    }
    var level = 1
    (startIndex + 1..lastIndex).forEach { index ->
        when (get(index)) {
            open -> level++
            close -> level--
        }
        if (level == 0) return index
    }
    error("No matching closing bracket found in ${this.drop(startIndex)}.")
}

fun String.tokenize(delimiters: String, startIndex: Int = 0): Sequence<String> = sequence {
    var idx = startIndex.coerceAtLeast(0)
    while (idx <= lastIndex) {
        var nextToken = idx
        while (nextToken <= lastIndex && get(nextToken) !in delimiters) {
            nextToken++
        }
        if (nextToken > lastIndex) break // ran till the end of the string

        // token at exactly nextToken position found!
        if (nextToken > idx) yield(substring(idx, nextToken + 1))
        yield(substring(nextToken, nextToken + 1))
        idx = nextToken + 1
    }
    if (idx <= lastIndex)
        yield(substring(idx))
}