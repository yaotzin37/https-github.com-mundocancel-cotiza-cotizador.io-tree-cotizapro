package com.example.api

import com.example.BuildConfig
import com.example.data.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// We use Moshi for JSON mapping since converter-moshi and moshi-kotlin are in our gradle dependencies.
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateProfessionalQuoteInfo(project: Project): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Clave de API de Gemini no configurada. Por favor, añada su GEMINI_API_KEY en el panel de Secrets de AI Studio."
        }

        val prompt = """
            Eres un ingeniero técnico y diseñador de interiores experto en carpintería de aluminio de alta gama y cancelería (Mundo Cancel Pro).
            Por favor, genera un presupuesto detallado y análisis técnico profesional estructurado y estético para el siguiente proyecto:
            
            DATOS DEL CLIENTE:
            - Nombre: ${project.clientName}
            - Teléfono: ${project.clientPhone}
            - Correo: ${project.clientEmail}
            - Dirección de Instalación: ${project.clientAddress}
            
            ESPECIFICACIONES TÉCNICAS DEL TRABAJO:
            - Tipo de Trabajo: ${project.typeOfWork}
            - Medidas: Ancho ${project.width}m | Alto ${project.height}m ${if (project.depth > 0) "| Profundidad de Trabajo Especial: ${project.depth}cm" else ""}
            - Color de la Estructura / Perfil: ${project.color}
            - Tipo de Vidrio/Cristal: ${project.glassType} (${project.glassThickness})
            - Notas y Requerimientos Especiales: ${project.notes}
            
            MONTO BASE GENERAL CALCULADO: $${String.format("%,.2f", project.calculatedBudget)} MXN
            
            Por favor, genera un reporte completo dividido estrictamente en las siguientes secciones (usa subtítulos claros y profesionales sin Markdown pesado, solo texto limpio):
            
            1. RESUMEN EJECUTIVO Y BIENVENIDA
            Una introducción formal y elegante agradeciendo al cliente, detallando el alcance general del trabajo.
            
            2. DESGLOSE DEL PRESUPUESTO PROFESIONAL (ESTIMADO)
            Un desglose detallado de materiales (Perfiles de aluminio, metros cuadrados de vidrio templado, herrajes de acero inoxidable, empaques, selladores) y mano de obra/instalación especializada, alineados coherentemente para sumar un presupuesto total aproximado de $${String.format("%,.2f", project.calculatedBudget)} pesos.
            
            3. ESPECIFICACIONES TÉCNICAS DE INSTALACIÓN
            Detalles sobre el proceso de instalación del aluminio de color ${project.color}, selladores de alta resistencia para evitar filtraciones y el templado/cristal tipo ${project.glassType}.
            
            4. DESCRIPCIÓN VISUAL DEL CROQUIS Y RENDER REALISTA
            Una descripción detallada de cómo luce el diseño final (el "render realista"), la combinación estética del aluminio y vidrio en el espacio real, aportando una visión tridimensional y premium para ayudar al cliente a visualizarlo ya terminado en su hogar o local.
            
            5. RECOMENDACIONES DE MANTENIMIENTO
            Instrucciones para mantener el aluminio y el cristal de ${project.glassType} limpios y en perfecto estado físico y funcional.
            
            Mantén un lenguaje corporativo, servicial, sumamente profesional y detallado. El resultado se incrustará en un PDF formal de cotización de Mundo Cancel Pro listo para ser aprobado por el cliente.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "Eres el asistente inteligente de Mundo Cancel Pro y redactas cotizaciones estéticas, ultra-profesionales, sin caracteres especiales extraños o hashtags innecesarios.")))
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No se pudo obtener el desglose detallado automático del presupuesto."
        } catch (e: Exception) {
            "No se pudo generar la propuesta con IA automáticamente debido a un error de conexión o clave inválida. Presupuesto local base calculado y listo para guardar.\nDetalle: ${e.localizedMessage}"
        }
    }
}
