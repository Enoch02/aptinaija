package com.enoch02.aptinaija.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionCountTest {

    @Test
    fun normalize_keepsCustomCountsInsideSettingsBounds() {
        listOf(5, 10, 15, 20, 30, 40, 100).forEach { count ->
            assertEquals(count, QuestionCount.normalize(count))
        }
    }

    @Test
    fun normalize_clampsCountsOutsideSettingsBounds() {
        assertEquals(QuestionCount.MIN, QuestionCount.normalize(0))
        assertEquals(QuestionCount.MIN, QuestionCount.normalize(-5))
        assertEquals(QuestionCount.MAX, QuestionCount.normalize(150))
    }
}
