class Dispatcher(private val source: Source, private val destinations: List<Destination>) {

    fun dispatchObjects(): String? {
        destinations.forEach { dest ->
            dest.to_remove.addAll(dest.exclusion(listOf(source)))
            // TODO drop current path instead of "drop(1)"?
            dest.to_remove.addAll(dest.all().drop(1).filter {
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

        println("Have ${source.toCopyOut.size} files to copy from source")

        for (sourceFile in source.toCopyOut) {
            val destinationsWithParent = destinations.filter { dest ->
                sourceFile.getTopParentPath()?.let { topParentPath ->
                    null != dest.findByPath(topParentPath) || dest.plannedFilesContainParent(topParentPath)
                } ?:false
            }
            val searchInDest = destinationsWithParent.ifEmpty { destinations }

            val toDest: Destination =
                searchInDest.maxWithOrNull { lh, rh -> (lh.availableSpace() - rh.availableSpace()).toInt() }
                ?: return "No destination when dispatching ${sourceFile.path}"

            println("Dispatching file $sourceFile to ${toDest.fullPath()}")
            toDest.to_copy_here.add(sourceFile)
            if (toDest.availableSpace() < 0) {
                return "No space available for dispatching ${sourceFile.fullPath()} to ${toDest.fullPath()}"
            }
        }

        return null
    }

    fun printPlans() {
        destinations.forEach{destination: Destination ->
            println("For destination: ${destination.fullPath()}:")
            if (destination.to_remove.isNotEmpty()) {
                println("    To remove:")
                destination.to_remove.forEach { println("        ${it.fullPath()}") }
                val removeSize = destination.to_remove.fold(0) { acc: Long, obj: Object -> obj.size() + acc }
                println("        In total ${destination.to_remove.size} objects, $removeSize bytes")
            }
            if (destination.to_copy_here.isNotEmpty()) {
                println("    To copy:")
                destination.to_copy_here.forEach { println("        ${it.fullPath()}") }
                val copySize = destination.to_copy_here.fold(0) { acc: Long, obj: Object -> obj.size() + acc }
                println("        In total ${destination.to_copy_here.size} objects, $copySize bytes")
            }
            println("Space available: ${destination.availableSpace() / 1024} kbytes")
        }
    }
}
