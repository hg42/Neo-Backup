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

plugins {
    id("com.palantir.git-version") version "0.15.0"
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
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


//------------------------------------------------------------ version
val major by extra(8)
val minor by extra(3)
//------------------------------------------------------------


//val gitVersion: groovy.lang.Closure<String> by extra
//val gitversion = gitVersion()
//println("gitversion: $gitversion")

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra

val gitDetails = versionDetails()
//println("gitDetails: $gitDetails")
val gitCommit = gitDetails.gitHash.take(8)
val gitTag = gitDetails.lastTag
val gitDistance = gitDetails.commitDistance
val gitHash = gitDetails.gitHash
// gitHashFull // full 40-character Git commit hash
val gitBranch = gitDetails.branchName // is null if the repository in detached HEAD mode
var gitDirty = !gitDetails.isCleanTag

val refTime = java.util.GregorianCalendar(2020, 0, 1).time!! // Date
val startTime = java.util.Date()
val seconds = ((startTime.time - refTime.time) / 1000); println("seconds:     $seconds")
val minutes = seconds / 60; println("minutes:     $minutes")
val fiveminutes = seconds / 60 / 5; println("fiveminutes: $fiveminutes")
val tenminutes = seconds / 60 / 10; println("tenminutes:  $tenminutes")
val hours = seconds / 60 / 60; println("hours:       $hours")

var buildTime by extra(java.text.SimpleDateFormat("yyMMddHHmmss").format(startTime)!!)
//var buildNumber by extra(buildTime.substring(1..6))
var buildNumber by extra(tenminutes.toString())
//var buildMinSec by extra(java.text.SimpleDateFormat("mmss").format(startTime))
var buildLabel by extra(
    gitBranch
        .replace(Regex("^feature-"), "F-")
        .replace(Regex("^PR-"), "P-")
        .replace(Regex("^fix-"), "I-")
        .replace(Regex("^work$"), "W")
        .replace(Regex("^temp$"), "T")
        .replace(Regex("^experimental$"), "X")
)
var buildVersion by extra("$major.$minor.${buildNumber}-hg42-${gitCommit}-${buildTime}-${buildLabel}")
// does not work? neither dirty (always?) nor distance (from last tag?)
// "${if(gitDirty) "+$gitDistance" else ""}"

println(
    """
version build:
    startTime:      $startTime
    buildTime:      $buildTime
    buildVersion:   $buildVersion
"""
)


//System.exit(0)
