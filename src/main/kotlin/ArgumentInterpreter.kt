import java.nio.file.Path
import java.nio.file.Paths

// TODO rename to AppFactory
class ArgumentInterpreter(val _args: Array<String>) {
    private fun cwd(): Path {
        return Paths.get("").toAbsolutePath()
    }

    fun buildApp(): Application {
        val args = listOf("./musync", "file", "add", "./gradlew")
//        val args = listOf("./musync", "init",)
        val helpCommand = HelpApplication()
        if (args.size <= 1) {
            return helpCommand
        }

        return when (args[1]) {
            "sync" -> SyncApplication(cwd(), args.drop(2))
            "init" -> InitApplication(cwd())
            "list" -> ListApplication(cwd())
            "file" -> FileApplication(cwd(), args.drop(2))
            //"destination"
            else -> helpCommand
        }
    }

}