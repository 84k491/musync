class ListApplication(i: Index, private val filter: String?): IndexedApplication(i) {
    private fun decodeFilter(v: String): Action? {
        return when (v) {
            "new" -> Action.Undefined
            "added" -> Action.Include
            "removed" -> Action.Exclude
            else -> null
        }
    }

    private val allFilter = "all"
    private val unsyncedFilter = "unsynced"
    private val syncedFilter = "synced"

    override fun work(): Error? {
        var files = index.indexedFiles()

        files = when (filter) {
            allFilter -> files
            syncedFilter -> files.filter { it.state.synced }
            unsyncedFilter -> files.filterNot { it.state.synced }
            else -> {
                val action = decodeFilter(filter ?: "new") ?: return Error("Unknown action <${filter}>")
                files.filter { it.state.getAction() == action }
            }
        }

        files.forEach { println(it) }
        return null
    }
}

