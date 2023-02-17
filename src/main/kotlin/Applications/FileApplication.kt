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
    private val allNewFlag = "--new"

    override fun work(): Error? {
        val action = decodeAction(args[0]) ?: return Error("Unknown action <${args[0]}>")

        println("Checking for flag..")
        val inflatedWithFlag: Sequence<GhostFile> =
            if (args.contains(allNewFlag)) {
                index
                    .indexedFiles()
                    .filter { Action.Undefined == it.state.getAction() }
            }
            else {
                sequenceOf()
            }

        println("Continue with regular input files")
        val filesToProcess = args
            .asSequence()
            .drop(1)
            .filter { allNewFlag != it }
            .map { cwd.resolve(Path(it)).toAbsolutePath().normalize() }
            .map { GhostFile(it, index) } +
                inflatedWithFlag
        filesToProcess
            .forEach { it.state.setAction(action) }

        println("Saving...")

        index.serialize()
        return null
    }
}

