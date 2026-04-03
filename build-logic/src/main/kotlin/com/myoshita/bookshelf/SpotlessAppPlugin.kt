package com.myoshita.bookshelf

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class SpotlessAppPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            configure<SpotlessExtension> {
                kotlin {
                    target("src/**/*.kt")
                    ktlint(libs.findVersion("ktlint").get().toString())
                }
            }
        }
    }
}