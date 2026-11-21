package com.enoch02.aptinaija.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelDestinationTest {

    @Test
    fun bookmarkAction_isEnabledForHomeCompaniesAndWrong() {
        assertEquals(
            listOf(
                TopLevelDestination.Home,
                TopLevelDestination.Companies,
                TopLevelDestination.Wrong
            ),
            TopLevelDestination.entries.filter { it.showBookmarksAction }
        )
    }

    @Test
    fun destinations_areOrderedForTheBottomBar() {
        assertEquals(
            listOf("Home", "Companies", "History", "Wrong", "Settings"),
            TopLevelDestination.entries.map { it.name }
        )
    }
}
