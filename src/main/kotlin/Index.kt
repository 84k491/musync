import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.lang.Exception

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
    val permissions = mutableMapOf<String, Action>()

    init {
        deserialize()
    }

    fun updatePermissions(updatedPermissions: Map<String, Action>) {
        permissions.clear()
        permissions.putAll(updatedPermissions)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun deserialize() {
        try {
            val pair: Pair<List<String>, Map<String, Action>> = Json.decodeFromStream(file.inputStream())
            updatePermissions(pair.second) // TODO move it to init App
            destinationPaths.addAll(pair.first)
        }
        catch (e: Exception) {
            serialize()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun serialize() {
        val pair: Pair<List<String>, Map<String, Action>> = Pair(destinationPaths, permissions)
        Json.encodeToStream(pair, file.outputStream())
    }
}