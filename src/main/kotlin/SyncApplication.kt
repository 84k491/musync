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

