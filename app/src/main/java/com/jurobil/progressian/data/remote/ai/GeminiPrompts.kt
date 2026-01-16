package com.jurobil.progressian.data.remote.ai

object GeminiPrompts {

    fun buildGamificationPrompt(userGoal: String): String {
        return """
            Actúa como 'Progressian', un arquitecto de vida RPG.
            
            OBJETIVO DEL USUARIO: "$userGoal"
            
            TU TAREA:
            Diseña un plan estratégico dividido en dos partes:
            1. UNA RUTINA RECURRENTE (Tipo: ROUTINE, Frecuencia: DAILY): Acciones pequeñas que el usuario debe repetir diariamente.
            2. UNA LISTA DE HAZAÑAS (Tipo: QUEST, Frecuencia: ONE_TIME): Hitos importantes o logros únicos.
            
            FORMATO JSON ESTRICTO (No incluyas markdown, solo el JSON):
            {
              "routine": {
                "title": "Título épico de la rutina diaria",
                "description": "Descripción motivadora general de la rutina",
                "xp_reward": 50,
                "daily_missions": [
                  { 
                    "title": "Acción concreta 1", 
                    "description": "Explicación breve de qué hacer exactamente", 
                    "xp": 10, 
                    "difficulty": "EASY" 
                  }
                ]
              },
              "quests": [
                {
                  "title": "Hito Importante 1",
                  "description": "Descripción del logro único",
                  "xp_reward": 200,
                  "difficulty": "HARD",
                  "sub_tasks": [
                     { 
                       "title": "Paso 1 para el hito", 
                       "description": "Detalle de este paso", 
                       "xp": 20, 
                       "difficulty": "MEDIUM" 
                     }
                  ]
                }
              ]
            }
        """.trimIndent()
    }
}