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
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

data class InfoFromGit(
    val lastTag: String?,
    val lastTagDateTime: LocalDateTime?,
    val lastTagMajor: Int?,
    val lastTagMinor: Int?,
    val lastTagPatch: Int?,
    val currentBranch: String,
    val headHash: String
)

fun getInfoFromGit(): InfoFromGit {
    val tagPattern = "(\\d+)\\.(\\d+)\\.(\\d+)"
    val tagProcess = ProcessBuilder("git", "log", "--tags", "--simplify-by-decoration", "--pretty=format:%ai %d", "--date=iso")
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()

    val tagResult = tagProcess.inputStream.bufferedReader().use { it.readText() }
    tagProcess.waitFor(10, TimeUnit.SECONDS)

    val regex = Regex(tagPattern)
    val matchResult = regex.find(tagResult)

    var lastTag: String? = null
    var lastTagDateTime: LocalDateTime? = null
    var lastTagMajor: Int? = null
    var lastTagMinor: Int? = null
    var lastTagPatch: Int? = null

    if (matchResult != null) {
        val tagLine = tagResult.lines().first { it.contains(matchResult.value) }
        val dateTime = tagLine.split(" ")[0] + "T" + tagLine.split(" ")[1]
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        lastTagDateTime = LocalDateTime.parse(dateTime, formatter)

        val (major, minor, patch) = matchResult.destructured.toList().map { it.toInt() }

        lastTag = matchResult.value
        lastTagMajor = major
        lastTagMinor = minor
        lastTagPatch = patch
    }

    // Aktuellen Branch ermitteln
    val branchProcess = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    val currentBranch = branchProcess.inputStream.bufferedReader().use { it.readText().trim() }
    branchProcess.waitFor(10, TimeUnit.SECONDS)

    // Hash des HEADs ermitteln
    val hashProcess = ProcessBuilder("git", "rev-parse", "HEAD")
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    val headHash = hashProcess.inputStream.bufferedReader().use { it.readText().trim() }
    hashProcess.waitFor(10, TimeUnit.SECONDS)

    return InfoFromGit(
        lastTag = lastTag,
        lastTagDateTime = lastTagDateTime,
        lastTagMajor = lastTagMajor,
        lastTagMinor = lastTagMinor,
        lastTagPatch = lastTagPatch,
        currentBranch = currentBranch,
        headHash = headHash.take(8)
    )
}

val gitInfo = getInfoFromGit()

val lastTag = gitInfo.lastTag
val lastTagDateTime = gitInfo.lastTagDateTime
val lastTagMajor = gitInfo.lastTagMajor
val lastTagMinor = gitInfo.lastTagMinor
val lastTagPatch = gitInfo.lastTagPatch
val currentBranch = gitInfo.currentBranch
val headHash = gitInfo.headHash


// extract base version from last git tag
val major by extra(lastTagMajor)
val minor by extra(lastTagMinor)
val revision by extra(lastTagPatch)

//val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
//
//val gitDetails = versionDetails()
////println("gitDetails: $gitDetails")
//val gitCommit = gitDetails.gitHash.take(8)
////val gitTag = gitDetails.lastTag
//val gitDistance = gitDetails.commitDistance
//val gitHash = gitDetails.gitHash
//// gitHashFull // full 40-character Git commit hash
//val gitBranch = gitDetails.branchName // is null if the repository in detached HEAD mode
//var gitDirty = !gitDetails.isCleanTag

//val refTime = java.util.GregorianCalendar(2020, 0, 1).time!! // Date
//val startTime = java.util.Date()
//val seconds = ((startTime.time - refTime.time) / 1000); println("seconds:     $seconds")
val refTime = lastTagDateTime ?: LocalDateTime.parse("2020-01-01T00:00:00"); println("refTime:     $refTime")
val startTime = LocalDateTime.now(); println("startTime:   $startTime")
val seconds = startTime.toEpochSecond(ZoneOffset.UTC) - refTime.toEpochSecond(ZoneOffset.UTC); println("seconds:     $seconds")
val minutes = seconds / 60; println("minutes:     $minutes")
val fiveminutes = seconds / 60 / 5; println("fiveminutes: $fiveminutes")
val tenminutes = seconds / 60 / 10; println("tenminutes:  $tenminutes")
val hours = seconds / 60 / 60; println("hours:       $hours")

val buildTime by extra { startTime.format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) }
//var buildNumber by extra(buildTime.substring(1..6))
val buildNumber : String by extra { minutes.toString() }
//var buildMinSec by extra(java.text.SimpleDateFormat("mmss").format(startTime))
val buildLabel by extra {
    currentBranch
        .replace(Regex("^feature-"), "F-")
        .replace(Regex("^PR-"), "P-")
        .replace(Regex("^fix-"), "I-")
        .replace(Regex("^work$"), "W")
        .replace(Regex("^wip$"), "W")
        .replace(Regex("^temp$"), "T")
        .replace(Regex("^experimental$"), "X")
}
var buildVersion by extra { "$major.$minor.$revision.$buildNumber-hg42-${headHash}-${buildTime}-${buildLabel}" }
var buildVersionCode by extra { "$major$minor$revision${buildNumber.padStart(5, '0')}".toInt() }

println(
    """
version build:
    startTime:          $startTime
    buildTime:          $buildTime
    buildVersion:       $buildVersion
    buildVersionCode:   $buildVersionCode
    basedOn:            $lastTag
        time:               $lastTagDateTime
        commit:             $headHash
        branch:             $currentBranch
"""
)


//System.exit(0)

val neobackup_keystore: String by rootProject.extra
val neobackup_keystorepass: String by rootProject.extra
val neobackup_keypass: String by rootProject.extra

android {
    namespace = "com.machiav3lli.backup"

    signingConfigs {
        create("hg42test") {
            storeFile = file(neobackup_keystore)
            storePassword = neobackup_keystorepass
            keyPassword = neobackup_keypass
            keyAlias = "cert"
        }
    }

    compileSdk = 34

    defaultConfig {
        applicationId = "com.machiav3lli.backup"
        minSdk = 26
        targetSdk = 34

        versionCode = buildVersionCode
        versionName = buildVersion
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
                ksp {
                    arg("room.schemaLocation", "$projectDir/schemas")
                    arg("room.incremental", "true")
                    arg("room.generateKotlin", "true")
                }
            }
        }

        println("\n---------------------------------------- version $versionCode $versionName\n\n")
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        create("pumpkin") {
            applicationIdSuffix = ".hg42"
            versionNameSuffix = ""
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
                "proguard-rules.pro",
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
        buildConfig = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)
    implementation(libs.koin.annotations)
    ksp(libs.koin.compiler)

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
    // hg42
    //implementation("de.voize:semver4k:$vSemVer")

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
    val langsListString = "{${langsList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField("String[]", "DETECTED_LOCALES", langsListString)
}
tasks.preBuild.dependsOn("detectAndroidLocals")

tasks.withType<Test> {
    useJUnit() // we still use junit4
    // useTestNG()
    // useJUnitPlatform()
}
