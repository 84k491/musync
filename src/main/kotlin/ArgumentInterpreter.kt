import java.nio.file.Path
import java.nio.file.Paths

// TODO rename to AppFactory
class ArgumentInterpreter(val _args: Array<String>) {
    private fun cwd(): Path {
        return Paths.get("").toAbsolutePath()
    }

    fun buildApp(): Application {
//        val args = listOf("./musync", "file", "add", "./gradlew")
//        val args = listOf("./musync", "file", "remove", "./gradlew.bat")
//        val args = listOf("./musync", "list", "added")
//        val args = listOf("./musync", "init",)
        val args = listOf("./musync", "destination", "add", "/bin")

        val helpCommand = HelpApplication()
        if (args.size <= 1) {
            return helpCommand
        }

        return when (args[1]) {
            "sync" -> SyncApplication(cwd(), args.drop(2))
            "init" -> InitApplication(cwd())
            "list" -> ListApplication(cwd(), args.drop(2).firstOrNull())
            "file" -> FileApplication(cwd(), args.drop(2))
            "destination" -> DestinationApplication(cwd(), args.drop(2))
            else -> helpCommand
        }
    }

}