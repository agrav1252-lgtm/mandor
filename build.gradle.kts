import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "com.mandor"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.ui)
    implementation(compose.materialIconsExtended)
    implementation(compose.animation)

    // Supabase & Realtime Libraries
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.1")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.6.1")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")

    // Koin for Dependency Injection
    implementation("io.insert-koin:koin-core:3.5.3")
    implementation("io.insert-koin:koin-compose:1.1.2")

    // PDF Generation
    implementation("com.github.librepdf:openpdf:2.0.2")
}

compose.desktop {
    application {
        mainClass = "com.mandor.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb
            )

            packageName = "Mandor"
            packageVersion = "1.0.0"
            description = "نظام مندور لإدارة مبيعات الجملة"
            vendor = "Mandor Systems"
            copyright = "© 2026 Mandor"

            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }

            windows {
                includeAllModules = true
                dirChooser = true
                perUserInstall = false
                menuGroup = "مندور"
                shortcut = true
                upgradeUuid = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
        }
    }
}
