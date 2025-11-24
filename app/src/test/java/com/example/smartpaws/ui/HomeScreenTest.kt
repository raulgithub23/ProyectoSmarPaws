package com.example.smartpaws.ui.screen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.smartpaws.data.remote.pets.PetFact
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun petFactCard_displaysFactAndTitle() {
        val fakeFact = PetFact(
            title = "Dato Gatuno",
            fact = "Los gatos duermen 16 horas al día.",
            type = "cat"
        )

        composeTestRule.setContent {
            PetFactCard(
                fact = fakeFact,
                cardColor = Color.White,
                textColor = Color.Black,
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("Dato Gatuno").assertIsDisplayed()
        composeTestRule.onNodeWithText("Los gatos duermen 16 horas al día.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Otro dato").assertIsDisplayed()
    }

    @Test
    fun noAppointmentsCard_showsMessageAndButton_andClickWorks() {
        var isClicked = false

        composeTestRule.setContent {
            NoAppointmentsCard(
                cardColor = Color.White,
                textColor = Color.Black,
                onCreateAppointment = { isClicked = true }
            )
        }

        composeTestRule.onNodeWithText("No tienes citas pendientes").assertIsDisplayed()
        composeTestRule.onNodeWithText("¡Agenda una cita para el cuidado de tu mascota!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agendar Cita").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agendar Cita").performClick()

        assert(isClicked) { "El botón debería haber ejecutado el callback onCreateAppointment" }
    }
}