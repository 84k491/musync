import java.nio.file.Path

// TODO dont use ExistingFile as base
class Destination(prefix: Path) : ExistingFile(prefix, Path.of("."), null) {
    val toRemove = mutableListOf<ExistingFile>()
    val toCopyHere = mutableListOf<ExistingFile>()
    private val initialAvailableSpace = FileSize(absolutePath().toFile().usableSpace)

    fun composeTarget(sourceFile: GhostFile): GhostFile {
        return GhostFile(absolutePrefix, sourceFile.path, null)
    }

    fun plannedFilesContainParent(relativePath: Path): Boolean {
        return null != toCopyHere.find { toCopyFile ->
            toCopyFile.getTopParent()?.path == relativePath
        }
    }

    private fun List<ExistingFile>.totalSize(): FileSize {
        return this.fold(FileSize(0)) { acc, file -> acc + file.size() }
    }

    private fun sizeAdded(): FileSize {
        return toCopyHere.totalSize() - toRemove.totalSize()
    }

    fun availableSpace(): FileSize {
        return initialAvailableSpace - sizeAdded().onDisk()
    }
}

