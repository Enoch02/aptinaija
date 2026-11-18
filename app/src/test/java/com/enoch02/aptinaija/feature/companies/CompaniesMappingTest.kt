package com.enoch02.aptinaija.feature.companies

import com.enoch02.aptinaija.domain.model.TemplateSection
import com.enoch02.aptinaija.domain.model.TestDefaults
import com.enoch02.aptinaija.domain.model.TestTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompaniesMappingTest {

    @Test
    fun buildCompanyItems_groupsTemplatesByCompany() {
        val items = buildCompanyItems(sampleTemplates())

        assertEquals(2, items.size)
        assertEquals(
            listOf("Cross-industry (Dragnet clients)", "UBA"),
            items.map { it.company }
        )

        val uba = items.single { it.company == "UBA" }
        assertEquals(1, uba.templateCount)
        assertEquals(64, uba.questionCount)
        assertEquals(75, uba.totalMinutes)
        assertEquals(setOf("In-house / MS Forms"), uba.providers)
        assertEquals(
            setOf("Numerical Reasoning", "Verbal Reasoning", "Logical Reasoning"),
            uba.sections
        )

        val dragnet = items.single { it.company.startsWith("Cross-industry") }
        assertEquals(2, dragnet.templateCount)
        assertEquals(95, dragnet.questionCount)
        assertEquals(95, dragnet.totalMinutes)
        assertEquals(setOf("Dragnet"), dragnet.providers)
        assertEquals(4, dragnet.sections.size)
    }

    @Test
    fun buildCompanyItems_groupsCompanyNamesIgnoringCase() {
        val items = buildCompanyItems(
            listOf(
                template("one", "UBA", "In-house", "Numerical Reasoning" to (10 to 10)),
                template("two", "uba", "In-house", "Verbal Reasoning" to (5 to 5))
            )
        )

        assertEquals(1, items.size)
        assertEquals(2, items.single().templateCount)
        assertEquals(15, items.single().questionCount)
    }

    @Test
    fun companyInitials_usesWordsAndHandlesParentheses() {
        assertEquals("UB", companyInitials("UBA"))
        assertEquals("FB", companyInitials("First Bank"))
        assertEquals("CI", companyInitials("Cross-industry (Dragnet clients)"))
        assertEquals("?", companyInitials(""))
    }

    @Test
    fun options_comeFromTemplatesAndAreSorted() {
        val templates = sampleTemplates()

        assertEquals(
            listOf(
                "Abstract Reasoning",
                "Logical Reasoning",
                "Numerical Reasoning",
                "Verbal Reasoning"
            ),
            sectionOptions(templates)
        )
        assertEquals(
            listOf("Dragnet", "In-house / MS Forms"),
            providerOptions(templates)
        )
    }

    @Test
    fun filterCompanies_matchesSectionAndProvider() {
        val items = buildCompanyItems(sampleTemplates())

        assertEquals(
            listOf("Cross-industry (Dragnet clients)"),
            filterCompanies(
                items,
                CompaniesFilters(sections = setOf("Abstract Reasoning"))
            ).map { it.company }
        )
        assertEquals(
            listOf("UBA"),
            filterCompanies(
                items,
                CompaniesFilters(providers = setOf("In-house / MS Forms"))
            ).map { it.company }
        )
        assertEquals(
            listOf("Cross-industry (Dragnet clients)"),
            filterCompanies(
                items,
                CompaniesFilters(
                    sections = setOf("Verbal Reasoning"),
                    providers = setOf("Dragnet")
                )
            ).map { it.company }
        )
        assertTrue(
            filterCompanies(
                items,
                CompaniesFilters(sections = setOf("Banking Awareness"))
            ).isEmpty()
        )
    }

    @Test
    fun filterCompanies_usesOrWithinEachCategoryAndAndAcrossCategories() {
        val items = buildCompanyItems(
            listOf(
                template("alpha", "Alpha", "Provider A", "Numerical Reasoning" to (1 to 1)),
                template("beta", "Beta", "Provider B", "Logical Reasoning" to (1 to 1)),
                template("gamma", "Gamma", "Provider A", "Verbal Reasoning" to (1 to 1))
            )
        )

        assertEquals(
            listOf("Alpha", "Beta"),
            filterCompanies(
                items,
                CompaniesFilters(
                    sections = setOf("Numerical Reasoning", "Logical Reasoning")
                )
            ).map { it.company }
        )
        assertEquals(
            listOf("Alpha", "Beta", "Gamma"),
            filterCompanies(
                items,
                CompaniesFilters(providers = setOf("Provider A", "Provider B"))
            ).map { it.company }
        )
        assertEquals(
            listOf("Alpha", "Gamma"),
            filterCompanies(
                items,
                CompaniesFilters(
                    sections = setOf("Numerical Reasoning", "Verbal Reasoning"),
                    providers = setOf("Provider A")
                )
            ).map { it.company }
        )
    }

    private fun sampleTemplates() = listOf(
        template(
            id = "UBA_GMAP",
            company = "UBA",
            provider = "In-house / MS Forms",
            "Numerical Reasoning" to (22 to 25),
            "Verbal Reasoning" to (22 to 25),
            "Logical Reasoning" to (20 to 25)
        ),
        template(
            id = "DRAGNET_ONLINE",
            company = "Cross-industry (Dragnet clients)",
            provider = "Dragnet",
            "Logical Reasoning" to (20 to 20)
        ),
        template(
            id = "DRAGNET_FULL",
            company = "Cross-industry (Dragnet clients)",
            provider = "Dragnet",
            "Numerical Reasoning" to (25 to 25),
            "Verbal Reasoning" to (25 to 25),
            "Abstract Reasoning" to (25 to 25)
        )
    )

    private fun template(
        id: String,
        company: String,
        provider: String,
        vararg sections: Pair<String, Pair<Int, Int>>
    ) = TestTemplate(
        id = id,
        title = id,
        company = company,
        provider = provider,
        sourceUrl = null,
        defaults = TestDefaults(),
        sections = sections.map { (section, values) ->
            TemplateSection(
                section = section,
                questionCount = values.first,
                minutes = values.second
            )
        }
    )
}
