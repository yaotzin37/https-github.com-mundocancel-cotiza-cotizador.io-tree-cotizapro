package com.example.utils

object BudgetCalculator {
    fun calculate(
        width: Double,
        height: Double,
        depth: Double,
        typeOfWork: String,
        color: String,
        glassType: String,
        glassThickness: String
    ): Double {
        val area = width * height
        if (area <= 0) return 0.0

        // Base Glass Cost per square meter (m²)
        val glassPricePerM2 = when (glassType) {
            "Vidrio Claro (Flotado)" -> 650.0
            "Templado Claro" -> 1400.0
            "Templado Esmerilado" -> 1650.0
            "Tintex Reflectivo" -> 1100.0
            "Vidrio Samblasteado (Satinado)" -> 1300.0
            else -> 800.0
        }

        // Glass thickness multiplier
        val thicknessMultiplier = when (glassThickness) {
            "6 milímetros" -> 1.0
            "10 milímetros" -> 1.4
            "12 milímetros" -> 1.7
            else -> 1.0
        }

        // Base Aluminum Profile Cost per square meter (m²) depending on color
        val aluminumPricePerM2 = when (color) {
            "Aluminio Natural Brillante" -> 500.0
            "Negro Mate Electroestático" -> 700.0
            "Blanco Brillante" -> 650.0
            "Gris Europa Anodizado" -> 850.0
            "Acabado Madera Premium" -> 1150.0
            else -> 600.0
        }

        // Work complexity factor
        val complexityFactor = when (typeOfWork) {
            "Cancel de Baño Corredizo" -> 1.2
            "Cancel de Baño Templado batiente" -> 1.4
            "Ventana Corrediza Serie 70" -> 1.1
            "Ventana Oscilobatiente Serie 150" -> 1.5
            "Puerta de Acceso Principal" -> 1.3
            "Barandal de Vidrio Templado" -> 1.6
            "Mampara de Vidrio Fijo" -> 1.0
            else -> 1.2
        }

        val baseMaterialsCost = (glassPricePerM2 * thicknessMultiplier * area) + (aluminumPricePerM2 * area)
        
        // Depth options for special works ("opciones para trabajos especiales profundo")
        val specialDepthCharge = if (depth > 0) {
            // Special deep/structural work. Deep frame structures add raw costs
            1200.0 + (depth * 25.0) 
        } else {
            0.0
        }

        val subtotal = (baseMaterialsCost * complexityFactor) + specialDepthCharge
        
        // Hardware costs
        val standardHardwareDetails = 750.0
        
        // Labor cost (20% of materials + craft)
        val labor = (subtotal + standardHardwareDetails) * 0.20

        return subtotal + standardHardwareDetails + labor
    }
}
