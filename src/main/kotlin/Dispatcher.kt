class Dispatcher(private val source: Source, private val destinations: List<Destination>) {

    fun dispatchObjects(): Error? {
        destinations.forEach { dest ->
            dest.toRemove.addAll(dest.exclusion(listOf(source)))
            // TODO drop current path instead of "drop(1)"?
            dest.toRemove.addAll(dest.all().drop(1).filter {
                val objAtSource = source.findByPath(it.path) ?: return@filter true
                return@filter when (objAtSource.action) {
                    Action.Mixed, Action.Include -> false
                    else -> true
                }
            })
        }
        source.toCopyOut.addAll(
            source.exclusion(destinations)
                .filter { it.action == Action.Include }
        )

        fun destWithMaxSpace(destinations: List<Destination>): Destination? {
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

        for (sourceFile in source.toCopyOut) {
            // TODO check for all parents, not only top one
            val destinationsWithParent = destinations.filter { dest ->
                val tpp =  sourceFile.getTopParentPath();
                tpp?.let { topParentPath ->
                    val hasParentInDest = null != dest.findByPath(topParentPath)
                    val hasParentInPlans = dest.plannedFilesContainParent(topParentPath)
                    hasParentInDest || hasParentInPlans
                } ?:false
            }
            val searchInDest = destinationsWithParent.ifEmpty { destinations }

            val toDest: Destination =
                destWithMaxSpace(searchInDest)
                ?: return Error("No destination when dispatching ${sourceFile.path}")

            toDest.toCopyHere.add(sourceFile)
            if (toDest.availableSpace().bytes() < 0) {
                return Error("No space available for dispatching ${sourceFile.fullPath()} to ${toDest.fullPath()}; ")
            }
        }

        return null
    }

    fun printPlans() {
        destinations.forEach{destination: Destination ->
            println("For destination: ${destination.fullPath()}:")
            if (destination.toRemove.isNotEmpty()) {
                println("    To remove:")
                destination.toRemove.forEach { println("        ${it.fullPath()}") }
                val removeSize = destination.toRemove.fold(FileSize(0)) {
                        acc: FileSize, obj: Object -> obj.size().onDisk() + acc
                }
                println("        In total ${destination.toRemove.size} objects, $removeSize")
            }
            if (destination.toCopyHere.isNotEmpty()) {
                println("    To copy:")
                destination.toCopyHere.forEach { println("        ${it.fullPath()}") }
                val copySize = destination.toCopyHere.fold(FileSize(0)) {
                        acc: FileSize, obj: Object -> obj.size().onDisk() + acc
                }
                println("        In total ${destination.toCopyHere.size} objects, $copySize")
            }
            println("Space left: ${destination.availableSpace()}")
        }
    }
}
