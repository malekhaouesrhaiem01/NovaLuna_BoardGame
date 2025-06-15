import gui.NovaApplication

/**
 * Main entry point that starts the [NovaApplication]
 *
 * Once the application is closed, it prints a message indicating the end of the application.
 */
fun main() {
    NovaApplication().show()
    println("Application ended. Goodbye")
}