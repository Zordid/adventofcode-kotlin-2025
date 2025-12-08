import org.junit.jupiter.api.TestFactory

class DaysTest {

    @TestFactory
    fun `AoC 2025`() = aocTests {
        test<Day01>(969, 5887)
        test<Day02>(24747430309, 30962646823)
        test<Day03>(16842, 167523425665348)
        test<Day04>(1356, 8713)
        test<Day05>(773, 332067203034711)
        test<Day06>(4722948564882, 9581313737063)
        test<Day07>(1658, 53916299384254)
        test<Day08>(112230, 2573952864)
//        test<Day09>(<>, <>)
//        test<Day10>(<>, <>)
//        test<Day11>(<>, <>)
//        test<Day12>(<>, <>)
    }

}
