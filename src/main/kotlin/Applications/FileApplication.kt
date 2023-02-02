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

        val launcher = Launcher(index)
        val files = args.drop(1)
        // TODO there is no need to set up the whole source.
        // create a path, check if it exists, walk and apply the action if it's a directory, push new policies
        files.forEach {
            val pathToFind = cwd.resolve(Path(it)).toAbsolutePath().normalize()
            val file = launcher.source.findByPath(pathToFind.relativeTo(launcher.source.fullPath()))
                ?: return@work Error("There is no such file: $it")
            file.setActionRecursively(action)
        }

        index.updatePermissions(launcher.source.getPermissions().filter {
            it.value != Action.Undefined &&
                    it.value != Action.Mixed
        })
        index.serialize()
        return null
    }
}

