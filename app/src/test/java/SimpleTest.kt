import org.junit.Test
import org.junit.Assert.*

class SimpleTest {
    @Test
    fun basicMathTest() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun stringTest() {
        val text = "Hello World"
        assertTrue(text.contains("World"))
    }

    @Test
    fun simpleLogicTest() {
        val numbers = listOf(1, 2, 3)
        assertEquals(3, numbers.size)
    }
}