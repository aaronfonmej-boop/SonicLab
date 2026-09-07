package com.soniclab3d.academic

import com.soniclab3d.cymatics.CymaticsState
import com.soniclab3d.physics.AcousticState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningCatalogTest {
    @Test
    fun catalogMeetsContinuousLearningContract() {
        assertEquals(34, LearningCatalog.guides.size)
        assertEquals(20, LearningCatalog.practices.size)
        assertTrue(LearningCatalog.faq.size >= 60)
        assertEquals(6, LearningPath.entries.size)
        assertTrue(LearningCatalog.validationErrors().isEmpty())
    }

    @Test
    fun versionOnePracticeIdentifiersRemainStable() {
        val inheritedIds = (AcademicPractices.all + CymaticsAcademicPractices.all).map { it.id }.toSet()
        assertEquals(8, inheritedIds.size)
        assertTrue(inheritedIds.all { it in LearningCatalog.practiceIds })
        assertTrue(LearningCatalog.practices.filter { it.id in inheritedIds }.all { it.inheritedFromV1 })
    }

    @Test
    fun faqSearchIgnoresAccentsAndUsesRelatedContent() {
        val results = LearningCatalog.searchFaq("particulas propagacion")
        assertTrue(results.isNotEmpty())
        assertTrue(results.first().question.contains("partículas", ignoreCase = true))
        assertTrue(results.first().relatedGuideIds.contains("g01_oscillation"))
    }

    @Test
    fun presetsPrepareTheCorrectScientificDomain() {
        val air = LearningPresetResolver.applyAir(AcousticState(), "air_interference_phase")
        assertTrue(air.secondarySource.enabled)
        assertTrue(air.showPressureSlice)

        val plate = LearningPresetResolver.applyPlate(CymaticsState(), "plate_coupling")
        assertFalse(plate.automaticMode)
        assertTrue(plate.showNodes)
        assertTrue(plate.showExciter)
    }
}
