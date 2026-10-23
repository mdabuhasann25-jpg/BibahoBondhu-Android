plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.hasantheabu.bibahobondhu"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.hasantheabu.bibahobondhu"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
        val apiKey = System.getenv("FIREBASE_API_KEY") ?: ""
        val appId = System.getenv("FIREBASE_APP_ID") ?: ""
        val projectId = System.getenv("FIREBASE_PROJECT_ID") ?: ""
        val databaseUrl = System.getenv("FIREBASE_DATABASE_URL") ?: ""
        buildConfigField("String", "FIREBASE_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"$appId\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$projectId\"")
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"$databaseUrl\"")
    }
    buildFeatures { compose = true; buildConfig = true }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
