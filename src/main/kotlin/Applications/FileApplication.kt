import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.relativeTo

class FileApplication(private val cwd: Path, i: Index, private val args: List<String>): IndexedApplication(i) {
    private fun decodeAction(str: String?): Action? {
        if (null == str) {
            return null
        }
        return when (str) {
            "add" -> Action.Include
            "remove" -> Action.Exclude
            else -> null
        }
    }

    override fun work(): Error? {
        val action = decodeAction(args[0]) ?: return Error("Unknown action <${args[0]}>")

        val inputFiles = args.drop(1).asSequence().map { cwd.resolve(Path(it)).toAbsolutePath().normalize() }
        inputFiles.forEach { if (!it.toFile().exists()) { return@work Error("There is no such file: $it")} }

        // TODO create paths in one place! // make an object factory with index?
        val inputObjects = inputFiles.map { Object(index.sourceFullPath(), it.relativeTo(index.sourceFullPath())) }

        val objectsToUpdate = inputObjects.map { it.all() }.flatten()
        objectsToUpdate.forEach {
            val state = index.permissions[it.path.toString()]
            it.action = state?.action ?: Action.Undefined
            it.isSynced = state?.synced ?: false

            if (action != it.action) {
                it.isSynced = false
            }
            it.action = action
        }

        val newPermissions = objectsToUpdate.associate { it.path.toString() to FileSyncState(action) }
        index.permissions.putAll(newPermissions)
        index.serialize()
        return null
    }
}

