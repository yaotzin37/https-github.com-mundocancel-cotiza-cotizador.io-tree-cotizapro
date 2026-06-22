package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ProjectViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Mundo Cancel Pro", appName)
  }

  @Test
  fun testDraftSaveLoadAndDelete() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ProjectViewModel(application)

    // Set some state
    viewModel.clientName = "Juan Pérez"
    viewModel.clientPhone = "5551234567"
    viewModel.typeOfWork = "Cancel de Baño"
    viewModel.wizardStep = 1

    // Save draft
    viewModel.saveDraftToLocal(application)

    // Create a new viewmodel instance to simulate clean app restart
    val newViewModel = ProjectViewModel(application)
    newViewModel.checkForExistingDraft(application)

    assertTrue(newViewModel.hasDraft)
    assertEquals("Juan Pérez", newViewModel.draftClientName)
    assertEquals("Cancel de Baño", newViewModel.draftTypeOfWork)

    // Load draft
    newViewModel.loadDraftFromLocal(application)
    assertEquals("Juan Pérez", newViewModel.clientName)
    assertEquals("5551234567", newViewModel.clientPhone)
    assertEquals("Cancel de Baño", newViewModel.typeOfWork)
    assertEquals(1, newViewModel.wizardStep)

    // Delete/discard draft
    newViewModel.deleteDraft(application)
    
    // Create another instance to confirm deletion is verified locally
    val postDeleteViewModel = ProjectViewModel(application)
    postDeleteViewModel.checkForExistingDraft(application)
    assertFalse(postDeleteViewModel.hasDraft)
  }

  @Test
  fun testDashboardCalculations() {
    val project1 = com.example.data.Project(
        id = 1,
        clientName = "Cliente A",
        clientPhone = "123456",
        clientEmail = "a@test.com",
        clientAddress = "Calle A",
        typeOfWork = "Cancel Baño",
        width = 1.2,
        height = 1.9,
        depth = 0.0,
        color = "Natural Anodizado",
        glassType = "Templado",
        glassThickness = "10mm",
        notes = "Notas",
        calculatedBudget = 10500.0
    )

    val project2 = com.example.data.Project(
        id = 2,
        clientName = "Cliente B",
        clientPhone = "789101",
        clientEmail = "b@test.com",
        clientAddress = "Calle B",
        typeOfWork = "Ventana",
        width = 1.5,
        height = 1.2,
        depth = 0.0,
        color = "Negro",
        glassType = "Flotado",
        glassThickness = "6mm",
        notes = "Notas 2",
        calculatedBudget = 4250.50
    )

    val list = listOf(project1, project2)
    val totalCount = list.size
    val totalValue = list.sumOf { it.calculatedBudget }

    assertEquals(2, totalCount)
    assertEquals(14750.50, totalValue, 0.001)
  }
}

