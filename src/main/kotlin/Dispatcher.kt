class Dispatcher(private val source: Source, private val destinations: List<Destination>) {

    fun dispatch_objects(): String? {
        destinations.forEach{ it.to_remove.addAll(it.exclusion(listOf(source))) }
        source.toCopyOut.addAll(source.exclusion(destinations).filter { !it.isDirectory() })

        if (source.copyOutSize() < destinations.fold(0, { acc: Long, it: Destination -> acc + it.size() })) {
            return "Not enough space"
        }

        for (source_file in source.toCopyOut) {
            val toDest: Destination = destinations.find { dest ->
                dest.children?.find { it == source_file.getTopParent() } != null }
                ?: destinations.maxWithOrNull { lh, rh -> (lh.available_space() - rh.available_space()).toInt() }
                ?: return "No destination when dispatching ${source_file.path}"

            toDest.to_copy_here.add(source_file)
        }

        return null
    }

    fun print_plans() {
        println("Some dispatcher plans")
    }
}
