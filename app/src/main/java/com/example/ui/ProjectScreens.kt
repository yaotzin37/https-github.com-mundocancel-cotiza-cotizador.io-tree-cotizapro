package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.data.Project
import com.example.ui.theme.*
import com.example.utils.BudgetCalculator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: ProjectViewModel) {
    val context = LocalContext.current
    val state = viewModel.currentScreen

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp).padding(end = 6.dp)
                        )
                        Text(
                            text = "MUNDO CANCEL PRO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    if (state !is Screen.Home) {
                        IconButton(onClick = {
                            if (state is Screen.Wizard && viewModel.wizardStep > 0) {
                                viewModel.wizardStep--
                            } else {
                                viewModel.navigateTo(Screen.Home)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            when (state) {
                is Screen.Home -> ProjectListScreen(viewModel)
                is Screen.Wizard -> ProjectWizardScreen(viewModel)
                is Screen.Details -> ProjectDetailsScreen(viewModel, state.project)
            }
        }
    }
}

@Composable
fun ProjectSummaryDashboard(projects: List<Project>) {
    val totalProjects = projects.size
    val totalValue = projects.sumOf { it.calculatedBudget }
    val formattedValue = String.format(Locale.US, "%,.2f", totalValue)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("summary_dashboard_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Indicador de Rendimiento",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rendimiento y Cotizaciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "MundoCancel Pro",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Total Projects Count
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_projects_count"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Proyectos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalProjects",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "instalaciones",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Metric 2: Total Combined Value
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("dashboard_projects_value"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Monto Total",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$$formattedValue",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "pesos MXN",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectListScreen(viewModel: ProjectViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val context = LocalContext.current

    // Check for draft progress whenever this list starts
    LaunchedEffect(Unit) {
        viewModel.checkForExistingDraft(context)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Real-time summary metrics dashboard
        ProjectSummaryDashboard(projects = projects)

        // Local draft persistence banner
        if (viewModel.hasDraft) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("draft_restore_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Borrador Incompleto Guardado",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            val clientDisp = if (viewModel.draftClientName.isBlank()) "Cliente sin nombre" else viewModel.draftClientName
                            val typeDisp = if (viewModel.draftTypeOfWork.isBlank()) "Cancelería" else viewModel.draftTypeOfWork
                            Text(
                                text = "Para: $clientDisp ($typeDisp)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Modificado: ${viewModel.draftTimestamp}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.deleteDraft(context)
                                Toast.makeText(context, "🗑️ Borrador descartado", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                "Eliminar Borrador", 
                                color = MaterialTheme.colorScheme.error, 
                                fontWeight = FontWeight.SemiBold, 
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.loadDraftFromLocal(context)
                                Toast.makeText(context, "🔄 Borrador reanudado con éxito", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reanudar Edición", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (projects.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Aún no hay cotizaciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Registre su primer proyecto de ventanas o canceles usando el botón de abajo. Calcula presupuestos, crea planos CAD y reportes con Inteligencia Artificial.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Wizard) },
                        modifier = Modifier.testTag("start_wizard_empty_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nueva Cotización")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Proyectos y Cotizaciones Recientes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(projects) { project ->
                        ProjectCard(project = project, onClick = {
                            viewModel.currentScreen = Screen.Details(project)
                        })
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            if (projects.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.navigateTo(Screen.Wizard) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("add_project_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo presupuesto")
                }
            }
        }
    }
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(project.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status Icon representing type of work
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        project.typeOfWork.contains("Baño", true) -> Icons.Default.HotTub
                        project.typeOfWork.contains("Ventana", true) -> Icons.Default.Window
                        project.typeOfWork.contains("Puerta", true) -> Icons.Default.DoorFront
                        project.typeOfWork.contains("Barandal", true) -> Icons.Default.Fence
                        else -> Icons.Default.HomeWork
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.clientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${project.typeOfWork} | ${project.width}m x ${project.height}m",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Aluminio: ${project.color} | Vidrio: ${project.glassType}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("$%,.2f", project.calculatedBudget),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun ProjectWizardScreen(viewModel: ProjectViewModel) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Step Indicator Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stepsList = listOf("Cliente", "Medidas/Specs", "Lugar/Fotos", "Análisis")
            stepsList.forEachIndexed { index, title ->
                val isActive = viewModel.wizardStep >= index
                val isCurrent = viewModel.wizardStep == index

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.wizardStep > index) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                if (index < 3) {
                    Divider(
                        color = if (viewModel.wizardStep > index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier
                            .weight(0.2f)
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }

        // Active Step Content
        Box(modifier = Modifier.weight(1f)) {
            when (viewModel.wizardStep) {
                0 -> StepClientInfo(viewModel)
                1 -> StepSpecsInfo(viewModel)
                2 -> StepImageEvidence(viewModel)
                3 -> StepPreProcessReview(viewModel)
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewModel.wizardStep > 0) {
                OutlinedButton(
                    onClick = { 
                        viewModel.saveDraftToLocal(context)
                        viewModel.wizardStep-- 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Atrás", fontSize = 13.sp)
                }
            }

            // Save Draft manual button
            OutlinedButton(
                onClick = {
                    viewModel.saveDraftToLocal(context)
                    Toast.makeText(context, "📝 Borrador guardado localmente", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).testTag("save_draft_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Guardar", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (viewModel.wizardStep < 3) {
                        // Validation before step increment
                        if (viewModel.wizardStep == 0 && viewModel.clientName.trim().isBlank()) {
                            Toast.makeText(context, "Por favor proporcione el nombre del cliente", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.wizardStep++
                            viewModel.saveDraftToLocal(context)
                        }
                    } else {
                        // Final execution
                        viewModel.processAndSaveProject(context)
                    }
                },
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("wizard_next_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.wizardStep == 3) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                if (viewModel.wizardStep == 3) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Procesar", fontSize = 13.sp)
                } else {
                    Text("Siguiente", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }

    // Processing Dialog Overlay
    if (viewModel.isProcessing) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Procesando Proyecto...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.processingProgress,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepClientInfo(viewModel: ProjectViewModel) {
    var showExplanation by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showExplanation) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Paso 1: Información del Cliente",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ingrese los datos básicos del cliente. Esta información será plasmada en la cabecera formal del reporte en el presupuesto PDF.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { showExplanation = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = viewModel.clientName,
                onValueChange = { viewModel.clientName = it },
                label = { Text("Nombre Completo del Cliente*") },
                placeholder = { Text("Ej: Alejandro González Ruiz") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        item {
            OutlinedTextField(
                value = viewModel.clientPhone,
                onValueChange = { viewModel.clientPhone = it },
                label = { Text("Teléfono de Contacto") },
                placeholder = { Text("Ej: +52 55 1234 5678") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("client_phone_input")
            )
        }

        item {
            OutlinedTextField(
                value = viewModel.clientEmail,
                onValueChange = { viewModel.clientEmail = it },
                label = { Text("Email (Opcional)") },
                placeholder = { Text("Ej: alex.gonzalez@correo.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("client_email_input")
            )
        }

        item {
            OutlinedTextField(
                value = viewModel.clientAddress,
                onValueChange = { viewModel.clientAddress = it },
                label = { Text("Dirección de Obra o Instalación") },
                placeholder = { Text("Ej: Av. Juárez #102, Col. Centro, CDMX") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("client_address_input")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepSpecsInfo(viewModel: ProjectViewModel) {
    val workTypes = listOf(
        "Cancel de Baño Corredizo",
        "Cancel de Baño Templado batiente",
        "Ventana Corrediza Serie 70",
        "Ventana Oscilobatiente Serie 150",
        "Puerta de Acceso Principal",
        "Barandal de Vidrio Templado",
        "Mampara de Vidrio Fijo"
    )

    val aluminumColors = listOf(
        "Aluminio Natural Brillante",
        "Negro Mate Electroestático",
        "Blanco Brillante",
        "Gris Europa Anodizado",
        "Acabado Madera Premium"
    )

    val glassTypes = listOf(
        "Vidrio Claro (Flotado)",
        "Templado Claro",
        "Templado Esmerilado",
        "Tintex Reflectivo",
        "Vidrio Samblasteado (Satinado)"
    )

    val glassThicknesses = listOf(
        "6 milímetros",
        "10 milímetros",
        "12 milímetros"
    )

    var workExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }
    var glassExpanded by remember { mutableStateOf(false) }
    var thicknessExpanded by remember { mutableStateOf(false) }

    var isSpecialWorkChecked by remember { mutableStateOf(viewModel.depthStr != "0" && viewModel.depthStr.isNotEmpty()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown: Tipo de Trabajo
        item {
            ExposedDropdownMenuBox(
                expanded = workExpanded,
                onExpandedChange = { workExpanded = !workExpanded }
            ) {
                OutlinedTextField(
                    value = viewModel.typeOfWork,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Trabajo / Instalación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("type_of_work_select")
                )
                ExposedDropdownMenu(
                    expanded = workExpanded,
                    onDismissRequest = { workExpanded = false }
                ) {
                    workTypes.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                viewModel.typeOfWork = item
                                workExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Row: Ancho and Alto
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.widthStr,
                    onValueChange = { viewModel.widthStr = it },
                    label = { Text("Ancho (metros)") },
                    placeholder = { Text("1.50") },
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("width_input")
                )

                OutlinedTextField(
                    value = viewModel.heightStr,
                    onValueChange = { viewModel.heightStr = it },
                    label = { Text("Alto (metros)") },
                    placeholder = { Text("1.90") },
                    leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("height_input")
                )
            }
        }

        // Special work Options ("opciones para trabajos especiales profundo")
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSpecialWorkChecked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSpecialWorkChecked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trabajo Especial Profundo / Estructural",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSpecialWorkChecked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Active si requiere perfiles de aluminio de mayor profundidad estructural o refuerzos especiales en mampostería.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = isSpecialWorkChecked,
                            onCheckedChange = {
                                isSpecialWorkChecked = it
                                if (!it) viewModel.depthStr = "0"
                            },
                            modifier = Modifier.testTag("special_work_switch")
                        )
                    }

                    if (isSpecialWorkChecked) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.depthStr,
                            onValueChange = { viewModel.depthStr = it },
                            label = { Text("Profundidad de Anclaje de Perfil (cm)") },
                            placeholder = { Text("15") },
                            leadingIcon = { Icon(Icons.Default.Flip, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("depth_input")
                        )
                    }
                }
            }
        }

        // Dropdown: Color de Aluminio
        item {
            ExposedDropdownMenuBox(
                expanded = colorExpanded,
                onExpandedChange = { colorExpanded = !colorExpanded }
            ) {
                OutlinedTextField(
                    value = viewModel.color,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Color de Estructura / Aluminio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("aluminum_color_select")
                )
                ExposedDropdownMenu(
                    expanded = colorExpanded,
                    onDismissRequest = { colorExpanded = false }
                ) {
                    aluminumColors.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                viewModel.color = item
                                colorExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Dropdown: Tipo de Vidrio
        item {
            ExposedDropdownMenuBox(
                expanded = glassExpanded,
                onExpandedChange = { glassExpanded = !glassExpanded }
            ) {
                OutlinedTextField(
                    value = viewModel.glassType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Vidrio / Cristal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = glassExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("glass_type_select")
                )
                ExposedDropdownMenu(
                    expanded = glassExpanded,
                    onDismissRequest = { glassExpanded = false }
                ) {
                    glassTypes.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                viewModel.glassType = item
                                glassExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Dropdown: Espesor de Vidrio
        item {
            ExposedDropdownMenuBox(
                expanded = thicknessExpanded,
                onExpandedChange = { thicknessExpanded = !thicknessExpanded }
            ) {
                OutlinedTextField(
                    value = viewModel.glassThickness,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Espesor del Cristal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = thicknessExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("glass_thickness_select")
                )
                ExposedDropdownMenu(
                    expanded = thicknessExpanded,
                    onDismissRequest = { thicknessExpanded = false }
                ) {
                    glassThicknesses.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                viewModel.glassThickness = item
                                thicknessExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Accesorios y Notas Extra
        item {
            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                label = { Text("Otros Requerimientos / Notas Especiales") },
                placeholder = { Text("Ej: Jaladera de H de 40cm, bisagras hidráulicas de acero inoxidable, empaques negros.") },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("notes_input")
            )
        }
    }
}

@Composable
fun StepImageEvidence(viewModel: ProjectViewModel) {
    val context = LocalContext.current

    // Temp file and uri states for real camera capture
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraFile?.let { file ->
                viewModel.capturedImages.add(file.absolutePath)
                Toast.makeText(context, "Foto capturada e integrada con éxito", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val filename = "photo_real_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, filename)
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.aistudio.mundocancel.fileprovider",
                    file
                )
                tempCameraFile = file
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al preparar cámara: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado. No se pueden capturar fotos reales de obra.", Toast.LENGTH_LONG).show()
        }
    }

    // Launchers for actual physical photos or gallery import!
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addImageUri(context, uri)
        }
    }

    var showCameraSimulationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Paso 3: Evidencia Visual del Lugar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Capture o adjunte imágenes de la mampostería, marcos existentes, baños o áreas donde se instalará. Ayudará a detallar plomos estructurales y niveles.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Captured list grid
            if (viewModel.capturedImages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                        .drawBehind {
                            drawRoundRect(
                                color = LightSecondary.copy(alpha = 0.4f),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            )
                        }
                        .clickable { showCameraSimulationDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin imágenes adjuntas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toque para simular captura de obra",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Áreas Documentadas (${viewModel.capturedImages.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Render horizontal gallery list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    viewModel.capturedImages.forEach { path ->
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            // Site Photo placeholder/mock or actual Coil image
                            AsyncImage(
                                model = path,
                                contentDescription = "Captura de obra",
                                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Glass shine effect overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.15f),
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.05f)
                                              )
                                        )
                                    )
                            )

                            // Interactive Cycle Tag Overlay (Local state categorizer)
                            val currentTag = viewModel.imageTags[path] ?: "Referencia"
                            val nextTag = when (currentTag) {
                                "Referencia" -> "Detalle Plomo"
                                "Detalle Plomo" -> "Mampostería"
                                "Mampostería" -> "Nivel/Suelo"
                                else -> "Referencia"
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .clickable { viewModel.imageTags[path] = nextTag }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (currentTag) {
                                        "Detalle Plomo" -> "📐 Plomo"
                                        "Mampostería" -> "🧱 Muro"
                                        "Nivel/Suelo" -> "📏 Nivel"
                                        else -> "📸 Referencia"
                                    },
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Delete Float Button
                            IconButton(
                                onClick = { viewModel.removeImage(path) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons for Media Capture
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Añadir Documentación de Campo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("real_camera_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara Real", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { showCameraSimulationDialog = true },
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("simulate_camera_button")
                    ) {
                        Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simular", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gallery_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }

    // Camera simulated site dialog (Offline perfect demo support!)
    if (showCameraSimulationDialog) {
        Dialog(onDismissRequest = { showCameraSimulationDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simulador de Captura de Obra",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showCameraSimulationDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                    Text(
                        text = "Seleccione un escenario arquitectónico para simular la captura fotográfica del espacio antes de la instalación:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val simulationScenarios = listOf(
                        Triple("Baño en Obra Gris (Cancel)", android.R.drawable.ic_menu_today, "Mampostería de mármol rústica sin cancel."),
                        Triple("Ventanal de Fachada", android.R.drawable.ic_menu_compass, "Espacio de 2.0m x 2.4m listo para cancel de aluminio."),
                        Triple("Estructura de Oficina", android.R.drawable.ic_menu_agenda, "División industrial con tablaroca lateral."),
                        Triple("Balcón rústico", android.R.drawable.ic_menu_camera, "Terraza lista para recibir barandal de vidrio templado.")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        simulationScenarios.forEach { (title, iconId, desc) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Generate simulated photo
                                        val photoFile = File(context.filesDir, "photo_sim_${java.util.UUID.randomUUID()}.jpg")
                                        // Save a dummy small color rectangle or standard resource
                                        try {
                                            // Write basic data representation for test loading
                                            photoFile.createNewFile()
                                            viewModel.capturedImages.add(photoFile.absolutePath)
                                            Toast.makeText(context, "$title capturado con éxito", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        showCameraSimulationDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconId),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepPreProcessReview(viewModel: ProjectViewModel) {
    val context = LocalContext.current
    val width = viewModel.widthStr.toDoubleOrNull() ?: 1.0
    val height = viewModel.heightStr.toDoubleOrNull() ?: 1.0
    val depth = viewModel.depthStr.toDoubleOrNull() ?: 0.0

    // Local live pricing calculation
    val baseMonto = BudgetCalculator.calculate(
        width = width,
        height = height,
        depth = depth,
        typeOfWork = viewModel.typeOfWork,
        color = viewModel.color,
        glassType = viewModel.glassType,
        glassThickness = viewModel.glassThickness
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Paso 4: Revisión del Proyecto y Croquis Técnico",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Su croquis técnico interactivo ha sido trazado a escala matemática en base a medidas. Un presupuesto preliminar se despliega debajo.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Live Technical Sketch Box
        item {
            var selectedTab by remember { mutableStateOf(0) } // 0 = Croquis CAD, 1 = Boceto Estilizado AI

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Modern Tab Selector for Previews
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).padding(bottom = 8.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        indicator = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent),
                            text = { 
                                Text(
                                    "Vista CAD Estándar", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedTab == 1) MaterialTheme.colorScheme.secondary else Color.Transparent),
                            text = { 
                                Text(
                                    "Boceto Arquitectónico AI", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            }
                        )
                    }

                    if (selectedTab == 0) {
                        // Show traditional Vector CAD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            VectorBlueprintSketch(
                                width = width,
                                height = height,
                                type = viewModel.typeOfWork,
                                color = viewModel.color
                            )
                        }
                    } else {
                        // Show premium generated sketch preview mapped by project type with customized dimension overlays!
                        val sketchResId = when {
                            viewModel.typeOfWork.contains("Baño", true) || viewModel.typeOfWork.contains("Cancel", true) -> R.drawable.img_sketch_bathroom_cancel
                            viewModel.typeOfWork.contains("Ventana", true) -> R.drawable.img_sketch_window
                            viewModel.typeOfWork.contains("Puerta", true) -> R.drawable.img_sketch_door
                            else -> R.drawable.img_sketch_glass_rail
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = sketchResId),
                                contentDescription = "Boceto arquitectónico estilizado para ${viewModel.typeOfWork}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // Overlay translucent layer
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.35f)
                                            )
                                        )
                                    )
                            )

                            // Dimension annotation overlays to make it dynamic
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ) {
                                    Text(
                                        text = "Boceto AI: ${viewModel.typeOfWork}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = "Estructura: Acabado ${viewModel.color}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Dynamic Dimension Anchors represented on preview to fulfill "based on dimensions"
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "📏 ${width}m × ${height}m",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trabajo: ${viewModel.typeOfWork}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tamaño: ${width}m x ${height}m",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quote summary card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRESUPUESTO PRELIMINAR LOCAL (MXN)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estructura Aluminio (${viewModel.color})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(String.format("$%,.2f", baseMonto * 0.25), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cristal (${viewModel.glassType} - ${viewModel.glassThickness})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(String.format("$%,.2f", baseMonto * 0.40), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Accesorios, Selladores y Fijación", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(String.format("$%,.2f", baseMonto * 0.15), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mano de Obra y Plomeo de Precisión", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(String.format("$%,.2f", baseMonto * 0.20), fontSize = 12.sp)
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL ESTIMADO:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = String.format("$%,.2f MXN", baseMonto),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VectorBlueprintSketch(width: Double, height: Double, type: String, color: String) {
    val coreBlue = Color(0xFF1D4ED8)
    val accentBlue = Color(0xFF3B82F6)

    Canvas(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .padding(8.dp)
    ) {
        val maxW = size.width
        val maxH = size.height

        val wFloat = width.toFloat()
        val hFloat = height.toFloat()
        val ratio = wFloat / hFloat
        var finalDrawW = maxW
        var finalDrawH = maxW / ratio

        if (finalDrawH > maxH) {
            finalDrawH = maxH
            finalDrawW = maxH * ratio
        }

        val left = (maxW - finalDrawW) / 2
        val top = (maxH - finalDrawH) / 2
        val right = left + finalDrawW
        val bottom = top + finalDrawH

        // Draw blueprint glass background
        drawRect(
            color = Color(0xFFDBEAFE),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(finalDrawW, finalDrawH)
        )

        // Draw outer thick blueprint aluminum structure
        drawRect(
            color = coreBlue,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(finalDrawW, finalDrawH),
            style = Stroke(width = 3f)
        )

        // Draw inner profile spacing
        drawRect(
            color = coreBlue,
            topLeft = androidx.compose.ui.geometry.Offset(left + 5f, top + 5f),
            size = androidx.compose.ui.geometry.Size(finalDrawW - 10f, finalDrawH - 10f),
            style = Stroke(width = 1f)
        )

        // Draw specific structure based on type
        when {
            type.contains("Corredizo", true) || type.contains("Ventana", true) -> {
                // Two sliding panels. Draw a vertical dividing line
                val midX = left + finalDrawW / 2
                drawLine(
                    color = coreBlue,
                    start = androidx.compose.ui.geometry.Offset(midX, top),
                    end = androidx.compose.ui.geometry.Offset(midX, bottom),
                    strokeWidth = 2f
                )
                // Draw slider arrows
                drawLine(
                    color = accentBlue,
                    start = androidx.compose.ui.geometry.Offset(midX - 25f, top + finalDrawH/2f),
                    end = androidx.compose.ui.geometry.Offset(midX - 5f, top + finalDrawH/2f),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = accentBlue,
                    start = androidx.compose.ui.geometry.Offset(midX - 15f, top + finalDrawH/2f - 4f),
                    end = androidx.compose.ui.geometry.Offset(midX - 5f, top + finalDrawH/2f),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = accentBlue,
                    start = androidx.compose.ui.geometry.Offset(midX - 15f, top + finalDrawH/2f + 4f),
                    end = androidx.compose.ui.geometry.Offset(midX - 5f, top + finalDrawH/2f),
                    strokeWidth = 1.5f
                )
            }
            type.contains("Batiente", true) || type.contains("Puerta", true) -> {
                // Swinging door panel. Draw swinging trajectory arc
                drawLine(
                    color = coreBlue,
                    start = androidx.compose.ui.geometry.Offset(left + 5f, bottom - 5f),
                    end = androidx.compose.ui.geometry.Offset(left + finalDrawW * 0.8f, bottom - finalDrawH * 0.15f),
                    strokeWidth = 2f
                )
                // Display hinges
                drawCircle(color = coreBlue, radius = 2.5f, center = androidx.compose.ui.geometry.Offset(left + 6f, top + 12f))
                drawCircle(color = coreBlue, radius = 2.5f, center = androidx.compose.ui.geometry.Offset(left + 6f, bottom - 12f))
            }
            type.contains("Barandal", true) -> {
                // Balustrade layout with posts
                val step = finalDrawW / 3f
                for (i in 1..2) {
                    val splitX = left + (step * i)
                    drawLine(
                        color = coreBlue,
                        start = androidx.compose.ui.geometry.Offset(splitX, top),
                        end = androidx.compose.ui.geometry.Offset(splitX, bottom),
                        strokeWidth = 2f
                    )
                }
            }
        }

        // Draw white diagnostic light reflect/shine bands
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(left + finalDrawW * 0.15f, top + finalDrawH * 0.3f),
            end = androidx.compose.ui.geometry.Offset(left + finalDrawW * 0.35f, top + finalDrawH * 0.1f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(left + finalDrawW * 0.6f, top + finalDrawH * 0.6f),
            end = androidx.compose.ui.geometry.Offset(left + finalDrawW * 0.8f, top + finalDrawH * 0.4f),
            strokeWidth = 2f
        )
    }
}

@Composable
fun ProjectDetailsScreen(viewModel: ProjectViewModel, project: Project) {
    val context = LocalContext.current
    var selectedVisualTab by remember { mutableStateOf(0) } // 0 = Croquis CAD, 1 = Render 3D, 2 = Side-by-side Comparativa

    var showPdfPreviewDialog by remember { mutableStateOf(false) }
    var actionToTakeAfterPreview by remember { mutableStateOf<((File) -> Unit)?>(null) }
    var generatedPdfFileState by remember { mutableStateOf<File?>(null) }

    // Sharing function
    fun shareQuotePdf(pdfFile: File) {
        val uriStr = "com.aistudio.mundocancel.fileprovider"
        try {
            val uri: Uri = FileProvider.getUriForFile(context, uriStr, pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Enviar presupuesto en PDF listo para aprobación..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir archivo PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Direct Pre-formatted Email Generation to Client
    fun sendEmailQuote(pdfFile: File) {
        val uriStr = "com.aistudio.mundocancel.fileprovider"
        try {
            val uri: Uri = FileProvider.getUriForFile(context, uriStr, pdfFile)
            
            val emailSubject = "Presupuesto de Canceloría - Folio MCP-${1000 + project.id}"
            val emailBody = """
                Hola ${project.clientName},
                
                Es un gusto saludarle de parte de MundoCancel.
                
                Adjunto a este correo encontrará la cotización detallada en formato PDF lista para su revisión y aprobación para su proyecto de cancelería:
                
                • Tipo de Estructura: ${project.typeOfWork}
                • Color de Aluminio: ${project.color}
                • Cristal Templado: ${project.glassType} (${project.glassThickness})
                • Dimensiones Totales: ${project.width} m (ancho) x ${project.height} m (alto)
                • Presupuesto Neto Estimado: $${String.format(Locale.US, "%,.2f", project.calculatedBudget)} pesos MXN
                
                Nuestros precios ya incluyen flete local, colocación profesional, nivelaciones de vano y sellado perimetral contra filtraciones con garantía de satisfacción.
                
                Por favor, confírmenos de recibido o indíquenos si desea proceder con la fabricación y agendar la fecha de toma de muestras final en obra.
                
                Saludos cordiales,
                El equipo de MundoCancel
                MundoCancel Core Systems
            """.trimIndent()

            // Using ACTION_SEND with SELECTOR ACTION_SENDTO mailto: to filter only dedicated Email clients
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(project.clientEmail))
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selector = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                }
            }
            context.startActivity(Intent.createChooser(emailIntent, "Enviar Cotización por Correo al Cliente..."))
        } catch (e: Exception) {
            try {
                // Simplified mailto link fallback
                val mailtoUri = Uri.parse("mailto:${Uri.encode(project.clientEmail)}")
                    .buildUpon()
                    .appendQueryParameter("subject", "Presupuesto de Cancelería - MCP-${1000 + project.id}")
                    .appendQueryParameter("body", "Presupuesto Estimado: $${String.format(Locale.US, "%,.2f", project.calculatedBudget)} pesos MXN.\nConsulte el PDF de cotización adjunto.")
                    .build()
                val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri)
                context.startActivity(Intent.createChooser(emailIntent, "Enviar correo..."))
            } catch (e2: Exception) {
                Toast.makeText(context, "Error al preparar correo: ${e2.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("project_details_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MCP-${1000 + project.id}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(project.timestamp)),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.clientName,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${project.typeOfWork} | ${project.width}m x ${project.height}m",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Section tabs: Croquis Técnico vs Render Realista vs Comparativa
        item {
            TabRow(
                selectedTabIndex = selectedVisualTab,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedVisualTab == 0,
                    onClick = { selectedVisualTab = 0 },
                    text = { Text("Croquis CAD", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { Icon(Icons.Default.Architecture, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = selectedVisualTab == 1,
                    onClick = { selectedVisualTab = 1 },
                    text = { Text("Render 3D", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = selectedVisualTab == 2,
                    onClick = { selectedVisualTab = 2 },
                    text = { Text("Comparar Obra", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    icon = { Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }
        }

        // Selected Visual tab display
        item {
            var selectedImageIndex by remember { mutableStateOf(0) }
            var compareDesignType by remember { mutableStateOf(0) } // 0 = Boceto AI, 1 = Render 3D

            val imagesList = project.imagePaths.split(",").filter { it.isNotEmpty() }

            when (selectedVisualTab) {
                0 -> {
                    // Show Blueprint croquis
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "CROQUIS TÉCNICO DE INSTALACIÓN (Alzados en mm)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                VectorBlueprintSketch(
                                    width = project.width,
                                    height = project.height,
                                    type = project.typeOfWork,
                                    color = project.color
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Show professional render generated!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.modern_cancel_render),
                                contentDescription = "Render realista de aluminio y vidrio templado",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                              )
                            // Gradient shading overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                            )
                            Text(
                                text = "Render Visual de Referencia Académica y Residencial",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            )
                        }
                    }
                }
                2 -> {
                    // Show Interactive Side-by-Side Comparison
                    if (imagesList.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Sin evidencia de sitio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Para comparar fotos de obra con el diseño digital, asegúrate de adjuntar fotos reales de campo en el formulario de levantamiento.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("side_by_side_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ANÁLISIS COMPARATIVO: CAMPO VS DISEÑO",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Options to choose comparison model (Boceto AI or Render 3D)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = compareDesignType == 0,
                                        onClick = { compareDesignType = 0 },
                                        label = { Text("vs Boceto AI", fontSize = 11.sp) },
                                        leadingIcon = if (compareDesignType == 0) {
                                            { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null
                                    )
                                    FilterChip(
                                        selected = compareDesignType == 1,
                                        onClick = { compareDesignType = 1 },
                                        label = { Text("vs Render 3D", fontSize = 11.sp) },
                                        leadingIcon = if (compareDesignType == 1) {
                                            { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null
                                    )
                                }

                                // Split row
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Left: Site Photo
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.05f))
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val indexSafe = if (selectedImageIndex < imagesList.size) selectedImageIndex else 0
                                        val pathAndTag = imagesList[indexSafe]
                                        val parts = pathAndTag.split("|")
                                        val cleanPath = parts[0]
                                        val tag = if (parts.size > 1) parts[1] else "Referencia"

                                        if (cleanPath.substringAfterLast("/").startsWith("photo_sim_")) {
                                            Box(
                                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Obra Simulada", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                                }
                                            }
                                        } else {
                                            AsyncImage(
                                                model = cleanPath,
                                                contentDescription = "Sitio de Obra",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        // Overlay tag text
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.65f))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (tag) {
                                                    "Detalle Plomo" -> "📐 Plomeo / Altura"
                                                    "Mampostería" -> "🧱 Muro Escuadra"
                                                    "Nivel/Suelo" -> "📏 Nivel / Desplante"
                                                    else -> "📸 Referencia de Campo"
                                                },
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Right: Digital proposal
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (compareDesignType == 0) {
                                            val sketchResId = when {
                                                project.typeOfWork.contains("Baño", true) || project.typeOfWork.contains("Cancel", true) -> R.drawable.img_sketch_bathroom_cancel
                                                project.typeOfWork.contains("Ventana", true) -> R.drawable.img_sketch_window
                                                project.typeOfWork.contains("Puerta", true) -> R.drawable.img_sketch_door
                                                else -> R.drawable.img_sketch_glass_rail
                                            }
                                            Image(
                                                painter = painterResource(id = sketchResId),
                                                contentDescription = "Boceto Técnico AI",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f))
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "📐 Boceto Técnico AI",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.modern_cancel_render),
                                                contentDescription = "Render Realista 3D",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "✨ Render Realista 3D",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Index switcher if multiple images exist
                                if (imagesList.size > 1) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                selectedImageIndex = if (selectedImageIndex > 0) selectedImageIndex - 1 else imagesList.size - 1
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.ArrowLeft, contentDescription = "Prev", modifier = Modifier.size(24.dp))
                                        }
                                        Text(
                                            text = "Foto ${selectedImageIndex + 1} de ${imagesList.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                selectedImageIndex = if (selectedImageIndex < imagesList.size - 1) selectedImageIndex + 1 else 0
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.ArrowRight, contentDescription = "Next", modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Client Basic Specs Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "DATOS E INTERIORISMO DEL ESPACIO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Cliente: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                        Text(project.clientName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Teléfono: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                        Text(project.clientPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Correo: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                        Text(project.clientEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Dirección: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                        Text(project.clientAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Aluminio: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                        Text(project.color, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Cristal: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                        Text("${project.glassType} (${project.glassThickness})", fontSize = 12.sp)
                    }
                    if (project.depth > 0) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                            Text("Especial: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                            Text("Estructura profunda de +${project.depth} cm", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Local evidence gallery if exists
        if (project.imagePaths.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "FOTOS REGISTRADAS DE COYUNTURA (" + project.imagePaths.split(",").size + ")",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            project.imagePaths.split(",").forEach { pathAndTag ->
                                val parts = pathAndTag.split("|")
                                val cleanPath = parts[0]
                                val tag = if (parts.size > 1) parts[1] else "Referencia"

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    ) {
                                        if (cleanPath.substringAfterLast("/").startsWith("photo_sim_")) {
                                            // Visual simulated photo template in actual list
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.HomeWork,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            AsyncImage(
                                                model = cleanPath,
                                                contentDescription = "Sitio",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                                                error = painterResource(id = android.R.drawable.ic_menu_gallery)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (tag) {
                                            "Detalle Plomo" -> "📐 Plomo"
                                            "Mampostería" -> "🧱 Muro"
                                            "Nivel/Suelo" -> "📏 Nivel"
                                            else -> "📸 Ref"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed Pricing Proposal card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRESUPUESTO TOTAL NETO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Listo para Autorizar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = String.format("$%,.2f pesos MXN", project.calculatedBudget),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El presupuesto incluye perfiles de unión, plomos, cristales, herrajes de acero inoxidable, flete local e instalación garantizada sin filtraciones.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Gemini AI Detailed Explanation Report
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REPORTE INTELIGENTE DE COTIZACIÓN (IA GEMINI)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val rawAIReport = project.aiQuoteBreakdown
                    if (rawAIReport.isBlank()) {
                        Text(
                            text = "No se ha generado reporte todavía.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        // Display sections in beautifully padded chunks
                        Text(
                            text = rawAIReport,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Action Buttons: Share, Email, Edit, Delete, Back
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.startEditingProject(project)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("edit_project_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar Cotización y Proyecto", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        val file = viewModel.generateAndGetPdf(context, project)
                        generatedPdfFileState = file
                        actionToTakeAfterPreview = { sendEmailQuote(it) }
                        showPdfPreviewDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("email_pdf_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar por Correo al Cliente", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                ElevatedButton(
                    onClick = {
                        val file = viewModel.generateAndGetPdf(context, project)
                        generatedPdfFileState = file
                        actionToTakeAfterPreview = { shareQuotePdf(it) }
                        showPdfPreviewDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("share_pdf_button"),
                    colors = ButtonDefaults.elevatedButtonColors()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir por Otras Vías (WhatsApp, etc.)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.deleteProject(project) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_project_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Eliminar Proyecto de Bitácora")
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showPdfPreviewDialog && generatedPdfFileState != null) {
        PdfPreviewDialog(
            pdfFile = generatedPdfFileState!!,
            project = project,
            onDismiss = {
                showPdfPreviewDialog = false
                actionToTakeAfterPreview = null
            },
            onConfirmActionButton = {
                val file = generatedPdfFileState!!
                showPdfPreviewDialog = false
                actionToTakeAfterPreview?.invoke(file)
                actionToTakeAfterPreview = null
            },
            onAlternativeShare = {
                val file = generatedPdfFileState!!
                showPdfPreviewDialog = false
                shareQuotePdf(file)
                actionToTakeAfterPreview = null
            }
        )
    }
}

@Composable
fun PdfPreviewDialog(
    pdfFile: File,
    project: Project,
    onDismiss: () -> Unit,
    onConfirmActionButton: () -> Unit,
    onAlternativeShare: () -> Unit
) {
    var renderedPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    var isRendering by remember { mutableStateOf(true) }

    LaunchedEffect(pdfFile) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val bitmaps = com.example.utils.PdfGenerator.renderPdfToBitmaps(pdfFile)
            renderedPages = bitmaps
            isRendering = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp)
                .testTag("pdf_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header of preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Previsualización de Cotización",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Folio: MCP-${1000 + project.id} • PDF Generado",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_preview_dialog_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                // Document view viewport area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRendering) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Renderizando documento PDF con precisión...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else if (renderedPages.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No se pudo previsualizar el documento.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // PDF Document container with pages scrollable vertically
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("pdf_pages_list"),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(renderedPages.size) { index ->
                                val bitmap = renderedPages[index]
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Page header tag
                                    Text(
                                        text = "PÁGINA ${index + 1} DE ${renderedPages.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Card(
                                        shape = RoundedCornerShape(4.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        androidx.compose.foundation.Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Página del PDF número ${index + 1}",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // File metadata details panel
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Archivo: Cotizacion_${project.clientName.replace(" ", "_")}.pdf",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val fileSizeKb = pdfFile.length() / 1024
                        Text(
                            text = "Tamaño: ~$fileSizeKb KB • Listo para enviar hoy",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Call to actions at bottom
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("preview_cancel_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Regresar", fontSize = 12.sp)
                    }

                    // Alternative generic share button
                    IconButton(
                        onClick = onAlternativeShare,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .testTag("preview_alternative_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Mas opciones de envío",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onConfirmActionButton,
                        modifier = Modifier
                            .weight(1.8f)
                            .height(44.dp)
                            .testTag("preview_proceed_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar Cotización", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
