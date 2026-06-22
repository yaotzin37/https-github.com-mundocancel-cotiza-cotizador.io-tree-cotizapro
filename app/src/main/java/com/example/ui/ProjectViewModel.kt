package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.Project
import com.example.data.ProjectRepository
import com.example.utils.BudgetCalculator
import com.example.utils.PdfGenerator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

sealed class Screen {
    object Home : Screen()
    object Wizard : Screen()
    data class Details(val project: Project) : Screen()
}

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository

    val allProjects: StateFlow<List<Project>>

    // Navigation state
    var currentScreen by mutableStateOf<Screen>(Screen.Home)

    // Current wizard step index (0 to 3)
    var wizardStep by mutableStateOf(0)

    // Form states
    var clientName by mutableStateOf("")
    var clientPhone by mutableStateOf("")
    var clientEmail by mutableStateOf("")
    var clientAddress by mutableStateOf("")

    var typeOfWork by mutableStateOf("Cancel de Baño Corredizo")
    var widthStr by mutableStateOf("1.50")
    var heightStr by mutableStateOf("1.90")
    var depthStr by mutableStateOf("0")

    var color by mutableStateOf("Aluminio Natural Brillante")
    var glassType by mutableStateOf("Templado Claro")
    var glassThickness by mutableStateOf("10 milímetros")
    var notes by mutableStateOf("")

    // List of local image paths for the active wizard
    val capturedImages = mutableStateListOf<String>()
    val imageTags = androidx.compose.runtime.mutableStateMapOf<String, String>()

    // Editing State
    var editingProjectId by mutableStateOf<Int?>(null)

    fun startEditingProject(project: Project) {
        editingProjectId = project.id
        clientName = project.clientName
        clientPhone = project.clientPhone
        clientEmail = project.clientEmail
        clientAddress = project.clientAddress
        typeOfWork = project.typeOfWork
        widthStr = project.width.toString()
        heightStr = project.height.toString()
        depthStr = project.depth.toString()
        color = project.color
        glassType = project.glassType
        glassThickness = project.glassThickness
        notes = project.notes
        
        capturedImages.clear()
        imageTags.clear()
        
        if (project.imagePaths.isNotEmpty()) {
            project.imagePaths.split(",").filter { it.isNotEmpty() }.forEach { entry ->
                val parts = entry.split("|")
                if (parts.isNotEmpty()) {
                    val path = parts[0]
                    val tag = if (parts.size > 1) parts[1] else "Referencia"
                    capturedImages.add(path)
                    imageTags[path] = tag
                }
            }
        }
        
        wizardStep = 0
        currentScreen = Screen.Wizard
    }

    // Loading & Operation States
    var isProcessing by mutableStateOf(false)
    var processingProgress by mutableStateOf("")
    var generatedPdfFile by mutableStateOf<File?>(null)

    // Draft states for local persistence
    var hasDraft by mutableStateOf(false)
    var draftClientName by mutableStateOf("")
    var draftTypeOfWork by mutableStateOf("")
    var draftTimestamp by mutableStateOf("")

    fun checkForExistingDraft(context: Context) {
        val prefs = context.getSharedPreferences("cancel_projects_drafts", Context.MODE_PRIVATE)
        hasDraft = prefs.getBoolean("has_draft", false)
        if (hasDraft) {
            draftClientName = prefs.getString("clientName", "") ?: ""
            draftTypeOfWork = prefs.getString("typeOfWork", "Proyecto sin guardado de tipo") ?: ""
            val ts = prefs.getLong("timestamp", 0L)
            draftTimestamp = if (ts > 0L) {
                val date = java.util.Date(ts)
                val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                formatter.format(date)
            } else ""
        }
    }

    fun saveDraftToLocal(context: Context) {
        val prefs = context.getSharedPreferences("cancel_projects_drafts", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("has_draft", true)
            putInt("wizardStep", wizardStep)
            putString("clientName", clientName)
            putString("clientPhone", clientPhone)
            putString("clientEmail", clientEmail)
            putString("clientAddress", clientAddress)
            putString("typeOfWork", typeOfWork)
            putString("widthStr", widthStr)
            putString("heightStr", heightStr)
            putString("depthStr", depthStr)
            putString("color", color)
            putString("glassType", glassType)
            putString("glassThickness", glassThickness)
            putString("notes", notes)
            putString("capturedImages", capturedImages.joinToString(","))
            
            val tagsStr = imageTags.entries.joinToString(";") { "${it.key}|${it.value}" }
            putString("imageTags", tagsStr)
            putLong("timestamp", System.currentTimeMillis())
            apply()
        }
        hasDraft = true
        draftClientName = clientName
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        draftTimestamp = formatter.format(java.util.Date())
    }

    fun loadDraftFromLocal(context: Context) {
        val prefs = context.getSharedPreferences("cancel_projects_drafts", Context.MODE_PRIVATE)
        if (prefs.getBoolean("has_draft", false)) {
            wizardStep = prefs.getInt("wizardStep", 0)
            clientName = prefs.getString("clientName", "") ?: ""
            clientPhone = prefs.getString("clientPhone", "") ?: ""
            clientEmail = prefs.getString("clientEmail", "") ?: ""
            clientAddress = prefs.getString("clientAddress", "") ?: ""
            typeOfWork = prefs.getString("typeOfWork", "Cancel de Baño Corredizo") ?: "Cancel de Baño Corredizo"
            widthStr = prefs.getString("widthStr", "1.50") ?: "1.50"
            heightStr = prefs.getString("heightStr", "1.90") ?: "1.90"
            depthStr = prefs.getString("depthStr", "0") ?: "0"
            color = prefs.getString("color", "Aluminio Natural Brillante") ?: "Aluminio Natural Brillante"
            glassType = prefs.getString("glassType", "Templado Claro") ?: "Templado Claro"
            glassThickness = prefs.getString("glassThickness", "10 milímetros") ?: "10 milímetros"
            notes = prefs.getString("notes", "") ?: ""
            
            capturedImages.clear()
            val savedImages = prefs.getString("capturedImages", "") ?: ""
            if (savedImages.isNotEmpty()) {
                capturedImages.addAll(savedImages.split(","))
            }

            imageTags.clear()
            val savedTags = prefs.getString("imageTags", "") ?: ""
            if (savedTags.isNotEmpty()) {
                savedTags.split(";").forEach { item ->
                    val parts = item.split("|")
                    if (parts.size == 2) {
                        imageTags[parts[0]] = parts[1]
                    }
                }
            }
            
            currentScreen = Screen.Wizard
        }
    }

    fun deleteDraft(context: Context) {
        val prefs = context.getSharedPreferences("cancel_projects_drafts", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        hasDraft = false
        draftClientName = ""
        draftTimestamp = ""
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProjectRepository(database.projectDao())
        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        checkForExistingDraft(application)

        @kotlin.OptIn(kotlinx.coroutines.FlowPreview::class)
        viewModelScope.launch {
            snapshotFlow {
                listOf(
                    clientName,
                    clientPhone,
                    clientEmail,
                    clientAddress,
                    typeOfWork,
                    widthStr,
                    heightStr,
                    depthStr,
                    color,
                    glassType,
                    glassThickness,
                    notes,
                    wizardStep.toString(),
                    capturedImages.toList().toString(),
                    imageTags.toMap().toString()
                )
            }
            .debounce(1500L)
            .collectLatest {
                if (currentScreen is Screen.Wizard) {
                    android.util.Log.d("ProjectViewModel", "[AUTO_SAVE_TRACE] Debounced auto-save triggered.")
                    saveDraftToLocal(application)
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        if (screen is Screen.Wizard) {
            resetWizard()
        }
    }

    private fun resetWizard() {
        wizardStep = 0
        editingProjectId = null
        clientName = ""
        clientPhone = ""
        clientEmail = ""
        clientAddress = ""
        typeOfWork = "Cancel de Baño Corredizo"
        widthStr = "1.50"
        heightStr = "1.90"
        depthStr = "0"
        color = "Aluminio Natural Brillante"
        glassType = "Templado Claro"
        glassThickness = "10 milímetros"
        notes = ""
        capturedImages.clear()
        imageTags.clear()
        isProcessing = false
        processingProgress = ""
        generatedPdfFile = null
    }

    fun addMockCapturedImage(context: Context) {
        // Since we are in simulation environment, user can simulate taking high-quality site photo
        // Create a beauty placeholder image
        val filename = "photo_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        
        // Let's create an empty mock photo or copy assets if available, but simple local representation is perfect
        viewModelScope.launch {
            try {
                context.assets.open("photo_site_mock.jpg").use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                // If asset doesn't exist, just create a solid empty placeholder or write a small signature file
                file.createNewFile()
            }
            capturedImages.add(file.absolutePath)
        }
    }

    fun addImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val filename = "photo_${UUID.randomUUID()}.jpg"
                    val file = File(context.filesDir, filename)
                    FileOutputStream(file).use { output ->
                        inputStream.copyTo(output)
                    }
                    capturedImages.add(file.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeImage(path: String) {
        capturedImages.remove(path)
        imageTags.remove(path)
        try {
            val file = File(path)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun processAndSaveProject(context: Context) {
        isProcessing = true
        processingProgress = "Calculando cotización matemática precisa..."
        
        viewModelScope.launch {
            val width = widthStr.toDoubleOrNull() ?: 1.0
            val height = heightStr.toDoubleOrNull() ?: 1.0
            val depth = depthStr.toDoubleOrNull() ?: 0.0

            val budget = BudgetCalculator.calculate(
                width = width,
                height = height,
                depth = depth,
                typeOfWork = typeOfWork,
                color = color,
                glassType = glassType,
                glassThickness = glassThickness
            )

            processingProgress = "Redactando reporte de ingeniería con IA (Gemini)..."

            val imagesJoined = capturedImages.joinToString(",") { path ->
                val tag = imageTags[path] ?: "Referencia"
                "$path|$tag"
            }

            // Dummy project for prompt
            var tempProject = Project(
                id = editingProjectId ?: 0,
                clientName = clientName.ifBlank { "Cliente Sin Nombre" },
                clientPhone = clientPhone.ifBlank { "N/A" },
                clientEmail = clientEmail.ifBlank { "N/A" },
                clientAddress = clientAddress.ifBlank { "En Obra" },
                typeOfWork = typeOfWork,
                width = width,
                height = height,
                depth = depth,
                color = color,
                glassType = glassType,
                glassThickness = glassThickness,
                notes = notes,
                imagePaths = imagesJoined,
                calculatedBudget = budget
            )

            // Get AI quote breakdown
            val aiResponse = GeminiClient.generateProfessionalQuoteInfo(tempProject)
            tempProject = tempProject.copy(aiQuoteBreakdown = aiResponse)

            processingProgress = "Guardando proyecto y generando firma digital en base de datos..."
            val finalId: Int
            if (editingProjectId != null) {
                repository.updateProject(tempProject)
                finalId = editingProjectId!!
            } else {
                val insertedId = repository.saveProject(tempProject)
                finalId = insertedId.toInt()
            }
            
            processingProgress = "Compilando documento PDF y Croquis técnico..."
            val finalProject = tempProject.copy(id = finalId)
            
            try {
                val pdfFile = PdfGenerator.generateProjectPdf(context, finalProject)
                generatedPdfFile = pdfFile
            } catch (e: Exception) {
                e.printStackTrace()
            }

            deleteDraft(context)
            editingProjectId = null
            isProcessing = false
            currentScreen = Screen.Details(finalProject)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            currentScreen = Screen.Home
        }
    }

    fun generateAndGetPdf(context: Context, project: Project): File {
        return PdfGenerator.generateProjectPdf(context, project)
    }
}
