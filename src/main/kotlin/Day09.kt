import utils.*

class Day09 : Day(9, 2025, "Movie Theater") {

    val red = input.map { it.extractAllIntegers().let { (x, y) -> Point(x, y) } }

    override fun part1() =
        red.combinations(2).maxOf { (a, b) -> Area.of(a, b).size }

    override fun part2(): Long {
        val outerBound = (red + red.first()).zipWithNext().map { (a, b) -> Area.of(a, b).fixed() }
        log { outerBound }
        val cache = mutableMapOf<Point, Boolean>()
        return red.combinations(2).maxOf { (c1, c2) ->
            val area = Area.of(c1, c2).fixed()
            if ((area.corners() + area.border()).all { p ->
                    cache.getOrPut(p) { isPointInPolygon(p, red) || outerBound.any { p in it } }
                })
                area.width.toLong() safeTimes area.height.toLong()
            else 0L
        }
    }

    /**
     * Prüft, ob ein Punkt innerhalb eines Polygons liegt, mithilfe des Winding-Number-Algorithmus.
     *
     * @param p Der zu testende Punkt (x, y).
     * @param polygon Die Liste der Eckpunkte, die das Polygon bilden.
     * @return True, wenn der Punkt P innerhalb des Polygons liegt (Winding Number != 0), sonst False.
     */
    fun isPointInPolygon(p: Point, polygon: List<Point>): Boolean {
        val n = polygon.size
        if (n < 3) return false // Ein Polygon muss mindestens 3 Punkte haben

        var windingNumber = 0
        val py = p.y

        // Durchlaufe alle Kanten des Polygons
        for (i in 0 until n) {
            // Kante von V[i] nach V[i+1] (oder V[0] für die letzte Kante)
            val V_i = polygon[i]
            val V_i1 = polygon[(i + 1) % n] // Modulo n sorgt für den Sprung zum Startpunkt

            val Vy1 = V_i1.y

            // Prüfe auf "Aufwärtskreuzung" der horizontalen Linie (y = Py)
            if (V_i.y <= py) {
                if (Vy1 > py) {
                    // Die Kante kreuzt die horizontale Linie aufwärts.
                    // Prüfe, ob P links von der Kante liegt.
                    // Der Cross-Product (Kreuzprodukt) gibt Auskunft über die relative Position.

                    if (orientation(V_i, V_i1, p) == Orientation.CLOCKWISE)
                        windingNumber++
                }
            }
            // Prüfe auf "Abwärtskreuzung" der horizontalen Linie (y = Py)
            else {
                if (Vy1 <= py) {
                    // Die Kante kreuzt die horizontale Linie abwärts.
                    // Prüfe, ob P rechts von der Kante liegt.
                    if (orientation(V_i, V_i1, p) == Orientation.COUNTERCLOCKWISE)
                        windingNumber--
                }
            }
        }

        // Wenn die Winding Number ungleich Null ist, ist der Punkt innen.
        return windingNumber != 0
    }

}

fun main() {
    solve<Day09> {
        """
            7,1
            11,1
            11,7
            9,7
            9,5
            2,5
            2,3
            7,3
        """.trimIndent() part1 50 part2 24
    }
}