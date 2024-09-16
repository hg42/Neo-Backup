/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    //alias(libs.plugins.kotlin.scripting)
}

val jvmVersion = JavaVersion.VERSION_17

android {
    namespace = "com.machiav3lli.backup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.machiav3lli.backup"
        minSdk = 26
        targetSdk = 34
        versionCode = 8318
        versionName = "8.3.8"
        buildConfigField("int", "MAJOR", "8")
        buildConfigField("int", "MINOR", "3")

        testApplicationId = "$applicationId.tests"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                ksp {
                    arg("room.schemaLocation", "$projectDir/schemas")
                    arg("room.incremental", "true")
                    arg("room.generateKotlin", "true")
                }
            }
        }
    }

    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Backup_${variant.name}_${variant.versionName}.apk"
        }
        true
    }

    buildTypes {
        named("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            isMinifyEnabled = true
        }
        named("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        create("neo") {
            applicationIdSuffix = ".neo"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }
    kotlin {
        jvmToolchain(jvmVersion.toString().toInt())
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                //"-Xuse-fir-lt=false",   // Scripts are not yet supported with K2 in LightTree mode
                //"-Xallow-any-scripts-in-source-roots",
            )
            if (project.findProperty("enableComposeCompilerReports") == "true") {
                val metricsDir =
                    "${project.layout.buildDirectory.asFile.get().absolutePath}/compose_metrics"
                println("--- enableComposeCompilerReports -> $metricsDir")
                freeCompilerArgs.addAll(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$metricsDir",
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$metricsDir",
                )
            }
        }
    }
    lint {
        checkReleaseBuilds = false
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    packaging {
        resources {
            excludes += listOf(
                "/DebugProbesKt.bin",
                "/kotlin/**.kotlin_builtins",
                "/kotlin/**.kotlin_metadata",
                "/META-INF/**.kotlin_module",
                "/META-INF/**.pro",
                "/META-INF/**.version",     // comment out to enable layout inspector
                "/META-INF/LICENSE-notice.md",
                "/META-INF/LICENSE.md"
            )
        }
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.ksp)
    // not yet necessary: implementation(libs.kotlin.reflect)

    // Libs
    implementation(libs.activity.compose)
    implementation(libs.collections.immutable)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    // TODO use the new WorkInfo.stopReason (report stopReason), WorkManager.getWorkInfosFlow (Flow instead of LiveData), setNextScheduleTimeOverride (Precise scheduling), Configuration.Builder.setContentUriTriggerWorkersLimit (limit for content uri workers)
    implementation(libs.work.runtime)
    implementation(libs.serialization.json)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle)
    implementation(libs.biometric)
    implementation(libs.kaml)
    implementation(libs.security.crypto)
    implementation(libs.commons.io)
    implementation(libs.commons.compress)
    implementation(variantOf(libs.zstd.jni) { artifactType("aar") })
    implementation(libs.timber)
    implementation(libs.semver)
    implementation(libs.libsu.core)
    implementation(libs.libsu.io)

    // UI
    implementation(libs.material)
    implementation(libs.preference)

    // Compose
    api(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation)
    implementation(libs.compose.navigation)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)

    // Testing
    androidTestImplementation(libs.test.runner)
    implementation(libs.test.rules)
    implementation(libs.test.ext)

    // compose testing
    // Test rules and transitive dependencies:
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.compose.ui.test.junit4)
    // Needed for createComposeRule, but not createAndroidComposeRule:
    debugImplementation(libs.compose.ui.test.manifest)
    //---------------------------------------- hg42
    // can only be enabled on demand, otherwise it conflicts with compilation
    // TODO hg42 without the library the .main.kts script still works, but syntax checking is not working
    //implementation(libs.kotlin.main.kts)
    implementation(kotlin("script-runtime"))    // for intellisense in kts scripts
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("src/main/res").visit {
        if (this.file.path.endsWith("strings.xml") &&
            this.file.canonicalFile
                .readText()
                .contains("<string")
        ) {
            var languageCode =
                this.file.parentFile.name
                    .replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.add(languageCode)
        }
    }
    val langsListString = "{${langsList.sorted().joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField("String[]", "DETECTED_LOCALES", langsListString)
}
tasks.preBuild.dependsOn("detectAndroidLocals")

tasks.withType<Test> {
    useJUnit() // we still use junit4
    // useTestNG()
    // useJUnitPlatform()
}


// Exclude (non-gradle) kts scripts from compilation
tasks.withType<KotlinCompile>().configureEach {
    setSource(sources.filterNot {
        //it.name.endsWith(".generator.kts")
        it.extension == "kts"
    })
}
