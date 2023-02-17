import java.nio.file.Path

class ScanApplication(i: Index): IndexedApplication(i) {

    private val destinations = index.getDestinations()
    private val ignoreDestinations = destinations
        .fold(true) { acc, it -> acc && !it.absolutePath().toFile().exists() }

    init {
        if (ignoreDestinations) {
            // without destinations, it would be impossible to remember what files need to be deleted from there
            println("At least one destination is unavailable. Skipping destination scan")
            destinations
                .filter { !it.absolutePath().toFile().exists() }
                .forEach { println("Absent destination: ${it.absolutePath()}") }
        }
    }

    override fun work(): Error? {
        syncSourceWithIndex()
        syncIndexWithDestinations()
        index.serialize()
        return null
    }

    private fun containedInDests(path: Path): Boolean {
        val res = destinations.fold(false) { acc, dest ->
            acc || dest.findByPath(path) != null
        }
        return res
    }

    private fun syncSourceWithIndex() {
        val (toExclude, toRemove) = index.indexedFiles()
            .filter { null == it.toExisting() }
            .partition { ignoreDestinations || containedInDests(it.path) }
        toExclude.forEach { it.state.setAction(Action.Exclude) }
        toRemove.forEach { index.permissions.remove(it.path.toString()) }

        val existingSource = index.getSource().toExisting()
        if (null == existingSource) {
            println("PANIC! Source ${index.getSourceAbsolutePath()} does not exist")
            return
        }
        existingSource.all().toList() // create from FS and init with default state
    }

    private fun syncIndexWithDestinations() {
        if (ignoreDestinations) {
            return
        }
        val (contained, notContained) = index.indexedFiles()
            .filter { it.state.getAction() != Action.Mixed }
            .partition { containedInDests(it.path) }

        contained
            .forEach {
                when (it.state.getAction()) {
                    Action.Include, Action.Mixed -> {}
                    Action.Exclude -> it.state.synced = false
                    Action.Undefined -> {
                        it.state.setAction(Action.Include); it.state.synced = true // rev sync
                    }
                }
            }

        notContained.forEach {
            when (it.state.getAction()) {
                Action.Exclude -> { it.state.synced = null != it.toExisting() }
                Action.Mixed -> {}
                else -> { it.state.synced = false }
            }
        }

        destinations
            .fold(sequenceOf<ExistingFile>()) { acc, dest -> acc + dest.all() }
            .filter { !index.permissions.containsKey(it.path.toString()) }
            .map { GhostFile(index.getSourceAbsolutePath(), it.path, index) }
            .forEach { it.state.setAction(Action.Exclude) }
    }
}
