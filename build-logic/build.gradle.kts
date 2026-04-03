plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.spotlessGradlePlugin)
}

gradlePlugin {
    plugins {
        register("spotless") {
            id = "com.myoshita.bookshelf.spotless"
            implementationClass = "com.myoshita.bookshelf.SpotlessAppPlugin"
        }
    }
}
