import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.relativeTo

abstract class Application {
    abstract fun work(): Error?

}
abstract class IndexedApplication(val index: Index): Application()

class HelpApplication: Application() {
    override fun work(): Error? {
        println("Musync is a tool for synchronizing music files from a single storage to several destinations.\n" +
                "Files must be added to index file first\n" +
                "\n" +
                "Usage:\n" +
                "musync init initialize (current) directory with empty index file\n" +
                "\n" +
                "musync file [flag] <file>: change policy for files\n" +
                "            |\n" +
                "            |-> add : Mark file as wanted to be present at destinations\n" +
                "            |-> remove : Mark file as unwanted at destinations\n" +
                "\n" +
                "musync list [optional flag] : list files of the specified type\n" +
                "            |\n" +
                "            |-> new : Default. Files that are not added to index\n" +
                "            |-> added : Files that will be copied to destinations\n" +
                "            |-> removed : Files that mustn't present in destinations\n" +
                "\n" +
                "musync space [<optional flag] : get overall size of files with specified type\n" +
                "             |\n" +
                "             |-> new : Default. Size of not indexed files\n" +
                "             |-> added : Size of files that will be copied\n" +
                "             |-> removed : Size of files that are marked as unwanted\n" +
                "\n" +
                "musync destination [flag] <path(s)>: Add or remove destination\n" +
                "                   |\n" +
                "                   |-> add : Remember a path to be a destination\n" +
                "                   |-> remove : Forget a destination\n" +
                "\n" +
                "musync sync [optional flag] : perform the synchronization (ask to copy)\n" +
                "            |\n" +
                "            |-> --ask : Default. Shows plans and ask before further actions\n" +
                "            |-> --dry : Dry run. Shows plans and exits\n" +
                "            |-> --force : Shows plans and proceeds with actions\n" +
        "")
        return null
    }
}

class FileApplication(private val cwd: Path, i: Index, private val args: List<String>): IndexedApplication(i) {
    private fun decodeAction(str: String?): Action? {
        if (null == str) {
            return null
        }
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
        // TODO there is no need to set up the whole source.
        // create a path, check if it exists, walk and apply the action if it's a directory, push new policies
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

class SyncApplication(i: Index, private val inputStr: String?): IndexedApplication(i) {
    enum class SubCommand {
        Ask,
        DryRun,
        Force,
    }

    private val subCommandMap = mapOf(
            Pair("--ask", SubCommand.Ask),
            Pair("--force", SubCommand.Force),
            Pair("--dry", SubCommand.DryRun),
        )

    private fun decodeSubcommand(str: String?): SubCommand? {
        if (null == str) {
            return SubCommand.Ask
        }
        return subCommandMap[str]
    }

    override fun work(): Error? {
        val subCommand = decodeSubcommand(inputStr) ?: return Error("Unknown subcommand $inputStr")

        val launcher = Launcher(index)

        val dispatcher = Dispatcher(launcher.source, launcher.destinations)
        dispatcher.dispatchObjects()?.let { return@work it }
        dispatcher.printPlans()

        when (subCommand) {
            // TODO implement asking
            SubCommand.Ask -> { println("Doing nothing. Use '--force' flag to remove and copy files"); return null }
            SubCommand.DryRun -> { return null }
            SubCommand.Force -> { /*continue*/ }
        }

        val copyStrategy: MutableList<()->Unit> = mutableListOf()
        val removeStrategy: MutableList<()->Unit> = mutableListOf()
        launcher.destinations.forEach { dest ->
            // TODO remove directories recursively if they are Excluded completely (don't touch Mixed)
            removeStrategy.addAll(dest.toRemove.map { obj -> { obj.file().delete() } })
            copyStrategy.addAll(dest.toCopyHere.map { obj -> {
                obj.file().copyTo(dest.composeTarget(obj).toFile())
            } })
        }

        if (copyStrategy.isEmpty() && removeStrategy.isEmpty()) {
            println("No files to copy or remove");
        }
        if (removeStrategy.isNotEmpty()) {
            println("Removing...")
            removeStrategy.forEachIndexed { i, cb ->
                println("${i + 1}/${removeStrategy.size}");
                cb()
            }
        }
        if (copyStrategy.isNotEmpty()) {
            println("Copying...")
            copyStrategy.forEachIndexed { i, cb ->
                println("${i + 1}/${copyStrategy.size}");
                cb()
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

    override fun work(): Error? {
        val action = decodeFilter(filter?:"new") ?: return Error("Unknown action <${filter}>")
        val totalSize = Launcher(index).source.all().filter { it.action == action }.fold(FileSize(0)) {
                acc: FileSize, obj: Object ->
            acc + obj.size().onDisk()
        }
        println(totalSize)
        return null
    }
}
