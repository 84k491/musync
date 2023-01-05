import java.nio.file.Path
import java.nio.file.Paths

// TODO rename to AppFactory
class ArgumentInterpreter(val args: Array<String>) {
    private fun cwd(): Path {
        return Paths.get("").toAbsolutePath()
    }

    fun buildApp(): Application {
        val helpCommand = HelpApplication()
        if (args.isEmpty()) {
            return helpCommand
        }

        return when (args[0]) {
            "sync" -> SyncApplication(cwd(), args.drop(1))
            "init" -> InitApplication(cwd())
            "list" -> ListApplication(cwd(), args.drop(1).firstOrNull())
            "space" -> SpaceApplication(cwd(), args.drop(1).firstOrNull())
            "file" -> FileApplication(cwd(), args.drop(1))
            "destination" -> DestinationApplication(cwd(), args.drop(1))
            else -> helpCommand
        }
    }

}