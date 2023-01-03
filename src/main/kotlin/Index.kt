import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.lang.Exception

class Index(val cwd: Path, val file: File) {

    companion object Builder {
        fun load(cwd: Path): Index? {
            var dir = cwd
            while (0 != dir.nameCount) {
                val filePath = dir.toFile().list()?.find { it.endsWith(filename()) }
                if (null != filePath) {
                    // TODO make all the checks for this file here
                    return Index(cwd, File(filePath))
                }
                dir = dir.parent
            }
            return null
        }
        fun create(cwd: Path): String? {
            val file = File(filename())
            if (!file.createNewFile()) {
                return null
            }
            return file.path
        }
        private fun filename(): String {
            return "synchronizer_index.txt"
        }
    }

    val destinationPaths = mutableListOf<Path>()
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
            updatePermissions(Json.decodeFromStream<Map<String, Action>>(file.inputStream())) // TODO move it to init App
        }
        catch (e: Exception) {
            serialize()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun serialize() {
        Json.encodeToStream(permissions, file.outputStream())
    }
}