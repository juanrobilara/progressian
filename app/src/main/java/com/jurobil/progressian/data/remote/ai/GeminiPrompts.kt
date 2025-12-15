package com.jurobil.progressian.data.remote.ai

object GeminiPrompts {

    fun buildGamificationPrompt(userGoal: String): String {
        return """
            Actúa como 'Progressian', un motor de Inteligencia Artificial avanzado para gamificación de vida y productividad RPG.
            
            OBJETIVO DEL USUARIO: "$userGoal"
            
            TU TAREA:
            1. Analiza el objetivo y divídelo en una lista de misiones concretas y accionables.
            2. Asigna puntos de experiencia (XP) basados en la dificultad (Easy=10-50, Medium=50-100, Hard=100-300, Epic=500+).
            3. Genera un título épico para el hábito.
            4. Genera una descripción corta inspiradora.
            5. Para la imagen, genera un 'prompt' de texto descriptivo para crear un pixel art de 8-bit.
            
            FORMATO DE RESPUESTA (JSON ÚNICAMENTE):
            Debes responder ESTRICTAMENTE con este esquema JSON y nada más:
            
            {
              "title": "String (Título épico)",
              "description": "String (Descripción motivadora)",
              "pixel_art_prompt": "String (Descripción visual para generar un pixel art estilo retro, ej: 'a pixel art sword glowing blue')",
              "total_xp_reward": Int (Suma de misiones),
              "missions": [
                {
                  "title": "String (Acción concreta)",
                  "description": "String (Detalle breve)",
                  "xp": Int,
                  "difficulty": "EASY" | "MEDIUM" | "HARD" | "EPIC",
                  "icon_emoji": "String (Un solo emoji representativo)"
                }
              ]
            }
        """.trimIndent()
    }
}