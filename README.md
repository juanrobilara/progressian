# Progressian

Aplicación Android nativa desarrollada en Kotlin y Jetpack Compose que gamifica la gestión de hábitos y productividad. El proyecto utiliza Inteligencia Artificial (Google Gemini) para transformar objetivos de usuario en planes de acción estructurados con mecánicas RPG (misiones, experiencia y niveles).

## Descripción General
Progressian combina conceptos de productividad con sistemas de progresión de videojuegos. Los usuarios ingresan un objetivo en lenguaje natural y el sistema genera automáticamente un hábito con misiones específicas, dificultad variable y recompensas de XP.

El proyecto implementa una arquitectura Clean Architecture con MVVM, priorizando la separación de responsabilidades y la escalabilidad.

### Stack Tecnológico
- Lenguaje: Kotlin

- UI: Jetpack Compose (Material3)

- Arquitectura: Clean Architecture + MVVM

- Inyección de Dependencias: Hilt (Dagger)

- Asincronía: Coroutines & Flow

-Base de Datos Local: Room

- Backend & Auth: Firebase (Firestore, Auth, Storage)

- IA Generativa: Google Gemini API (AI Client SDK)

- Almacenamiento de Imágenes: Cloudflare R2 (vía AWS S3 SDK)

- Carga de Imágenes: Coil

- Navegación: Jetpack Navigation Compose

### Arquitectura del Proyecto
El código está organizado en tres capas principales:

- Domain: Contiene la lógica de negocio pura, modelos y contratos de repositorios. No tiene dependencias del framework de Android.

- Modelos: Habit, Mission, UserStats.

- Casos de Uso: GenerateHabitPlanUseCase, CompleteMissionUseCase, etc.

- Data: Implementación de repositorios y fuentes de datos.

- Local: Room Database para persistencia offline (HabitDao, MissionDao).

- Remote: Firebase Firestore y Gemini API.

- Mappers: Transformación de datos entre capas.

- UI: Capa de presentación.

- Screens: Componentes Compose (HomeScreen, HabitDetailScreen).

- ViewModels: Gestión del estado de la UI (HomeViewModel, FeedViewModel).

### Configuración y Requisitos
Para compilar y ejecutar este proyecto, es necesario configurar las claves de API en el archivo local.properties o mediante variables de entorno en tu sistema de CI/CD.

### Variables requeridas
El proyecto utiliza BuildConfig para acceder a las siguientes credenciales:

### Properties

### Google Gemini AI
```GEMINI_API_KEY="tu_api_key_de_google_ai"```

### Google Auth (Firebase)
```SERVER_CLIENT_ID="tu_web_client_id_de_google_cloud"```

### Cloudflare R2 / S3 Storage (Para avatares)
```
ACCOUNT_ID="tu_account_id"
ACCESS_KEY="tu_access_key"
SECRET_ACCESS_KEY="tu_secret_key"
PUBLIC_URL="url_publica_de_tu_bucket"
```
Configuración de Firebase
Descarga el archivo google-services.json desde la consola de Firebase.

Colócalo en el directorio app/ del proyecto.

### Funcionalidades Principales
- Generación de Hábitos con IA: Utiliza un prompt estructurado enviado a Gemini para desglosar objetivos en misiones con niveles de dificultad (Easy, Medium, Hard, Epic).

- Sistema de Progresión: Cálculo de XP y subida de niveles basado en la dificultad de las misiones completadas.

- Modo Offline/Online: Sincronización de datos entre Room (local) y Firestore (remoto) para usuarios autenticados.

- Feed Social: Funcionalidad para compartir logros y visualizar la actividad de otros usuarios.

- Gestión de Perfil: Subida de avatares personalizada conectada a almacenamiento compatible con S3.

### Instalación
Clona el repositorio y sincroniza el proyecto con Gradle:

```Bash

git clone https://github.com/juanrobilara/progressian.git
cd progressian
./gradlew build
```
