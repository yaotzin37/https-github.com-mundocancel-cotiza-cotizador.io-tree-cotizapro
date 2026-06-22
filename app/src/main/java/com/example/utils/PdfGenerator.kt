package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.Project
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    
    private fun sanitizeText(input: String?): String {
        if (input == null) return ""
        return input.replace(Regex("[\\r\\t\\n\\u0000-\\u001F\\u007F-\\u009F]"), " ")
            .replace("[", "")
            .replace("]", "")
            .replace("  ", " ")
            .trim()
    }

    private fun cleanMarkdown(input: String?): String {
        if (input == null) return ""
        return input
            .replace("**", "")
            .replace("###", "")
            .replace("##", "")
            .replace("#", "")
            .replace("---", "")
            .replace("___", "")
            .replace(Regex("-{2,}"), "")
            .replace("* ", "• ")
            .replace(Regex("[\\r\\t\\u0000-\\u001F\\u007F-\\u009F]"), " ")
            .replace("  ", " ")
            .trim()
    }

    private fun logDraw(element: String, x: Float, y: Float, description: String = "", paint: Paint? = null) {
        val tag = "PdfGenerator"
        val hexColor = paint?.let { String.format("#%06X", (it.color and 0xFFFFFF)) } ?: "Inherited"
        val textSizeFraction = paint?.textSize ?: 0f
        val details = if (description.isNotEmpty()) "[$description]" else ""
        val msg = "[LOG_RENDER] DrawText -> Element: \"$element\" at (X: $x, Y: $y) | Font: ${textSizeFraction}pt | Color: $hexColor $details"
        android.util.Log.d(tag, msg)
        println("$tag: $msg")
    }

    private fun logRectDraw(element: String, left: Float, top: Float, right: Float, bottom: Float, description: String = "", paint: Paint? = null) {
        val tag = "PdfGenerator"
        val hexColor = paint?.let { String.format("#%06X", (it.color and 0xFFFFFF)) } ?: "Inherited"
        val styleType = paint?.style?.name ?: "FILL"
        val w = right - left
        val h = bottom - top
        val details = if (description.isNotEmpty()) "[$description]" else ""
        val msg = "[LOG_RENDER] DrawRect -> Rectangle: \"$element\" from (L: $left, T: $top) to (R: $right, B: $bottom) | Size: [W: $w, H: $h] | Style: $styleType | Color: $hexColor $details"
        android.util.Log.d(tag, msg)
        println("$tag: $msg")
    }

    private fun logLineDraw(element: String, startX: Float, startY: Float, endX: Float, endY: Float, description: String = "", paint: Paint? = null) {
        val tag = "PdfGenerator"
        val hexColor = paint?.let { String.format("#%06X", (it.color and 0xFFFFFF)) } ?: "Inherited"
        val thick = paint?.strokeWidth ?: 1f
        val details = if (description.isNotEmpty()) "[$description]" else ""
        val msg = "[LOG_RENDER] DrawLine -> Line: \"$element\" from (S_X: $startX, S_Y: $startY) to (E_X: $endX, E_Y: $endY) | Stroke: ${thick}px | Color: $hexColor $details"
        android.util.Log.d(tag, msg)
        println("$tag: $msg")
    }

    fun generateProjectPdf(context: Context, project: Project): File {
        val tag = "PdfGenerator"
        android.util.Log.i(tag, "[PDF_EXEC_TRACE] --- Starting PDF Generation for Project ID: ${project.id} ---")
        println("$tag: [PDF_EXEC_TRACE] --- Starting PDF Generation for Project ID: ${project.id} ---")

        val pdfDocument = PdfDocument()

        // Page size: Letter (612 x 792 points)
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Set up paints
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E3A8A") // Dark Blue
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#0D9488") // Teal Active Accent
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val sectionHeaderPaint = Paint().apply {
            color = Color.parseColor("#1F2937") // Charcoal
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#374151") // Slate
            textSize = 9f
            isAntiAlias = true
        }

        val bodyBoldPaint = Paint().apply {
            color = Color.parseColor("#111827") // Near Black
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#E5E7EB") // Light Grey border
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val blueprintGridPaint = Paint().apply {
            color = Color.parseColor("#EFF6FF") // Ultra Light blue for grid
            style = Paint.Style.FILL
        }

        val blueprintBluePaint = Paint().apply {
            color = Color.parseColor("#1D4ED8") // Blueprint core blue
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        val blueprintGlassPaint = Paint().apply {
            color = Color.parseColor("#DBEAFE") // Very light blue glass fill
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val blueprintLabelPaint = Paint().apply {
            color = Color.parseColor("#1E40AF") // Dark blue for labels
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // Draw decorative top banner
        val bannerPaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 612f, 15f, bannerPaint)
        logRectDraw("Banner Decorativo Superior", 0f, 0f, 612f, 15f, "Fila de color azul oscuro superior", bannerPaint)

        // Title and header
        canvas.drawText("MUNDO CANCEL PRO", 35f, 45f, titlePaint)
        logDraw("MUNDO CANCEL PRO", 35f, 45f, "Título principal de la app", titlePaint)
        
        canvas.drawText("Presupuesto e Ingeniería de Cancelería de Alto Impacto", 35f, 65f, subtitlePaint)
        logDraw("Lema / Subtítulo", 35f, 65f, "Descripción complementaria", subtitlePaint)

        // Document Details Top-Right
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date(project.timestamp))
        canvas.drawText("COTIZACIÓN #: MCP-${1000 + project.id}", 400f, 45f, bodyBoldPaint)
        logDraw("COTIZACIÓN #: MCP-${1000 + project.id}", 400f, 45f, "Folio asignado", bodyBoldPaint)
        
        canvas.drawText("Fecha: $dateString", 400f, 58f, bodyPaint)
        logDraw("Fecha del presupuesto", 400f, 58f, dateString, bodyPaint)
        
        canvas.drawText("Validez: 15 días naturales", 400f, 71f, bodyPaint)
        logDraw("Periodo validez", 400f, 71f, "Vigencia", bodyPaint)

        // Divider
        canvas.drawLine(35f, 80f, 577f, 80f, borderPaint)
        logLineDraw("Divisor Secundaria Header", 35f, 80f, 577f, 80f, "Línea horizontal divisora", borderPaint)

        // CLIENT INFORMATION SECTION
        canvas.drawRect(35f, 90f, 577f, 160f, borderPaint)
        logRectDraw("Contenedor Datos Cliente", 35f, 90f, 577f, 160f, "Marco exterior de datos de cliente", borderPaint)
        
        val sectionTitleBgPaint = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            style = Paint.Style.FILL
        }
        canvas.drawRect(36f, 91f, 576f, 110f, sectionTitleBgPaint)
        logRectDraw("Fondo Título Sección Clientes", 36f, 91f, 576f, 110f, "Fondo sombreado gris", sectionTitleBgPaint)
        
        canvas.drawText("DATOS DEL CLIENTE", 45f, 104f, sectionHeaderPaint)
        logDraw("DATOS DEL CLIENTE", 45f, 104f, "Encabezado sección", sectionHeaderPaint)
 
        canvas.drawText("Nombre:", 45f, 125f, bodyBoldPaint)
        canvas.drawText(sanitizeText(project.clientName), 105f, 125f, bodyPaint)
        logDraw("Nombre del Cliente", 105f, 125f, sanitizeText(project.clientName), bodyPaint)
        
        canvas.drawText("Teléfono:", 45f, 138f, bodyBoldPaint)
        canvas.drawText(sanitizeText(project.clientPhone), 105f, 138f, bodyPaint)
        logDraw("Teléfono Cliente", 105f, 138f, sanitizeText(project.clientPhone), bodyPaint)
        
        canvas.drawText("Correo:", 45f, 151f, bodyBoldPaint)
        canvas.drawText(sanitizeText(project.clientEmail), 105f, 151f, bodyPaint)
        logDraw("Correo Cliente", 105f, 151f, sanitizeText(project.clientEmail), bodyPaint)
 
        canvas.drawText("Dirección:", 340f, 125f, bodyBoldPaint)
        canvas.drawText(sanitizeText(project.clientAddress), 400f, 125f, bodyPaint)
        logDraw("Dirección Cliente", 400f, 125f, sanitizeText(project.clientAddress), bodyPaint)
 
        // SPECIFICATIONS GRID
        canvas.drawRect(35f, 175f, 577f, 260f, borderPaint)
        logRectDraw("Contenedor Especificaciones", 35f, 175f, 577f, 260f, "Grilla exterior de especificaciones", borderPaint)
        
        canvas.drawRect(36f, 176f, 576f, 195f, sectionTitleBgPaint)
        logRectDraw("Fondo Título Especificaciones", 36f, 176f, 576f, 195f, "Fondo sombreado gris encabezado", sectionTitleBgPaint)
        
        canvas.drawText("ESPECIFICACIONES TÉCNICAS DEL PROYECTO", 45f, 189f, sectionHeaderPaint)
        logDraw("ESPECIFICACIONES TÉCNICAS DEL PROYECTO", 45f, 189f, "Título sección", sectionHeaderPaint)
 
        var yPos = 211f
        canvas.drawText("Tipo de Trabajo:", 45f, yPos, bodyBoldPaint)
        canvas.drawText(sanitizeText(project.typeOfWork), 140f, yPos, bodyPaint)
        logDraw("Tipo de Trabajo", 140f, yPos, sanitizeText(project.typeOfWork), bodyPaint)
        
        canvas.drawText("Medidas:", 340f, yPos, bodyBoldPaint)
        canvas.drawText("Ancho ${project.width} m x Alto ${project.height} m", 420f, yPos, bodyPaint)
        logDraw("Medidas (Ancho/Alto)", 420f, yPos, "A: ${project.width}m x Al: ${project.height}m", bodyPaint)
 
        yPos = 227f
        canvas.drawText("Color de Estructura:", 45f, yPos, bodyBoldPaint)
        canvas.drawText(sanitizeText(project.color), 140f, yPos, bodyPaint)
        logDraw("Color Estructura", 140f, yPos, sanitizeText(project.color), bodyPaint)
        
        canvas.drawText("Tipo de Cristal:", 340f, yPos, bodyBoldPaint)
        canvas.drawText("${sanitizeText(project.glassType)} (${sanitizeText(project.glassThickness)})", 420f, yPos, bodyPaint)
        logDraw("Tipo de Cristal", 420f, yPos, "${sanitizeText(project.glassType)} (${sanitizeText(project.glassThickness)})", bodyPaint)
 
        yPos = 243f
        canvas.drawText("Profundidad Especial:", 45f, yPos, bodyBoldPaint)
        canvas.drawText(if (project.depth > 0) "${project.depth} cm" else "N/A (Estándar)", 140f, yPos, bodyPaint)
        logDraw("Profundidad Especial", 140f, yPos, "Prof: ${project.depth}cm", bodyPaint)
        
        canvas.drawText("Otros / Accesorios:", 340f, yPos, bodyBoldPaint)
        canvas.drawText(if (project.notes.isEmpty()) "Ninguna" else sanitizeText(project.notes), 420f, yPos, bodyPaint)
        logDraw("Accesorios / Notas", 420f, yPos, if (project.notes.isEmpty()) "Ninguna" else sanitizeText(project.notes), bodyPaint)
 
        // CROQUIS TÉCNICO / BLUEPRINT DRAWING SECTION
        val bpLeft = 35f
        val bpTop = 270f
        val bpRight = 577f
        val bpBottom = 415f
        
        // Background Grid
        canvas.drawRect(bpLeft, bpTop, bpRight, bpBottom, blueprintGridPaint)
        logRectDraw("Fondo Croquis Plano Técnico", bpLeft, bpTop, bpRight, bpBottom, "Cuadrícula azul del plano", blueprintGridPaint)
        
        // Draw Blueprint Grid Lines (decorative spacing)
        val gridGridPaint = Paint().apply {
            color = Color.parseColor("#E0F2FE")
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        for (gridX in bpLeft.toInt()..bpRight.toInt() step 20) {
            canvas.drawLine(gridX.toFloat(), bpTop, gridX.toFloat(), bpBottom, gridGridPaint)
        }
        for (gridY in bpTop.toInt()..bpBottom.toInt() step 20) {
            canvas.drawLine(bpLeft, gridY.toFloat(), bpRight, gridY.toFloat(), gridGridPaint)
        }
        
        // Draw Title for Blueprint
        canvas.drawText("CROQUIS TÉCNICO ESTRUCTURAL AUTOMÁTICO", bpLeft + 15f, bpTop + 18f, subtitlePaint)
        logDraw("Título del Plano Técnico", bpLeft + 15f, bpTop + 18f, "Texto azul cyan", subtitlePaint)

        // Draw drawing box
        val drawBoxLeft = bpLeft + 120f
        val drawBoxRight = bpRight - 120f
        val drawBoxTop = bpTop + 30f
        val drawBoxBottom = bpBottom - 25f

        // Let's render the physical sketch inside the drawBox based on width and height ratio!
        val widthVal = project.width.toFloat()
        val heightVal = project.height.toFloat()
        val maxPxWidth = drawBoxRight - drawBoxLeft
        val maxPxHeight = drawBoxBottom - drawBoxTop

        val ratio = widthVal / heightVal
        var finalDrawWidth = maxPxWidth
        var finalDrawHeight = maxPxWidth / ratio
        
        if (finalDrawHeight > maxPxHeight) {
            finalDrawHeight = maxPxHeight
            finalDrawWidth = maxPxHeight * ratio
        }

        // Center the drawing in the area
        val cx = drawBoxLeft + (maxPxWidth - finalDrawWidth) / 2
        val cy = drawBoxTop + (maxPxHeight - finalDrawHeight) / 2

        val renderRectLeft = cx
        val renderRectRight = cx + finalDrawWidth
        val renderRectTop = cy
        val renderRectBottom = cy + finalDrawHeight

        android.util.Log.d(tag, "[DIAGRAM_CALC] Outer draw box limits: ($drawBoxLeft, $drawBoxTop) -> ($drawBoxRight, $drawBoxBottom)")
        android.util.Log.d(tag, "[DIAGRAM_CALC] Ideal ratio (W/H): $ratio. Dimensions calculated -> Draw width: $finalDrawWidth, Height: $finalDrawHeight")
        android.util.Log.d(tag, "[DIAGRAM_CALC] Offset offsets -> Center X: $cx, Center Y: $cy")
        android.util.Log.d(tag, "[DIAGRAM_CALC] Render bounding Box: L=$renderRectLeft, T=$renderRectTop, R=$renderRectRight, B=$renderRectBottom")

        // Fill glass area
        canvas.drawRect(renderRectLeft, renderRectTop, renderRectRight, renderRectBottom, blueprintGlassPaint)
        logRectDraw("Área de Vidrio (Vidrio)", renderRectLeft, renderRectTop, renderRectRight, renderRectBottom, "Fondo de panel cristal templado translúcido", blueprintGlassPaint)

        // Draw outer aluminum frame boundary
        canvas.drawRect(renderRectLeft, renderRectTop, renderRectRight, renderRectBottom, blueprintBluePaint)
        logRectDraw("Marco de Aluminio Exterior", renderRectLeft, renderRectTop, renderRectRight, renderRectBottom, "Borde perimetral exterior", blueprintBluePaint)
        
        val innerFramePaint = Paint().apply {
            color = Color.parseColor("#1D4ED8")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(renderRectLeft + 6f, renderRectTop + 6f, renderRectRight - 6f, renderRectBottom - 6f, innerFramePaint)
        logRectDraw("Marco de Aluminio Interior", renderRectLeft+6f, renderRectTop+6f, renderRectRight-6f, renderRectBottom-6f, "Doble línea del marco de aluminio", innerFramePaint)

        // Draw specific divisions matching work type
        when {
            project.typeOfWork.contains("Corredizo", true) || project.typeOfWork.contains("Ventana", true) -> {
                // Two vertical sections (Sliding panels)
                val midX = renderRectLeft + finalDrawWidth / 2f
                canvas.drawLine(midX, renderRectTop, midX, renderRectBottom, blueprintBluePaint)
                logLineDraw("División Corrediza Central", midX, renderRectTop, midX, renderRectBottom, "Línea central vertical", blueprintBluePaint)
                
                canvas.drawRect(midX - 4f, renderRectTop, midX + 4f, renderRectBottom, innerFramePaint)
                logRectDraw("Traslape Corredizo Central", midX-4f, renderRectTop, midX+4f, renderRectBottom, "Barra de traslape", innerFramePaint)
                
                // Sliding direction arrows
                val arrowPaint = Paint().apply {
                    color = Color.parseColor("#1E40AF")
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                }
                canvas.drawLine(midX - 30f, cy + finalDrawHeight/2f, midX - 10f, cy + finalDrawHeight/2f, arrowPaint)
                canvas.drawLine(midX - 15f, cy + finalDrawHeight/2f - 4f, midX - 10f, cy + finalDrawHeight/2f, arrowPaint)
                canvas.drawLine(midX - 15f, cy + finalDrawHeight/2f + 4f, midX - 10f, cy + finalDrawHeight/2f, arrowPaint)
                logLineDraw("Flecha Dirección Deslizamiento", midX - 30f, cy + finalDrawHeight/2f, midX - 10f, cy + finalDrawHeight/2f, "Flecha de panel móvil", arrowPaint)
            }
            project.typeOfWork.contains("Batiente", true) || project.typeOfWork.contains("Puerta", true) -> {
                // Hinged panel. Draw hinges and opening trajectory line
                canvas.drawLine(renderRectLeft, renderRectBottom, renderRectLeft + finalDrawWidth * 0.8f, renderRectBottom - finalDrawHeight * 0.15f, blueprintBluePaint)
                logLineDraw("Trayectoria Abatible Puerta", renderRectLeft, renderRectBottom, renderRectLeft + finalDrawWidth * 0.8f, renderRectBottom - finalDrawHeight * 0.15f, "Batiente simulado", blueprintBluePaint)
                
                canvas.drawCircle(renderRectLeft + 6f, renderRectTop + 15f, 2f, blueprintBluePaint)
                canvas.drawCircle(renderRectLeft + 6f, renderRectBottom - 15f, 2f, blueprintBluePaint)
                logDraw("Bisagras Superior/Inferior", renderRectLeft + 6f, renderRectTop + 15f, "Bisagras circulares simulación técnico", blueprintBluePaint)
            }
            project.typeOfWork.contains("Barandal", true) -> {
                // Balustrade layout with multiple columns/clamps
                val step = finalDrawWidth / 4f
                for (i in 1..3) {
                    val splitX = renderRectLeft + (step * i)
                    canvas.drawLine(splitX, renderRectTop, splitX, renderRectBottom, blueprintBluePaint)
                    logLineDraw("Poste de Soporte Templado #$i", splitX, renderRectTop, splitX, renderRectBottom, "Poste separador de vidrio", blueprintBluePaint)
                }
            }
        }

        // Display diagonal gloss lines on the glass
        val glossPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            strokeWidth = 1f
        }
        canvas.drawLine(renderRectLeft + finalDrawWidth * 0.2f, renderRectTop + finalDrawHeight * 0.3f, renderRectLeft + finalDrawWidth * 0.35f, renderRectTop + finalDrawHeight * 0.15f, glossPaint)
        canvas.drawLine(renderRectLeft + finalDrawWidth * 0.6f, renderRectTop + finalDrawHeight * 0.5f, renderRectLeft + finalDrawWidth * 0.75f, renderRectTop + finalDrawHeight * 0.35f, glossPaint)
        logLineDraw("Brillo Reflejo Cristal 1", renderRectLeft + finalDrawWidth * 0.2f, renderRectTop + finalDrawHeight * 0.3f, renderRectLeft + finalDrawWidth * 0.35f, renderRectTop + finalDrawHeight * 0.15f, "Línea diagonal decorativa blanca", glossPaint)

        // Draw measurements arrows
        val arrowHeadPaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        // Horizontal measurement line at bottom
        val labelY = renderRectBottom + 12f
        canvas.drawLine(renderRectLeft, labelY, renderRectRight, labelY, arrowHeadPaint)
        canvas.drawLine(renderRectLeft, labelY - 3f, renderRectLeft, labelY + 3f, arrowHeadPaint)
        canvas.drawLine(renderRectRight, labelY - 3f, renderRectRight, labelY + 3f, arrowHeadPaint)
        logLineDraw("Línea Acotación Horizontal", renderRectLeft, labelY, renderRectRight, labelY, "Línea de flechas de ancho", arrowHeadPaint)
        
        canvas.drawText("${project.width} m (Ancho Real)", cx + finalDrawWidth / 2f - 35f, labelY + 10f, blueprintLabelPaint)
        logDraw("Etiqueta de Ancho Real", cx + finalDrawWidth / 2f - 35f, labelY + 10f, "${project.width} m", blueprintLabelPaint)

        // Vertical measurement line at left
        val labelX = renderRectLeft - 12f
        canvas.drawLine(labelX, renderRectTop, labelX, renderRectBottom, arrowHeadPaint)
        canvas.drawLine(labelX - 3f, renderRectTop, labelX + 3f, renderRectTop, arrowHeadPaint)
        canvas.drawLine(labelX - 3f, renderRectBottom, labelX + 3f, renderRectBottom, arrowHeadPaint)
        logLineDraw("Línea Acotación Vertical", labelX, renderRectTop, labelX, renderRectBottom, "Línea de flechas de alto", arrowHeadPaint)
        
        // Draw vertical text (rotated conceptually on canvas or just neatly positioned)
        canvas.drawText("${project.height} m", labelX - 35f, cy + finalDrawHeight / 2f + 3f, blueprintLabelPaint)
        canvas.drawText("(Alto)", labelX - 32f, cy + finalDrawHeight / 2f + 13f, blueprintLabelPaint)
        logDraw("Etiqueta de Alto", labelX - 35f, cy + finalDrawHeight / 2f + 3f, "${project.height} m", blueprintLabelPaint)

        // Draw depth if special
        if (project.depth > 0) {
            canvas.drawText("TRABAJO ESPECIAL PROFUNDO: +${project.depth}cm", bpLeft + 15f, bpBottom - 10f, blueprintLabelPaint)
            logDraw("Etiqueta Profundidad Especial", bpLeft + 15f, bpBottom - 10f, "+${project.depth}cm", blueprintLabelPaint)
        }

        // DETAILED COST BREAKDOWN SECTION
        val costTop = 425f
        canvas.drawRect(35f, costTop, 577f, 520f, borderPaint)
        logRectDraw("Contenedor Propuesta Económica", 35f, costTop, 577f, 520f, "Marco tabla desglose de costos", borderPaint)
        
        canvas.drawRect(36f, costTop + 1, 576f, costTop + 18, sectionTitleBgPaint)
        logRectDraw("Fondo Encabezado Propuesta", 36f, costTop + 1, 576f, costTop + 18, "Fondo gris tabla desglose", sectionTitleBgPaint)
        
        canvas.drawText("PROPUESTA ECONÓMICA Y DESGLOSE", 45f, costTop + 13f, sectionHeaderPaint)
        logDraw("PROPUESTA ECONÓMICA Y DESGLOSE", 45f, costTop + 13f, "Título sección", sectionHeaderPaint)

        // Grid contents
        var cY = costTop + 30f
        canvas.drawText("Descripción de Materiales y Labor", 45f, cY, bodyBoldPaint)
        canvas.drawText("Importe Estimado (MXN)", 440f, cY, bodyBoldPaint)
        canvas.drawLine(35f, cY + 4f, 577f, cY + 4f, borderPaint)
        logLineDraw("Divisor Encabezado de Tabla", 35f, cY + 4f, 577f, cY + 4f, "Delimitador columnas tabla", borderPaint)

        cY += 12f
        val descCristal = "Vidrio Cristal Templado / Especial ${sanitizeText(project.glassType)} (${sanitizeText(project.glassThickness)})"
        val val1 = String.format("$%,.2f", project.calculatedBudget * 0.40)
        canvas.drawText(descCristal, 45f, cY, bodyPaint)
        canvas.drawText(val1, 440f, cY, bodyPaint)
        logDraw("Fila Tabla: Vidrio", 45f, cY, "Monto: $val1", bodyPaint)

        cY += 11f
        val descPerfiles = "Suministro de Perfiles de Aluminio de Alta Resistencia - Color ${sanitizeText(project.color)}"
        val val2 = String.format("$%,.2f", project.calculatedBudget * 0.25)
        canvas.drawText(descPerfiles, 45f, cY, bodyPaint)
        canvas.drawText(val2, 440f, cY, bodyPaint)
        logDraw("Fila Tabla: Aluminio", 45f, cY, "Monto: $val2", bodyPaint)

        cY += 11f
        val descHerrajes = "Herrajes, Accesorios de Fijación, Bisagras, Cerraduras y Empaques de Sellado"
        val val3 = String.format("$%,.2f", project.calculatedBudget * 0.15)
        canvas.drawText(descHerrajes, 45f, cY, bodyPaint)
        canvas.drawText(val3, 440f, cY, bodyPaint)
        logDraw("Fila Tabla: Herrajes", 45f, cY, "Monto: $val3", bodyPaint)

        cY += 11f
        val descInstalacion = "Servicio de Instalación Especializada, Nivelado y Flete de Mundo Cancel Pro"
        val val4 = String.format("$%,.2f", project.calculatedBudget * 0.20)
        canvas.drawText(descInstalacion, 45f, cY, bodyPaint)
        canvas.drawText(val4, 440f, cY, bodyPaint)
        logDraw("Fila Tabla: Instalación", 45f, cY, "Monto: $val4", bodyPaint)

        cY += 13f
        canvas.drawLine(35f, cY - 7f, 577f, cY - 7f, borderPaint)
        logLineDraw("Divisor Total Neto", 35f, cY - 7f, 577f, cY - 7f, "Divisor precio total", borderPaint)
        
        val finalPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val totStr = String.format("$%,.2f MXN", project.calculatedBudget)
        canvas.drawText("TOTAL NETO PUNTUAL A PAGAR:", 45f, cY, finalPaint)
        canvas.drawText(totStr, 440f, cY, finalPaint)
        logDraw("Fila Tabla: TOTAL", 45f, cY, "Total: $totStr", finalPaint)

        // ADDITIONAL AI EXPLANATION SECTION
        val aiTop = 532f
        canvas.drawRect(35f, aiTop, 577f, 682f, borderPaint)
        logRectDraw("Contenedor Análisis de Ingeniería", 35f, aiTop, 577f, 682f, "Caja exterior análisis IA/Rendering", borderPaint)
        
        canvas.drawRect(36f, aiTop + 1, 576f, aiTop + 18, sectionTitleBgPaint)
        logRectDraw("Fondo Encabezado Análisis", 36f, aiTop + 1, 576f, aiTop + 18, "Fondo gris tabla análisis", sectionTitleBgPaint)
        
        canvas.drawText("ANÁLISIS DE INGENIERÍA INTELIGENTE & RENDERING EXPLICATIVO", 45f, aiTop + 13f, sectionHeaderPaint)
        logDraw("ANÁLISIS DE INGENIERÍA INTELIGENTE & RENDERING EXPLICATIVO", 45f, aiTop + 13f, "Título sección", sectionHeaderPaint)

        // Wrap lines of aiQuoteBreakdown inside this box
        val textLayoutPaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 8.5f
            isAntiAlias = true
        }
        
        val explanationText = if (project.aiQuoteBreakdown.startsWith("Error:") || project.aiQuoteBreakdown.isEmpty()) {
            "Este proyecto ha sido calculado usando la base de precios oficial de Mundo Cancel Pro. Se han incorporado perfiles de aluminio de línea nacional, selladores elásticos de poliuretano y un cristal flotado templado de alta resistencia. La instalación contempla el flete, plomeo estructural y un perfecto balanceo. Listo para ser fabricado sobre el croquis técnico previo."
        } else {
            project.aiQuoteBreakdown
        }

        // Draw multiple lines safely wrapping text
        var textY = aiTop + 30f
        val cleanExplanation = cleanMarkdown(explanationText).replace("\n", " ")
        val words = cleanExplanation.split(" ")
        var line = StringBuilder()
        var lineCount = 0
        android.util.Log.d(tag, "[TEXT_WRAP_TRACE] Commencing safe wrap process for text of length: ${explanationText.length} chars")
        for (word in words) {
            if (textLayoutPaint.measureText(line.toString() + " " + word) > 520f) {
                if (textY < 675f) {
                    canvas.drawText(line.toString().trim(), 45f, textY, textLayoutPaint)
                    logDraw("Línea Explicativa Wrapped #$lineCount", 45f, textY, "Longitud: ${line.length} chars", textLayoutPaint)
                    textY += 10.5f
                    lineCount++
                    line = StringBuilder()
                } else {
                    android.util.Log.w(tag, "[TEXT_OVERFLOW_WARNING] Text exceeded safe box height limit (675f). Clipping remaining text block.")
                }
                line.append(word).append(" ")
            } else {
                line.append(word).append(" ")
            }
        }
        if (line.isNotEmpty() && textY < 675f) {
            canvas.drawText(line.toString().trim(), 45f, textY, textLayoutPaint)
            logDraw("Línea Explicativa Final #$lineCount", 45f, textY, "Residuo: ${line.length} chars", textLayoutPaint)
        }

        // SIGNATURES AND APPROVAL LINES AT THE VERY BOTTOM ("LISTO PARA SER APROBADO")
        val sigY = 725f
        canvas.drawLine(35f, sigY, 200f, sigY, borderPaint)
        logLineDraw("Línea Firma Autorización", 35f, sigY, 200f, sigY, "Línea de firma izquierda", borderPaint)
        canvas.drawText("Autoriza: Mundo Cancel Pro", 35f, sigY + 12f, bodyBoldPaint)
        logDraw("Texto Autoriza", 35f, sigY + 12f, "Mundo Cancel Pro", bodyBoldPaint)
        
        canvas.drawLine(377f, sigY, 542f, sigY, borderPaint)
        logLineDraw("Línea Firma Conformidad", 377f, sigY, 542f, sigY, "Línea de firma derecha", borderPaint)
        canvas.drawText("Acepta Cliente y Firma de Conformidad", 377f, sigY + 12f, bodyBoldPaint)
        canvas.drawText("Autorizo fabricación con anticipo del 50%", 377f, sigY + 23f, bodyPaint)
        logDraw("Texto Conformidad", 377f, sigY + 12f, "Acepta Cliente", bodyBoldPaint)

        // Footer copyright info
        val footerPaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText("Estudio generado por Mundo Cancel Pro. Las medidas finales serán tomadas en obra previa fabricación.", 120f, 775f, footerPaint)
        logDraw("Footer Página 1", 120f, 775f, "Copyright nota", footerPaint)

        pdfDocument.finishPage(page)
        android.util.Log.i(tag, "[PDF_EXEC_TRACE] Finished Page 1 render.")

        // Page 2: Evidencia de campo (if we have images)
        val imagesList = project.imagePaths.split(",").filter { it.isNotEmpty() }
        if (imagesList.isNotEmpty()) {
            android.util.Log.i(tag, "[PDF_EXEC_TRACE] Found ${imagesList.size} attachment images. Registering page 2...")
            val pageInfo2 = PdfDocument.PageInfo.Builder(612, 792, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            val canvas2 = page2.canvas

            // Page 2 Paint configurations
            val titlePaint2 = Paint().apply {
                color = Color.parseColor("#1E3A8A")
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val subtitlePaint2 = Paint().apply {
                color = Color.parseColor("#0D9488")
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val borderPaint2 = Paint().apply {
                color = Color.parseColor("#E5E7EB")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val textPaint2 = Paint().apply {
                color = Color.parseColor("#4B5563")
                textSize = 10f
                isFakeBoldText = true
                isAntiAlias = true
            }

            // Draw header bar for page 2
            val bannerPaint2 = Paint().apply {
                color = Color.parseColor("#1E3A8A")
                style = Paint.Style.FILL
            }
            canvas2.drawRect(0f, 0f, 612f, 15f, bannerPaint2)
            logRectDraw("[Page2] Banner Decorativo", 0f, 0f, 612f, 15f, "Banner superior", bannerPaint2)
            
            canvas2.drawText("MUNDO CANCEL PRO", 35f, 45f, titlePaint2)
            canvas2.drawText("Anexo Técnico: Evidencia Fotográfica y Levantamiento de Campo", 35f, 62f, subtitlePaint2)
            canvas2.drawLine(35f, 72f, 577f, 72f, borderPaint2)
            logLineDraw("[Page2] Divisor Encabezado", 35f, 72f, 577f, 72f, "", borderPaint2)

            // Render up to 4 images
            val maxPhotos = 4
            val columnWidth = 245f
            val rowHeight = 220f
            
            val cols = 2
            val colSpacing = 22f
            val rowSpacing = 30f
            
            val startX = 45f
            val startY = 100f

            for (i in 0 until Math.min(imagesList.size, maxPhotos)) {
                val pathAndTag = imagesList[i]
                val partSplits = pathAndTag.split("|")
                val cleanPath = partSplits[0]
                val imageTag = if (partSplits.size > 1) partSplits[1] else "Referencia"

                val colIndex = i % cols
                val rowIndex = i / cols

                val xL = startX + colIndex * (columnWidth + colSpacing)
                val xR = xL + columnWidth
                val yT = startY + rowIndex * (rowHeight + rowSpacing)
                val yB = yT + rowHeight

                android.util.Log.d(tag, "[PHOTO_TRACE_Page2] Processing attachment #$i. Path: '$cleanPath', Tag: '$imageTag'")
                android.util.Log.d(tag, "[PHOTO_TRACE_Page2] Frame grid computed: Left=$xL, Top=$yT, Right=$xR, Bottom=$yB")

                // Draw solid background card for photo area
                val cardBgPaint = Paint().apply {
                    color = Color.parseColor("#F9FAFB")
                    style = Paint.Style.FILL
                }
                canvas2.drawRect(xL, yT, xR, yB, cardBgPaint)
                canvas2.drawRect(xL, yT, xR, yB, borderPaint2)
                logRectDraw("[Page2] Borde Foto #$i", xL, yT, xR, yB, "Caja de foto de evidencia", borderPaint2)

                // Decode and draw bitmap safely maintaining aspect ratio without distortion
                val bmp = loadScaledBitmap(cleanPath, 400, 400)
                if (bmp != null) {
                    val maxLeft = xL + 10f
                    val maxTop = yT + 10f
                    val maxRight = xR - 10f
                    val maxBottom = yB - 30f

                    val boxWidth = maxRight - maxLeft
                    val boxHeight = maxBottom - maxTop

                    val bmpWidth = bmp.width.toFloat()
                    val bmpHeight = bmp.height.toFloat()

                    val left: Float
                    val top: Float
                    val right: Float
                    val bottom: Float

                    if (bmpWidth / bmpHeight > boxWidth / boxHeight) {
                        // Image is wider than box aspect ratio: scale to box width and center vertically
                        val newHeight = boxWidth * (bmpHeight / bmpWidth)
                        left = maxLeft
                        top = maxTop + (boxHeight - newHeight) / 2f
                        right = left + boxWidth
                        bottom = top + newHeight
                    } else {
                        // Image is taller than or equal to box aspect ratio: scale to box height and center horizontally
                        val newWidth = boxHeight * (bmpWidth / bmpHeight)
                        left = maxLeft + (boxWidth - newWidth) / 2f
                        top = maxTop
                        right = left + newWidth
                        bottom = top + boxHeight
                    }

                    val destRect = android.graphics.RectF(left, top, right, bottom)
                    canvas2.drawBitmap(bmp, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
                    bmp.recycle()
                    android.util.Log.d(tag, "[PHOTO_TRACE_Page2] Successfully decoded and drew scaled bitmap preserving aspect ratio inside RectF: $destRect")
                } else {
                    // Draw a placeholder icon / error box
                    val errorPaint = Paint().apply {
                        color = Color.parseColor("#D1D5DB")
                        textSize = 24f
                        isAntiAlias = true
                    }
                    canvas2.drawText("📷", xL + columnWidth / 2f - 12f, yT + rowHeight / 2f, errorPaint)
                }

                // Photo Caption text with the matching tag information!
                val displayTag = when (imageTag) {
                    "Detalle Plomo" -> "Plomeo y Medida 📐"
                    "Mampostería" -> "Muro / Estructura 🧱"
                    "Nivel/Suelo" -> "Nivel de Suelo 📏"
                    else -> "Referencia General 📸"
                }
                canvas2.drawText("Foto #${i + 1}: $displayTag", xL + 15f, yB - 10f, textPaint2)
                logDraw("[Page2] Descripcion Foto #${i+1}", xL+15f, yB-10f, displayTag, textPaint2)
            }

            // Footer for Page 2
            val footerPaint2 = Paint().apply {
                color = Color.parseColor("#9CA3AF")
                textSize = 8f
                isAntiAlias = true
            }
            canvas2.drawText("Mundo Cancel Pro - Las imágenes de campo sustentan el levantamiento final y plomeo estructural.", 115f, 755f, footerPaint2)
            canvas2.drawText("Pág 2 de 2", 520f, 755f, footerPaint2)

            pdfDocument.finishPage(page2)
        }

        // Write content to a temporary file
        val pdfFile = File(context.cacheDir, "Cotizacion_${project.clientName.replace(" ", "_").trim()}_${project.id}.pdf")
        val fos = FileOutputStream(pdfFile)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()

        return pdfFile
    }

    private fun loadScaledBitmap(path: String, maxW: Int, maxH: Int): android.graphics.Bitmap? {
        try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeFile(path, options)
            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            var inSampleSize = 1
            if (srcWidth > maxW || srcHeight > maxH) {
                val halfWidth = srcWidth / 2
                val halfHeight = srcHeight / 2
                while ((halfWidth / inSampleSize) >= maxW && (halfHeight / inSampleSize) >= maxH) {
                    inSampleSize *= 2
                }
            }
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            return android.graphics.BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun renderPdfToBitmaps(pdfFile: java.io.File): List<android.graphics.Bitmap> {
        val bitmaps = mutableListOf<android.graphics.Bitmap>()
        try {
            val parcelFileDescriptor = android.os.ParcelFileDescriptor.open(
                pdfFile, 
                android.os.ParcelFileDescriptor.MODE_READ_ONLY
            )
            val pdfRenderer = android.graphics.pdf.PdfRenderer(parcelFileDescriptor)
            val pageCount = pdfRenderer.pageCount
            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                // Use a standard scale factor for high performance preview
                val scale = 2.0f
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                
                val bitmap = android.graphics.Bitmap.createBitmap(
                    width, 
                    height, 
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            pdfRenderer.close()
            parcelFileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmaps
    }
}
