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

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.0-rc02")
    }
}

plugins {
    id("com.palantir.git-version") version "0.12.3"
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}


//println("============================================== version")

//val gitVersion: groovy.lang.Closure<String> by extra
//val gitversion = gitVersion()
//println("gitversion: $gitversion")

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra

val gitdetails = versionDetails()
//println("gitdetails: $gitdetails")
val commit = gitdetails.gitHash.take(8)

val refTime = java.util.GregorianCalendar(2020,0,1).time!! // Date
val startTime = java.util.Date()
val seconds = ((startTime.time - refTime.time) / 1000)
println("seconds:     $seconds")
val minutes = seconds/60
println("minutes:     $minutes")
val tenminutes = seconds/60/10
println("tenminutes:  $tenminutes")
val fiveminutes = seconds/60/5
println("fiveminutes: $fiveminutes")
val hours = seconds/60/60
println("hours:       $hours")

val major = 8
val minor = 1

var buildTime    = java.text.SimpleDateFormat("yyMMddHHmmss").format(startTime)!!
var buildNumber  = if("$major$minor" == "80") fiveminutes.toString() else tenminutes.toString() //TODO hg42
//var buildNumber  = java.text.SimpleDateFormat("yyDDDHHmm").format(startTime).substring(1, 7)
//var buildmmss    = java.text.SimpleDateFormat("mmss").format(startTime)
var buildLabel   = gitdetails.branchName
                        .replace(Regex("^feature-"), "f-")
                        .replace(Regex("^PR-"), "p-")
                        .replace(Regex("^fix-"), "x-")
                        .replace(Regex("^work$"), "W")
                        .replace(Regex("^temp$"), "T")
var buildVersion = "$major.$minor.${buildNumber}-hg42-${commit}-${buildLabel}-${buildTime}"
//var buildVersion = "${buildNumber}${if(gitdetails.isCleanTag) "" else "-DIRTY"}-${buildLabel}-${commit}-${buildTime}"
//var buildVersion = "${buildNumber}${if(gitdetails.isCleanTag) "" else "-DIRTY"}-${buildLabel}-${gitdetails.commitDistance}-${commit}-${buildTime}"
//buildVersion = buildVersion.replace("--", "")

rootProject.ext.set("major",        major)
rootProject.ext.set("minor",        minor)
rootProject.ext.set("buildNumber",  buildNumber)
rootProject.ext.set("buildVersion", buildVersion)

/*
println("""
version palantir:
    commitDistance: ${gitdetails.commitDistance}
    lastTag:        ${gitdetails.lastTag}
    gitHash:        ${gitdetails.gitHash}
    gitHashFull:    ${gitdetails.gitHashFull}
    branchName:     ${gitdetails.branchName}
    isCleanTag:     ${gitdetails.isCleanTag}
""")
*/
println("""
version build:
    startTime:      $startTime
    buildTime:      $buildTime
    buildVersion:   $buildVersion
""")

//println("============================================== version.")
