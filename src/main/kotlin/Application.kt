import java.nio.file.Path

abstract class Application {
    abstract fun work(): Int
}

abstract class WorkingApplication(val cwd: Path): Application() {
}

class HelpApplication: Application() {
    override fun work(): Int {
        println("" +
                "musync init <path>: initialize (current) directory with empty index file \n" +
                "musync list [added|excluded|(default)undefined]: list files of the specified type \n" +
                "musync destination [add|remove]: add or remove destination \n" +
                "musync file [add|remove] <file(s)>: change permission of file(s)\n" +
                "musync sync: perform the synchronization (ask to copy)\n")
        return 0 // TODO other return values?
    }
}

class FileApplication(cwd: Path, private val args: List<String>): WorkingApplication(cwd) {
    fun decodeAction(str: String): Action? {
        return when (str) {
            "add" -> Action.Include
            "remove" -> Action.Exclude
            else -> null
        }
    }

    override fun work(): Int {

        val action = decodeAction(args[0])
        if (null == action) {
            println("Unknown action <${args[0]}>")
            return -1
        }

        val index = Index.load(cwd)
        if (null == index) {
            println("Can't find index file in <$cwd> or above. You need to create one first with 'init'")
            return -1
        }

        val launcher = Launcher(index)
        args.drop(1).forEach {
            val file = launcher.source.findByPath(Path.of(it))
            if (null == file) {
                println("There is no such file: $it")
                return@work -1
            }
            file.action = action
        }

        index.updatePermissions(launcher.source.getPermissions().filter {
            it.value != Action.Undefined &&
            it.value != Action.Mixed
        })
        index.serialize()
        return 0
    }
}

class InitApplication(cwd: Path): WorkingApplication(cwd) {
    override fun work(): Int {
        val i = Index.load(cwd)
        if (null != i) {
            println("Init failed. There is an index file already: ${i.file.path}")
            return -1
        }

        val filePath = Index.create(cwd)
        return if (null != filePath) {
            println("Index file created: $filePath")
            0
        } else {
            println("Failed to create index file in $cwd")
            -1
        }
    }
}

class SyncApplication(cwd: Path, private val args: List<String>): WorkingApplication(cwd) {
    enum class SubCommand {
        Ask,
        DryRun,
        Force,
    }

    override fun work(): Int {
        val sc = if (args.isNotEmpty()) {
             SubCommand.valueOf(args[0])
        }
        else {
            SubCommand.Ask
        }
        // TODO make a check if args are incorrect
        sync(sc)
        return 0
    }

    private fun sync(subCommand: SubCommand): Int {
        val index = Index.load(cwd)
        if (null == index) {
            println("Can't find index file in <$cwd> or above. You need to create one first with 'init'")
            return -1
        }

        val launcher = Launcher(index)
        launcher.checkForSyncAndGetError()?.let { println(it); return@sync -1 }

        val dispatcher = Dispatcher(launcher.source, launcher.destinations)
        dispatcher.print_plans()

        if (SubCommand.DryRun == subCommand) {
            return 0
        }

        if (SubCommand.Ask == subCommand) {
            // ask
            // if no, then return
            return 0
        }

        // TODO copy
        println("Copying (fake)")

        return 0
    }
}

class ListApplication(cwd: Path): WorkingApplication(cwd) {
    override fun work(): Int {
        return 0
    }
}
