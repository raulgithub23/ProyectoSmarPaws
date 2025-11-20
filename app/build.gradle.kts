plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.example.smartpaws"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartpaws"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3.window.size.class1)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //librerias nuevas añadidas
    implementation("androidx.navigation:navigation-compose:2.9.5") //Permite navegar entre pantallas en Jetpack Compose de forma declarativa (usando rutas y NavController).
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4") // Proporciona extensiones de Kotlin para ViewModels (como viewModelScope para corrutinas)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4") // Integra ViewModels con Compose (función viewModel() para obtener/crear ViewModels
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4") // Permite observar estados del ciclo de vida en Composables (como collectAsStateWithLifecycle())
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")// Soporte de corrutinas para Android, incluyendo Dispatchers.Main para operaciones en el hilo principa
    implementation("androidx.compose.material:material-icons-extended")// Acceso a todos los iconos de Material Design (más allá de los básicos incluidos por defecto)
    implementation("androidx.room:room-runtime:2.6.1")// Motor de ejecución de Room (base de datos SQLite con ORM).
    implementation("androidx.room:room-ktx:2.6.1")// Extensiones de Kotlin para Room que permiten usar suspend functions y Flow en las consultas DAO
    ksp("androidx.room:room-compiler:2.6.1")//Fechas para el calendario
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")//
    implementation("io.coil-kt:coil-compose:2.7.0")//cargar imagenes con compose
    implementation("androidx.datastore:datastore-preferences:1.1.1")//Data Storage

    //NUEVAS LIBRERIAS POST MICROSERVICIOS

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}