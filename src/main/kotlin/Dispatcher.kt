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

        for (sourceFile in source.toCopyOut) {
            val destinationsWithParent = destinations.filter { dest ->
                sourceFile.getTopParentPath()?.let { topParentPath ->
                    null != dest.findByPath(topParentPath) || dest.plannedFilesContainParent(topParentPath)
                } ?:false
            }
            val searchInDest = destinationsWithParent.ifEmpty { destinations }

            val toDest: Destination =
                searchInDest.maxWithOrNull { lh, rh -> (lh.availableSpace() - rh.availableSpace()).toInt() }
                ?: return Error("No destination when dispatching ${sourceFile.path}")

            toDest.toCopyHere.add(sourceFile)
            if (toDest.availableSpace() < 0) {
                return Error("No space available for dispatching ${sourceFile.fullPath()} to ${toDest.fullPath()}")
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
                val removeSize = destination.toRemove.fold(0) { acc: Long, obj: Object -> obj.size() + acc }
                println("        In total ${destination.toRemove.size} objects, $removeSize bytes")
            }
            if (destination.toCopyHere.isNotEmpty()) {
                println("    To copy:")
                destination.toCopyHere.forEach { println("        ${it.fullPath()}") }
                val copySize = destination.toCopyHere.fold(0) { acc: Long, obj: Object -> obj.size() + acc }
                println("        In total ${destination.toCopyHere.size} objects, $copySize bytes")
            }
            println("Space left: ${destination.availableSpace() / 1024} kbytes")
        }
    }
}
