plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.qromarck.reciperu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.qromarck.reciperu"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)


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
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    //implementation(libs.play.services.auth)
    implementation(libs.play.services.auth.v1810)
    //QR
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)
    //MAIL
    implementation (libs.android.mail.v167)
    implementation (libs.android.activation.v167)
}