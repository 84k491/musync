class Dispatcher(private val source: Source, private val destinations: List<Destination>) {

    fun dispatchObjects(): String? {
        destinations.forEach{ dest ->
            dest.to_remove.addAll(dest.exclusion(listOf(source)).filter { !it.isDirectory() }) }
        source.toCopyOut.addAll(
            source.exclusion(destinations)
                .filter { !it.isDirectory() }
                .filter { it.action == Action.Include }
        )

//        val totalSizeAvailable = destinations.fold(0) { acc: Long, it: Destination -> acc + it.availableSpace() }
//        if (source.copyOutSize() > totalSizeAvailable) {
//            return "Not enough space: ${source.copyOutSize()} bytes to copy, $totalSizeAvailable bytes available"
//        }

        println("Have ${source.toCopyOut.size} files to copy")

        for (sourceFile in source.toCopyOut) {
            val toDest: Destination = destinations.find { dest ->
                dest.children.find { it == sourceFile.getTopParent() } != null }
                ?: destinations.maxWithOrNull { lh, rh -> (lh.availableSpace() - rh.availableSpace()).toInt() }
                ?: return "No destination when dispatching ${sourceFile.path}"

            println("Dispatching file $sourceFile to ${toDest.fullPath()}")
            toDest.to_copy_here.add(sourceFile)
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
