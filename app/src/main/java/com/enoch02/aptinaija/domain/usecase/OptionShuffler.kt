package com.enoch02.aptinaija.domain.usecase

import com.enoch02.aptinaija.domain.model.Question
import javax.inject.Inject
import kotlin.random.Random

/**
 * Builds a stable option order per question and attempt seed (requirements §4.3).
 * Options that refer to other options ("All of the above") keep their authored order.
 */
class OptionShuffler @Inject constructor() {

    fun orderFor(seed: Long, question: Question): List<Int> {
        val indices = question.options.indices.toList()
        if (indices.size <= 1 || question.options.any { POSITIONAL_OPTION.containsMatchIn(it) }) {
            return indices
        }
        return indices.shuffled(Random(seed xor question.id.hashCode().toLong()))
    }

    private companion object {
        val POSITIONAL_OPTION = Regex(
            """(?i)\b(all|none|both|neither)\b[^.]*\b(of the above|of these|a and|b and|c and)\b"""
        )
    }
}
