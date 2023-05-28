import interfaces.IFilesystemGate

class SyncApplication(i: Index, private val inputStr: String?, private val fileBuilder: IFilesystemGate): IndexedApplication(i) {
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

    // TODO it's possible to dispatch directory to one dest and it's files to other!
    override fun work(): Error? {
        val subCommand = decodeSubcommand(inputStr) ?: return Error("Unknown subcommand $inputStr")
        val destinations = index.getDestinations()
        val dispatcher = Dispatcher(index, destinations, fileBuilder)
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
        destinations.forEach { dest ->
            removeStrategy.addAll(dest.toRemove.asReversed().map { existingDestFile ->
                {
                    println("Removing ${existingDestFile.path} ...")
                    val success = fileBuilder.remove(existingDestFile)
                    GhostFile(index.getSource().absolutePrefix, existingDestFile.path, index).state.synced = success
                }
            })
            copyStrategy.addAll(dest.toCopyHere.map { existingDestFile -> {
                println("Copying ${existingDestFile.path} ...")
                val success = fileBuilder.copy(existingDestFile, dest.composeTarget(existingDestFile))
                GhostFile(index.getSource().absolutePrefix, existingDestFile.path, index).state.synced = success
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
                cb()
                println("${i + 1}/${copyStrategy.size}");
            }
        }

        index.save()

        return null
    }
}

