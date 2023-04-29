import interfaces.IIndex
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.lang.Exception
import kotlin.io.path.relativeTo

class Index(val file: File) : IIndex {
    companion object Builder {
        fun load(cwd: Path): Index? {
            var dir = cwd
            while (0 != dir.nameCount) {
                val filePath = dir.resolve(filename())
                val possibleFile = filePath.toFile()
                if (possibleFile.exists()) {
                    // TODO make all the checks for this file here
                    return Index(possibleFile)
                }
                dir = dir.parent
            }
            return null
        }
        fun create(): File? {
            val file = File(filename())
            if (!file.createNewFile()) {
                println("Can't create an index file")
                return null
            }
            return file
        }
        private fun filename(): String {
            return "synchronizer_index.txt"
        }
    }

    val destinationPaths = mutableListOf<String>()
    private val permissions = mutableMapOf<String, FileSyncState>()

    init {
        deserialize()
    }

    override fun permissions(): MutableMap<String, FileSyncState> {
        return permissions
    }

    override fun indexedFiles(): Sequence<GhostFile> {
        return permissions
            .map { (pathString, _) -> GhostFile(getSourceAbsolutePath(), Path.of(pathString), this) }
            .asSequence()
    }

    override fun getSource(): GhostFile {
        return GhostFile(file.toPath().toAbsolutePath().parent, this)
    }
    override fun getSourceAbsolutePath(): Path {
        return file.toPath().toAbsolutePath().parent
    }

    override fun getDestinations(): List<Destination> {
        return destinationPaths.map { Destination(Path.of(it)) }
    }

    private fun setNewPermissions(updatedPermissions: Map<String, FileSyncState>) {
        permissions.clear()
        permissions.putAll(updatedPermissions)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun deserialize() {
        try {
            val pair: Pair<List<String>, Map<String, FileSyncState>> = Json.decodeFromStream(file.inputStream())
            setNewPermissions(pair.second) // TODO move it to init App
            destinationPaths.addAll(pair.first)
        }
        catch (e: Exception) {
            serialize()
        }
    }

    override fun save() { serialize() }
    @OptIn(ExperimentalSerializationApi::class)
    private fun serialize() {
        listOf("", ".").forEach { permissions.remove(it) }
        val pair: Pair<List<String>, Map<String, FileSyncState>> = Pair(destinationPaths, permissions)
        Json.encodeToStream(pair, file.outputStream())
    }
}
