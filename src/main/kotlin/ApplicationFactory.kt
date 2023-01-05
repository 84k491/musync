import java.nio.file.Path
import java.nio.file.Paths

// TODO rename to AppFactory
class ApplicationFactory(private val initialArgs: Array<String>) {
    private fun cwd(): Path {
        return Paths.get("").toAbsolutePath()
    }

    fun buildApp(): Pair<Application?, Error?> {
        val helpCommand = HelpApplication()
        if (initialArgs.isEmpty()) {
            return Pair(helpCommand, Error("A command needed"))
        }

        val command = initialArgs[0]
        val app = when (command) {
            "init" -> InitApplication(cwd())
            "help" -> helpCommand
            else -> null
        }
        app?.let { return Pair(it, null) }
        val args = initialArgs.drop(1)

        val index = Index.load(cwd())
            ?: return Pair(
                null,
                Error("Can't find index file in <${cwd()}> or above. You need to create one first with 'init'"))

        return when (command) {
            "sync" -> Pair(SyncApplication(index, args.firstOrNull()), null)
            "list" -> Pair(ListApplication(index, args.firstOrNull()), null)
            "space" -> Pair(SpaceApplication(index, args.firstOrNull()), null)
            "file" -> Pair(FileApplication(cwd(), index, args), null)
            "destination" -> Pair(DestinationApplication(index, args), null)
            else -> Pair(helpCommand, Error("Unknown command: $command"))
        }
    }

}