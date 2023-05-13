import interfaces.IFilesystemGate
import java.nio.file.Path

class ScanApplication(i: Index, private val fileBuilder: IFilesystemGate): IndexedApplication(i) {

    private val destinations = index.getDestinations()
    private val ignoreDestinations = destinations
        .fold(true) { acc, it -> acc && !it.file.absolutePath().toFile().exists() }

    init {
        if (ignoreDestinations) {
            // without destinations, it would be impossible to remember what files need to be deleted from there
            println("At least one destination is unavailable. Skipping destination scan")
            destinations
                .filter { !it.file.absolutePath().toFile().exists() }
                .forEach { println("Absent destination: ${it.file.absolutePath()}") }
        }
    }

    override fun work(): Error? {
        syncSourceWithIndex()
        syncIndexWithDestinations()
        index.save()
        return null
    }

    private fun containedInDests(path: Path): Boolean {
        val res = destinations.fold(false) { acc, dest ->
            acc || dest.file.findByPath(path) != null
        }
        return res
    }

    private fun syncSourceWithIndex() {
        val (toExclude, toRemove) = index.indexedFiles()
            .filter { null == fileBuilder.build(it) }
            .partition { ignoreDestinations || containedInDests(it.path) }
        toExclude.forEach { it.state.setAction(Action.Exclude) }
        toRemove.forEach { index.permissions().remove(it.path.toString()) }

        val existingSource = fileBuilder.build(index.getSource())
        if (null == existingSource) {
            println("PANIC! Source ${index.getSourceAbsolutePath()} does not exist")
            return
        }
        existingSource.all().toList() // create from FS and init with default state
        index.permissions().remove(existingSource.path.toString())
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
                Action.Exclude -> {
                    val res = null != fileBuilder.build(it)
                    it.state.synced = res
                }
                Action.Mixed -> {}
                else -> { it.state.synced = false }
            }
        }

        destinations
            .fold(sequenceOf<ExistingFile>()) { acc, dest -> acc + dest.file.all() }
            .filter { !index.permissions().containsKey(it.path.toString()) }
            .map { GhostFile(index.getSourceAbsolutePath(), it.path, index) }
            .forEach { it.state.setAction(Action.Exclude) }
    }
}
