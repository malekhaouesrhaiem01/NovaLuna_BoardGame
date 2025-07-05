import edu.udo.cs.sopra.util.addFileToDistribution
import edu.udo.cs.sopra.util.ignoreClassesInCoverageReport
import edu.udo.cs.sopra.util.sonatypeSnapshots
import edu.udo.cs.sopra.util.sopraPackageRegistry
import org.gradle.kotlin.dsl.application

plugins {
    kotlin("jvm") version "1.9.25"
    application
    id("edu.udo.cs.sopra") version "1.0.2"
}

group = "edu.udo.cs.sopra"
version = "1.0"

/* Change this to the version of the BGW you want to use */
val bgwVersion = "0.10"

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

repositories {
    maven {
        url = uri("https://sopra-gitlab.cs.tu-dortmund.de/api/v4/projects/2347/packages/maven")
        credentials(HttpHeaderCredentials::class) {
            name = "Private-Token"
            value = "glpat-hgGtbYdUceoyLL-zWveM"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }

    mavenCentral()
    sonatypeSnapshots()
    sopraPackageRegistry()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation(group = "tools.aqua", name = "bgw-gui", version = bgwVersion)
    implementation(group = "tools.aqua", name = "bgw-net-common", version = bgwVersion)
    implementation(group = "tools.aqua", name = "bgw-net-client", version = bgwVersion)
    implementation(group = "edu.udo.cs.sopra", name = "ntf", version = "1.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")


}

/* This is how you can add the HowToPlay.pdf to the distribution zip file */
addFileToDistribution(file("HowToPlay.pdf"))

/* This is how you can ignore additional classes from test coverage */
/* All classes in gui, entity and service.bot package are already excluded. */
ignoreClassesInCoverageReport("") // Add any class you want to ignore here

/* To ignore a class Foo in the package foo.bar.baz you would use the following line */
// this.ignoreClassesInCoverageReport("foo.bar.baz.Foo")

/* To ignore all classes in the foo.bar.baz package use a wildcard like this */
// this.ignoreClassesInCoverageReport("foo.bar.baz.*")

tasks.clean {
    delete.add("public")
}

