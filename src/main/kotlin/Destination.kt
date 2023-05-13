import interfaces.IExistingFile
import java.nio.file.Path

// TODO dont use ExistingFile as base
class Destination(prefix: Path)  {
    val file = ExistingFile(prefix, Path.of("."), null)
    val toRemove = mutableListOf<IExistingFile>()
    val toCopyHere = mutableListOf<IExistingFile>()
    // private val initialAvailableSpace = FileSize(file.absolutePath().toFile().usableSpace)
    private val initialAvailableSpace = file.usableSpace()

    fun composeTarget(sourceFile: GhostFile): GhostFile {
        return GhostFile(file.absolutePrefix, sourceFile.path, null)
    }

    fun plannedFilesContainParent(relativePath: Path): Boolean {
        return null != toCopyHere.find { toCopyFile ->
            toCopyFile.getTopParent()?.path == relativePath
        }
    }

    private fun List<IExistingFile>.totalSize(): FileSize {
        return this.fold(FileSize(0)) { acc, file -> acc + file.size() }
    }

    private fun sizeAdded(): FileSize {
        return toCopyHere.totalSize() - toRemove.totalSize()
    }

    fun availableSpace(): FileSize {
        return initialAvailableSpace - sizeAdded().onDisk()
    }
}

