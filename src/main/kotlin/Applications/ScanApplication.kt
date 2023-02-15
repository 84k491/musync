import java.nio.file.Path

class ScanApplication(i: Index): IndexedApplication(i) {

    private val destinations = index.getDestinations()
    private val ignoreDestinations = destinations.fold(true) { acc, it -> acc && !it.exists() }
    private val sourceFiles = index.getSource().all()

    init {
        if (ignoreDestinations) {
            // without destinations, it would be impossible to remember what files need to be deleted from there
            println("At least one destination is unavailable. Skipping destination scan")
        }
    }

    override fun work(): Error? {
        val newIndexMap = syncIndexWithDestinations(syncSourceWithIndex())
        index.setNewPermissions(newIndexMap)
        index.serialize()
        return null
    }

    private fun containedInDests(path: String): Boolean {
        return destinations.fold(false) { acc, dest -> acc || dest.findByPath(Path.of(path)) != null }
    }

    private fun syncSourceWithIndex(): Map<String, FileSyncState> {
        val newIndexMapIntermediate: Map<String, FileSyncState> =
            index.permissions
                .mapValues { (filepath, state) ->
            val matchedFileInSource = sourceFiles.find { it.fullPath().toString() == filepath }
            if (matchedFileInSource != null) {
                return@mapValues state
            }

            if (!ignoreDestinations && containedInDests(filepath)) {
                FileSyncState(Action.Exclude, false)
            } else {
                null
            }
        }.mapNotNull { (key, value) -> value?.let { key to value } }.toMap()

        val result = newIndexMapIntermediate +
                sourceFiles
                    .map { it.path.toString() }
                    .filter { !newIndexMapIntermediate.containsKey(it) }
                    .associateWith { FileSyncState(Action.Undefined, false) }.toMutableMap()
        return result
    }

    private fun syncIndexWithDestinations(newIndexMap: Map<String, FileSyncState>): Map<String, FileSyncState> {
        if (!ignoreDestinations) {
            newIndexMap.forEach {(path, state) ->
                if (Action.Mixed == state.action) { return@forEach }
                if (containedInDests(path)) {
                    when (state.action) {
                        Action.Include, Action.Mixed -> {}
                        Action.Exclude -> state.synced = false
                        Action.Undefined -> { state.action = Action.Include; state.synced = true } // rev sync
                    }
                }
                else {
                    when (state.action) {
                        Action.Exclude, Action.Mixed -> {}
                        else -> { state.synced = false }
                    }
                }
            }

            destinations
                .fold(listOf<FileWrapper>()) { acc, dest -> acc + dest.all() }
                .filter { !newIndexMap.containsKey(it.path.toString()) }
                .associate { it.path.toString() to FileSyncState(Action.Exclude, false) }
        }
        return newIndexMap
    }
}
