package com.enoch02.aptinaija.domain.usecase

import com.enoch02.aptinaija.domain.model.Difficulty
import com.enoch02.aptinaija.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OptionShufflerTest {

    private val shuffler = OptionShuffler()

    @Test
    fun `order is stable for the same seed and question`() {
        val question = question(options = listOf("One", "Two", "Three", "Four"))

        val first = shuffler.orderFor(seed = 42L, question = question)
        val second = shuffler.orderFor(seed = 42L, question = question)

        assertEquals(first, second)
        assertEquals(question.options.indices.toList(), first.sorted())
    }

    @Test
    fun `different seeds can produce different orders`() {
        val question = question(options = listOf("One", "Two", "Three", "Four"))
        val orders = (1L..20L).map { seed -> shuffler.orderFor(seed, question) }.toSet()

        assertNotEquals(1, orders.size)
    }

    @Test
    fun `positional options keep their authored order`() {
        val question = question(
            options = listOf("10", "20", "All of the above", "None of the above")
        )

        assertEquals(listOf(0, 1, 2, 3), shuffler.orderFor(seed = 7L, question = question))
    }

    private fun question(options: List<String>) = Question(
        id = "UBA-NUM-0001",
        company = "UBA",
        section = "Numerical Reasoning",
        difficulty = Difficulty.EASY,
        stem = "Sample stem",
        options = options,
        answerIndex = 0,
        explanation = "Sample explanation"
    )
}
