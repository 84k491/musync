package tests.mocks

import Destination
import FileSyncState
import GhostFile
import interfaces.IIndex
import java.nio.file.Path

class MockIndex : IIndex {

    val permissions = mutableMapOf<String, FileSyncState>()
    override fun permissions(): MutableMap<String, FileSyncState> {
        return permissions
    }
    override fun indexedFiles(): Sequence<GhostFile> {
        // TODO it's all the same as in real class
        return permissions
            .map { (pathString, _) -> GhostFile(getSourceAbsolutePath(), Path.of(pathString), this) }
            .asSequence()
    }
    override fun getSource(): GhostFile {
        return GhostFile(Path.of("absolute"), Path.of("relative"), this)
    }
    override fun getSourceAbsolutePath(): Path { return Path.of(".") }
    override fun getDestinations(): List<Destination> {
        return listOf()
    }
    override fun save() {}
}