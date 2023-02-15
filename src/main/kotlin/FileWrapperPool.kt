import java.nio.file.Path

abstract class FileWrapperPool(prefix: Path): FileWrapper(prefix, Path.of(".")) {
    fun exclusion(others: List<FileWrapperPool>): List<FileWrapper> {
        // TODO drop current path instead of "drop(1)"?
        val ex = all().drop(1).filter { thisObject ->
            others.fold(true) {acc, objectPool ->
                val obj = objectPool.findByPath(thisObject.path)
                acc && null == obj
            }
        }

        return ex
    }
}

class Source(prefix: Path) : FileWrapperPool(prefix) {
    val toCopyOut = mutableListOf<FileWrapper>()

    fun updatePermissionsGetUndef(permissions: Map<String, FileSyncState>): List<FileWrapper> {
        val undefined = mutableListOf<FileWrapper>()
        foreach { file ->
            permissions[file.path.toString()].let { perm ->
                if (null != perm) {
                    file.action = perm.action
                    file.isSynced = perm.synced
                }
                else {
                    undefined.add(file)
                }
            }
        }

        updateDirPermissions()

        return undefined
    }
}

class Destination(prefix: Path) : FileWrapperPool(prefix) {
    val toRemove = mutableListOf<FileWrapper>()
    val toCopyHere = mutableListOf<FileWrapper>()
    private val initialAvailableSpace = FileSize(fullPath().toFile().usableSpace)

    fun composeTarget(obj: FileWrapper): Path {
        return absolutePrefix.resolve(obj.path).toAbsolutePath()
    }

    fun plannedFilesContainParent(pathToFind: Path): Boolean {
        return null != toCopyHere.find { it.getTopParentPath() == pathToFind }
    }

    private fun List<FileWrapper>.totalSize(): FileSize {
        return this.fold(FileSize(0)) { acc: FileSize, obj: FileWrapper -> acc + obj.size() }
    }

    private fun sizeAdded(): FileSize {
        return toCopyHere.totalSize() - toRemove.totalSize()
    }

    fun availableSpace(): FileSize {
        return initialAvailableSpace - sizeAdded().onDisk()
    }
}

