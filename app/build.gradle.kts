plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
    id("com.google.gms.google-services")
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.qromarck.reciperu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.qromarck.reciperu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
        resources.excludes.add("META-INF/*")
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.work.runtime)

    implementation(platform(libs.firebase.bom.v3280))
    implementation(libs.preference)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.okhttp) // OkHttp para realizar solicitudes HTTP
    implementation(libs.json) // org.json para trabajar con JSON
    implementation(libs.gson)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebaseDatabaseRealtime)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.auth.v1810)
    //QR
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)
    //MAIL
    implementation (libs.android.mail.v167)
    implementation (libs.android.activation.v167)

    //Forp perfil
    implementation (libs.volley)
    implementation (libs.retrofit2.retrofit)
    implementation (libs.converter.gson)

    //GIF
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}