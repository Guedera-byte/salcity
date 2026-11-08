import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
}
// =====================================================
// SALCITY - CONFIGURAÇÃO DO GOOGLE MAPS
// =====================================================
//
// Lê o arquivo local.properties e recupera
// a chave MAPS_API_KEY.
//
// A chave será enviada ao AndroidManifest
// durante a compilação do aplicativo.
// =====================================================

// Cria um objeto para armazenar as propriedades.
val localProperties = Properties()

// Localiza o arquivo local.properties.
val localPropertiesFile = rootProject.file("local.properties")

// Verifica se o arquivo existe.
if (localPropertiesFile.exists()) {

    // Abre o arquivo e carrega suas propriedades.
    localPropertiesFile.inputStream().use { input ->

        localProperties.load(input)
    }
}

/*
 * =====================================================
 * SALCITY - VALIDAÇÃO DA CHAVE GOOGLE MAPS
 * =====================================================
 *
 * OBJETIVO:
 * Recuperar a chave do arquivo local.properties.
 *
 * Caso a chave não exista, a compilação será
 * interrompida com uma mensagem explicativa.
 */

val mapsApiKey = localProperties
    .getProperty("MAPS_API_KEY", "")
    .trim()

// Sem chave, o app AINDA COMPILA e roda (apenas o mapa fica cinza).
// Antes, require() derrubava o build inteiro para quem não tinha a chave.
if (mapsApiKey.isEmpty()) {
    logger.warn(
        "SalCity: MAPS_API_KEY ausente em local.properties. " +
                "O app compila, mas o mapa nao sera exibido."
    )
}
// URL pública do backend da IA. A chave GEMINI_API_KEY nunca deve ser colocada aqui.
val salcityAiBaseUrl = providers.gradleProperty("SALCITY_AI_BASE_URL")
    .getOrElse("https://api.salcity.example/api")
android {
    namespace = "com.example.salcity"

    compileSdk {
        version = release(37)

    }

    defaultConfig {
        applicationId = "com.example.salcity"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SALCITY_AI_BASE_URL", "\"$salcityAiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //
        // Envia a chave de API para o AndroidManifest.
        //
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey.ifEmpty { "CHAVE_NAO_CONFIGURADA" }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    // SDK responsável pela exibição do Google Maps.
    implementation("com.google.android.gms:play-services-maps:19.2.0")

    // Biblioteca para consultar a localização do dispositivo.
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Lon-in com o google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
}
