import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zf.camera.trick"
    compileSdk = 34

    viewBinding {
        enable = true
    }

    // 加载签名配置文件（正式项目必用）
    val keystorePropertiesFile = rootProject.file("./key/keystore.properties")
    System.out.println("keystorePropertiesFile : ${keystorePropertiesFile.absolutePath}")
    val keystoreProperties = Properties()

    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        System.out.println(file(keystoreProperties["storeFile"] as String))
    } else {
        System.out.println("keystorePropertiesFile not found ")
    }
// ==============================================
    // 签名配置（核心部分）
    // ==============================================
    signingConfigs {
        create("release") {
            // 从配置文件读取
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String

            // 签名版本（兼容所有设备）
            isV1SigningEnabled = true
            isV2SigningEnabled = true
//            isV3SigningEnabled = true
        }

        // 可选：调试签名
//        create("debug") {
//            storeFile = file("debug.keystore")
//            storePassword = "android"
//            keyAlias = "androiddebugkey"
//            keyPassword = "android"
//        }
    }


    defaultConfig {
        applicationId = "com.zf.camera.trick"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
//            abiFilters.add("armeabi")
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
//            abiFilters.add("x86")
        }
    }

    // ======================================================
    // 【核心】自定义 APK 输出名称 + 输出到 根目录/pkg
    // ======================================================
    applicationVariants.configureEach {
        val variant = this

        if (variant.buildType.name == "release") {
            assembleProvider.configure {
                doLast {
                    // 时间戳
                    val dateFormat = SimpleDateFormat("yyyy-MMddHHmm")
                    val buildTime = dateFormat.format(Date())

                    // 输出目录
                    val outDir = File(rootProject.projectDir, "pkg")
                    outDir.mkdirs()

                    // 复制 APK
                    val apkInput = File("${layout.buildDirectory.get()}/outputs/apk/release/app-release.apk")

                    if (apkInput.exists()) {
                        val apkOutput = File(outDir, "CameraTrick_V${variant.versionName}_${buildTime}_Release.apk")
                        apkInput.copyTo(apkOutput, overwrite = true)
                    }

                    // 复制 mapping
                    val mappingInput = File("${layout.buildDirectory.get()}/outputs/mapping/release/mapping.txt")
                    if (mappingInput.exists()) {
                        val mappingOutput = File(outDir, "mapping.txt")
                        mappingInput.copyTo(mappingOutput, overwrite = true)
                    }
                }
            }
        }
    }

    sourceSets["main"].jniLibs {
        srcDir("./libs")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// ✅ clean 时自动删除 pkg 目录下的文件
tasks.clean {
    doLast {
        val pkgDir = File(rootProject.projectDir, "pkg")
        if (pkgDir.exists()) {
            pkgDir.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.14-SNAPSHOT")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // 权限请求库
    implementation("pub.devrel:easypermissions:3.0.0")

    //状态啦库
    implementation ("com.geyifeng.immersionbar:immersionbar:3.2.2")
    implementation ("com.geyifeng.immersionbar:immersionbar-components:3.2.2")

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}