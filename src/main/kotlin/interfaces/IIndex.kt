package interfaces

import Destination
import FileSyncState
import GhostFile
import java.nio.file.Path

interface IIndex {
    fun permissions(): MutableMap<String, FileSyncState>
    fun indexedFiles(): Sequence<GhostFile>
    fun getSource(): GhostFile
    fun getSourceAbsolutePath(): Path
    fun getDestinations(): List<Destination>
    fun save()
}