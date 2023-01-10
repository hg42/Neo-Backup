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
import org.jetbrains.kotlin.konan.properties.Properties

val major: Int by rootProject.extra
val minor: Int by rootProject.extra
val buildNumber: String by rootProject.extra
val buildVersion: String by rootProject.extra

val locals = Properties()
if (rootProject.file("local.properties").exists()) {
    locals.load(rootProject.file("local.properties").inputStream())
}

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization").version("1.8.0")
}

val vKotlin = "1.8.0"
val vComposeCompiler = "1.4.0"

val vCompose = "1.3.1"
val vRoom = "2.5.0"
val vNavigation = "2.5.3"
val vAccompanist = "0.28.0"
val vLibsu = "5.0.4"
//val vIconics = "5.3.4"

val vJunitJupiter = "5.9.2"
val vAndroidxTest = "1.5.0"
val vAndroidxTestExt = "1.1.5"

android {
    namespace = "com.machiav3lli.backup"

    signingConfigs {
        create("hg42test") {
            storeFile = file(locals.getProperty("keystore"))
            storePassword = locals.getProperty("keystorepass")
            keyAlias = "cert"
            keyPassword = locals.getProperty("keypass")
        }
    }

    compileSdk = 33

    defaultConfig {
        applicationId = "com.machiav3lli.backup"
        minSdk = 26
        targetSdk = 32

        versionCode = "$major$minor$buildNumber".toInt()
        versionName = "$buildVersion"
        buildConfigField("int", "MAJOR", "$major")
        buildConfigField("int", "MINOR", "$minor")

        // Tests
        testApplicationId = "${applicationId}.tests"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnit5Runner"
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitPlatformRunner"
        //testInstrumentationRunner = "androidx.test.ext.junit.runners.AndroidJUnit5"

        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        // testInstrumentationRunnerArguments.put("clearPackageData", "true")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(
                    mapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true"
                    )
                )
            }
        }

        println("\n---------------------------------------- version $versionCode $versionName\n\n")
    }

    buildTypes {
        named("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //versionNameSuffix = "-alpha01"
            isMinifyEnabled = true
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
            signingConfig = signingConfigs.getByName("hg42test")
        }
        named("debug") {
            applicationIdSuffix = ".hg42.debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_vv"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_vv"
            signingConfig = signingConfigs.getByName("hg42test")
        }
        create("neo") {
            applicationIdSuffix = ".neo"
            //versionNameSuffix = "-alpha01"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("pumpkin") {
            applicationIdSuffix = ".hg42"
            versionNameSuffix = ""
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_vv"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_vv"
            signingConfig = signingConfigs.getByName("hg42test")
        }
        create("pumprel") {
            applicationIdSuffix = ".hg42.rel"
            versionNameSuffix = "-rel"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_vv"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_vv"
            signingConfig = signingConfigs.getByName("hg42test")
        }
        applicationVariants.all {
            outputs.all {
                this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                //val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                //println("--< ${outputFileName}")

                //output.outputFileName =
                outputFileName =
                    "nb-${
                        name
                            .replace("release", "")
                            .replace("hg42", "")
                            .replace("pumpkin", "")
                            .replace("pumprel", "rel")
                    }-${buildVersion}.apk"
                        .replace(Regex("""--+"""), "-")

                println("----------------------------------------> output ${outputFileName}")
            }
        }
    }
    buildFeatures {
        dataBinding = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        //sourceCompatibility = JavaVersion.VERSION_1_8
        //targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = vComposeCompiler
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = compileOptions.sourceCompatibility.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
    lint {
        checkReleaseBuilds = false
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    packagingOptions {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(kotlin("stdlib", vKotlin))
    implementation(kotlin("reflect", vKotlin))
    implementation(kotlin("script-runtime", vKotlin))

    // Libs
    implementation("androidx.room:room-runtime:$vRoom")
    implementation("androidx.room:room-ktx:$vRoom")
    kapt("androidx.room:room-compiler:$vRoom")
    implementation("androidx.work:work-runtime-ktx:2.8.0-rc01")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha04")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("org.apache.commons:commons-compress:1.22")
    implementation("commons-io:commons-io:2.11.0")      // attention, there is an old 2003 version, that looks like newer
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.github.topjohnwu.libsu:core:$vLibsu")
    implementation("com.github.topjohnwu.libsu:io:$vLibsu")
    implementation("de.voize:semver4k:4.1.0")

    // UI
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.preference:preference-ktx:1.2.0")

    // Compose
    implementation("androidx.compose.runtime:runtime:$vCompose")
    implementation("androidx.compose.ui:ui:$vCompose")
    implementation("androidx.compose.ui:ui-tooling:$vCompose")
    implementation("androidx.compose.foundation:foundation:$vCompose")
    implementation("androidx.compose.runtime:runtime-livedata:$vCompose")
    implementation("androidx.navigation:navigation-compose:$vNavigation")
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("com.google.accompanist:accompanist-flowlayout:$vAccompanist")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$vAccompanist")
    implementation("com.google.accompanist:accompanist-navigation-animation:$vAccompanist")
    implementation("com.google.accompanist:accompanist-pager:$vAccompanist")
    //implementation("androidx.paging:paging-runtime-ktx:3.2.0-alpha03")

    //implementation("com.mikepenz:iconics-core:$vIconics")
    //implementation("com.mikepenz:iconics-compose:$vIconics")
    //implementation("com.mikepenz:phosphor-typeface:1.0.0.0-kotlin@aar")
    //implementation("com.mikepenz:community-material-typeface:6.4.95.0-kotlin@aar")

    // Testing
    //androidTestImplementation("org.junit.jupiter:junit-jupiter:$vJunitJupiter")
    androidTestImplementation("androidx.test:runner:$vAndroidxTest")
    implementation("androidx.test:rules:$vAndroidxTest")
    implementation("androidx.test.ext:junit-ktx:$vAndroidxTestExt")

    // compose testing
    //androidTestImplementation("androidx.ui:ui-test:$vCompose")
    // Test rules and transitive dependencies:
    androidTestImplementation("androidx.compose.ui:ui-test:$vCompose")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$vCompose")
    // Needed for createComposeRule, but not createAndroidComposeRule:
    debugImplementation("androidx.compose.ui:ui-test-manifest:$vCompose")

    //    // Testing
    //
    //    // junit4
    //
    //    //testImplementation("junit:junit:$vJunit4")
    //+   implementation("androidx.test:rules:$vAndroidxTest")
    //+   androidTestImplementation("androidx.test:runner:$vAndroidxTest")
    //
    //    // To use the androidx.test.core APIs
    //    //androidTestImplementation("androidx.test:core:$vAndroidxTest")
    //    // Kotlin extensions for androidx.test.core
    //    //androidTestImplementation("androidx.test:core-ktx:$vAndroidxTest")
    //
    //    // To use the JUnit Extension APIs
    //+   androidTestImplementation("androidx.test.ext:junit:$vAndroidxTestExt")
    //    // Kotlin extensions for androidx.test.ext.junit
    //+   androidTestImplementation("androidx.test.ext:junit-ktx:$vAndroidxTestExt")
    //
    //    // compose
    //+   androidTestImplementation("androidx.compose.ui:ui-test:$vCompose")
    //    //androidTestImplementation("androidx.compose.ui:ui-test-junit4:$vCompose")
    //    // Needed for createComposeRule, but not createAndroidComposeRule:
    //+   debugImplementation("androidx.compose.ui:ui-test-manifest:$vCompose")
    //
    //          // Optional -- Hamcrest library
    //          //androidTestImplementation("org.hamcrest:hamcrest-library:$vHamcrest")
    //          // Optional -- UI testing with Espresso
    //          //androidTestImplementation("androidx.test.espresso:espresso-core:$vEspresso")
    //          //androidTestImplementation("androidx.test.espresso:espresso-contrib:$vEspresso")
    //          // Optional -- UI testing with UI Automator
    //          //androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    //          // Optional -- UI testing with Roboelectric
    //          //testImplementation("org.robolectric:robolectric:4.4")
    //
    //          // To use the Truth Extension APIs
    //          //androidTestImplementation("androidx.test.ext:truth:$vAndroidxTest")
    //
    //          // To use android test orchestrator
    //          //androidTestUtil("androidx.test:orchestrator:$vAndroidxTest")
    //
    //          // junit5
    //
    //          //androidTestImplementation("org.junit.jupiter:junit-jupiter:$vJunitJupiter")
    //          //testImplementation("org.junit.jupiter:junit-jupiter-api:$vJunitJupiter")
    //          //androidTestImplementation("org.junit.jupiter:junit-jupiter-api:$vJunitJupiter")
    //          //testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$vJunitJupiter")
    //          //androidTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$vJunitJupiter")
    //          // (Optional) If "Parameterized Tests" are needed
    //          //testImplementation("org.junit.jupiter:junit-jupiter-params:$vJunitJupiter")
    //          // (Optional) If you also have JUnit 4-based tests
    //          //testImplementation("junit:junit:$vJunit4")
    //          //testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$vJunitJupiter")
    //
    //          //testImplementation("org.junit.platform:junit-platform-runner:$vJunitPlatform")
    //          //androidTestImplementation("org.junit.platform:junit-platform-runner:$vJunitPlatform")
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("src/main/res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile.name.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.add(languageCode)
        }
    }
    val langsListString = "{${langsList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField("String[]", "DETECTED_LOCALES", langsListString)
}
tasks.preBuild.dependsOn("detectAndroidLocals")

// tells all test tasks to use Gradle's built-in JUnit 5 support
tasks.withType<Test> {
    useJUnit()
    //useTestNG()
    //useJUnitPlatform()
}
