import utils.*

class Day11 : Day(11, 2025, "Reactor") {

    val devices = input.map { it.split(": ").let { (n, c) -> n to c.split(' ') } }.toMap()

    override fun part1(): Long {

        val cache = mutableMapOf<String, Long>()

        fun waysToOut(from: String): Long = cache.getOrPut(from) {
            if (from == "out") 1L else devices[from]!!.sumOf { waysToOut(it) }
        }

        return waysToOut("you")
    }

    override fun part2(): Long {
        val cache = mutableMapOf<Pair<String, String>, Long>()

        fun waysToOut(from: String, to: String): Long = cache.getOrPut(from to to) {
            if (from == to) 1L else devices[from].orEmpty().sumOf { waysToOut(it, to) }
        }

        val fromSvrToFft = waysToOut("svr", "fft")
        val fromFftToDac = waysToOut("fft", "dac")
        val fromDacToOut = waysToOut("dac", "out")
        val f = listOf(fromSvrToFft, fromFftToDac, fromDacToOut)
        alog {
            "svr to fft: $fromSvrToFft, fft to dac: $fromFftToDac, dac to out: $fromDacToOut"
        }

        val fromSvrToDac = waysToOut("svr", "dac")
        val fromDacToFft = waysToOut("dac", "fft")
        val fromFftToOut = waysToOut("fft", "out")
        val s = listOf(fromSvrToDac, fromDacToFft, fromFftToOut)
        alog {
            "svr to dac: $fromSvrToDac, dac to fft: $fromDacToFft, fft to out: $fromFftToOut"
        }

        return f.product() + s.product()
    }

}

fun main() {
    solve<Day11> {
        """
            aaa: you hhh
            you: bbb ccc
            bbb: ddd eee
            ccc: ddd eee fff
            ddd: ggg
            eee: out
            fff: out
            ggg: out
            hhh: ccc fff iii
            iii: out
        """.trimIndent() part1 5

        """
            svr: aaa bbb
            aaa: fft
            fft: ccc
            bbb: tty
            tty: ccc
            ccc: ddd eee
            ddd: hub
            hub: fff
            eee: dac
            dac: fff
            fff: ggg hhh
            ggg: out
            hhh: out
        """.trimIndent() part2 2
    }
}