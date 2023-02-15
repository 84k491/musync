import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.lang.Exception

/*
    "action" stands for what we want it to be. And "synced" marks if current state meets our expectations

    present in source, absent in any dest, we want it to copy to any dest
    {action: Include, synced = false}

    present in source, present in ONE dest, we want to DO NOTHING
    {action: Include, synced = true}

    present in source, absent in any dest, we don't know what to do yet
    {action: Undefined, synced = false}

    invalid action!
    {action: Undefined, synced = true}

    present in source, absent in any dest, we DON'T want it to be at dest
    {action: Exclude, synced = true}

    ABSENT/PRESENT in source, present in one dest, we want it to be removed from dest
    {action: Exclude, synced = false}

    present in source, present in MANY dests, we want to keep just one of them // not implemented
    absent in source, present in any dest, we want it to be copied in source // not implemented
* */

@Serializable
class FileSyncState(
    var action: Action = Action.Undefined,
    var synced: Boolean = false,
    )

class Index(val file: File) {
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
    val permissions = mutableMapOf<String, FileSyncState>()

    init {
        deserialize()
    }

    fun getSource(): Source {
        return Source(file.toPath().toAbsolutePath().parent)
    }

    fun getDestinations(): List<Destination> {
        return destinationPaths.map { Destination(Path.of(it)) }
    }

    fun setNewPermissions(updatedPermissions: Map<String, FileSyncState>) {
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

    @OptIn(ExperimentalSerializationApi::class)
    fun serialize() {
        val pair: Pair<List<String>, Map<String, FileSyncState>> = Pair(destinationPaths, permissions)
        Json.encodeToStream(pair, file.outputStream())
    }
}
