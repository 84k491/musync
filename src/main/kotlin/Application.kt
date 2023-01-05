import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.relativeTo

abstract class Application { abstract fun work(): Error? }
abstract class IndexedApplication(val index: Index): Application()

class HelpApplication: Application() {
    override fun work(): Error? {
        println("" +
                "musync init: initialize (current) directory with empty index file\n" +
                "musync list: [added|removed|(default)new]: list files of the specified type\n" +
                "musync space: [added|removed|(default)new]: get size of all files with specified type\n" +
                "musync destination: [add|remove] <path(s)>: add or remove destination\n" +
                "musync file: [add|remove] <file(s)>: change permission of file(s)\n" +
                "musync sync: perform the synchronization (ask to copy)\n")
        return null
    }
}

class FileApplication(private val cwd: Path, i: Index, private val args: List<String>): IndexedApplication(i) {
    private fun decodeAction(str: String): Action? {
        return when (str) {
            "add" -> Action.Include
            "remove" -> Action.Exclude
            else -> null
        }
    }

    override fun work(): Error? {
        val action = decodeAction(args[0]) ?: return Error("Unknown action <${args[0]}>")

        val launcher = Launcher(index)
        val files = args.drop(1)
        // TODO use undefined if empty
        files.forEach {
            val pathToFind = cwd.resolve(Path(it)).toAbsolutePath().normalize()
            val file = launcher.source.findByPath(pathToFind.relativeTo(launcher.source.fullPath()))
                ?: return@work Error("There is no such file: $it")
            file.setActionRecursively(action)
        }

        index.updatePermissions(launcher.source.getPermissions().filter {
            it.value != Action.Undefined &&
            it.value != Action.Mixed
        })
        index.serialize()
        return null
    }
}

class InitApplication(private val cwd: Path): Application() {
    override fun work(): Error? {
        val i = Index.load(cwd)
        if (null != i) {
            return Error("Init failed. There is an index file already: ${i.file.path}")
        }

        val filePath = Index.create() ?: return Error("Failed to create index file in $cwd")
        println("Index file created: $filePath")
        return null
    }
}

class SyncApplication(i: Index, private val args: List<String>): IndexedApplication(i) {
    enum class SubCommand {
        Ask,
        DryRun,
        Force,
    }

    private fun decodeSubcommand(str: String?): SubCommand? {
        // TODO make a check if arg is an incorrect command
        return if (null != str) {
            SubCommand.valueOf(str)
        }
        else {
            SubCommand.Ask
        }
    }

    override fun work(): Error? {
        val scString = args.firstOrNull()
        val subCommand = decodeSubcommand(scString) ?: return Error("Unknown subcommand $scString")

        val launcher = Launcher(index)

        val dispatcher = Dispatcher(launcher.source, launcher.destinations)
        dispatcher.dispatchObjects()?.let { return@work it }
        dispatcher.printPlans()

        if (SubCommand.DryRun == subCommand) {
            return null
        }

//        if (SubCommand.Ask == subCommand) {

        println("Removing and copying...")
        launcher.destinations.forEach { dest ->
            dest.toRemove.forEach { obj ->
                obj.file().delete()
                // TODO remove directories recursively if they are Excluded completely (don't touch Mixed)
            }
            dest.toCopyHere.forEach { obj ->
                obj.file().copyTo(dest.composeTarget(obj).toFile())
            }
        }

        return null
    }
}

class ListApplication(i: Index, private val filter: String?): IndexedApplication(i) {
    private fun decodeFilter(v: String): Action? {
        return when (v) {
            "new" -> Action.Undefined
            "added" -> Action.Include
            "removed" -> Action.Exclude
            else -> null
        }
    }

    override fun work(): Error? {
        val action = decodeFilter(filter?:"new") ?: return Error("Unknown action <${filter}>")

        val launcher = Launcher(index)
        launcher.source.all().filter { it.action == action }.forEach{ println(it.fullPath()) }
        return null
    }
}

class DestinationApplication(i: Index, private val args: List<String>): IndexedApplication(i) {
    private fun decodeSubcommand(v: String): Action? {
        return when (v) {
            "add" -> Action.Include
            "remove" -> Action.Exclude
            else -> null
        }
    }

    override fun work(): Error? {
        if (args.isEmpty()) {
            return Error("No command specified")
        }

        val action = decodeSubcommand(args.first())
        val paths = args.drop(1)
        if (paths.isEmpty()) {
            return Error("No path specified")
        }
        // TODO check if paths exists

        val callback = if (action == Action.Include) {
            { list: MutableList<String>, item: String ->
                if (!list.contains(item)) {
                    println("Adding destination path: $item")
                    list.add(item)
                }
                else {
                    println("Destination is already added: $item")
                }
            }
        }
        else {
            { list: MutableList<String>, item: String -> list.remove(item) }
        }

        paths.forEach {
            callback(index.destinationPaths, it)
        }

        index.serialize()
        return null
    }
}

class SpaceApplication(i: Index, private val filter: String?): IndexedApplication(i) {
    private fun decodeFilter(v: String): Action? {
        return when (v) {
            "new" -> Action.Undefined
            "added" -> Action.Include
            "removed" -> Action.Exclude
            else -> null
        }
    }

    private fun printSize(bytes: Long) {
        val sizeMb = bytes / (1024 * 1024)
        if (0L != sizeMb) {
            println("$sizeMb Mb")
            return
        }
        val sizeKb = bytes / 1024
        if (0L != sizeKb) {
            println("$sizeKb Kb")
            return
        }
        println("$bytes b")
    }

    override fun work(): Error? {
        val action = decodeFilter(filter?:"new") ?: return Error("Unknown action <${filter}>")
        val totalSize = Launcher(index).source.all().filter { it.action == action }.fold(0) {
                acc: Long, obj: Object ->
            acc + obj.size()
        }
        printSize(totalSize)
        return null
    }
}