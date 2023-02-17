class Dispatcher(val index: Index, private val source: ExistingFile, private val destinations: List<Destination>) {

    // general use case:
    // index file is updated recently
    // this will be used right before actual copying
    // all files are scanned by now, no need to search for exclusions in destinations
    fun dispatchObjects(): Error? {
        val sourceFiles = index.indexedFiles()
        val unsynced = sourceFiles.filter { !it.state.synced }

        val toRemoveFromDestinations = unsynced
            .filter { Action.Exclude == it.state.getAction() }
        toRemoveFromDestinations
            .forEach { ghostFile ->
                val targetDest = destinations
                    .find { dest -> null != dest.findByPath(ghostFile.path) }
                    ?: // no need to touch anything, it's a general scenario
                    return@forEach

                val targetGhost = targetDest.composeTarget(ghostFile)
                val targetFile = targetGhost.toExisting()

                if (null == targetFile) {
                    println("No file $targetGhost in $targetDest")
                    return@forEach
                }
                targetDest.toRemove.add(targetFile)
            }

        val toCopyToDestinations = unsynced
            .filter { Action.Include == it.state.getAction() }
            .mapNotNull {
                val e = it.toExisting()
                if (null == e) {
                    println("File $it does not exist!")
                }
                e
            }

        for (sourceFile in toCopyToDestinations) {
            val targetDestination = pickDestinationForFile(sourceFile)
                ?: return Error("No destination when dispatching ${sourceFile.path}")

            targetDestination.toCopyHere.add(sourceFile)
            if (targetDestination.availableSpace().bytes() < 0) {
                return Error(
                    "No space available for dispatching " +
                            "${sourceFile.absolutePath()} to ${targetDestination.absolutePath()}; ")
            }
        }

        return null
    }

    private fun pickDestinationForFile(file: ExistingFile): Destination? {
        // TODO check for all parents, not only top one
        val destinationsWithParent = destinations.filter { dest ->
            val tpp = file.getTopParent()?.path
            tpp?.let { topParentPath ->
                val hasParentInDest = null != dest.findByPath(topParentPath)
                val hasParentInPlans = dest.plannedFilesContainParent(topParentPath)
                hasParentInDest || hasParentInPlans
            } ?:false
        }
        val searchInDest = destinationsWithParent.ifEmpty { destinations }
        return destWithMaxSpace(searchInDest)
    }

    private fun destWithMaxSpace(destinations: List<Destination>): Destination? {
        var res: Destination? = null
        var maxSize = 0L
        destinations.forEach {
            val currentSize = it.availableSpace().bytes()
            if (currentSize > maxSize) {
                maxSize = currentSize
                res = it
            }
        }
        return res
    }


    fun printPlans() {
        destinations.forEach{destination: Destination ->
            println("For destination: ${destination.absolutePath()}:")
            if (destination.toRemove.isNotEmpty()) {
                println("    To remove:")
                destination.toRemove.forEach { println("        ${it.absolutePath()}") }
                val removeSize = destination.toRemove.fold(FileSize(0)) {
                        acc: FileSize, obj: ExistingFile -> obj.size().onDisk() + acc
                }
                println("        In total ${destination.toRemove.size} objects, $removeSize")
            }
            if (destination.toCopyHere.isNotEmpty()) {
                println("    To copy:")
                destination.toCopyHere.forEach { println("        ${it.absolutePath()}") }
                val copySize = destination.toCopyHere.fold(FileSize(0)) {
                        acc: FileSize, obj: ExistingFile -> obj.size().onDisk() + acc
                }
                println("        In total ${destination.toCopyHere.size} objects, $copySize")
            }
            println("Space left: ${destination.availableSpace()}")
        }
    }
}
