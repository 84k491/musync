import java.nio.file.Path

abstract class ObjectPool(prefix: Path): Object(prefix, Path.of(".")) {
    fun exclusion(others: List<ObjectPool>): List<Object> {
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

class Source(prefix: Path) : ObjectPool(prefix) {
    val toCopyOut = mutableListOf<Object>()

    fun updatePermissionsGetUndef(permissions: Map<String, Action>): List<Object> {
        val undefined = mutableListOf<Object>()
        foreach { file ->
            permissions[file.path.toString()].let { perm ->
                if (null != perm) {
                    file.action = perm
                }
                else {
                    undefined.add(file)
                }
            }
        }

        updateDirPermissions()

        return undefined
    }

    fun getPermissions(): Map<String, Action> {
        return all().filter { !it.isDirectory() }.associateBy ( {it.path.toString()}, {it.action} )
    }

    fun excluded(): List<Object> {
        return all().filter { it.action == Action.Exclude }
    }
}

class Destination(prefix: Path) : ObjectPool(prefix) {
    val toRemove = mutableListOf<Object>()
    val toCopyHere = mutableListOf<Object>()
    private val initialAvailableSpace = FileSize(fullPath().toFile().usableSpace)

    fun composeTarget(obj: Object): Path {
        return absolutePrefix.resolve(obj.path).toAbsolutePath()
    }

    fun plannedFilesContainParent(pathToFind: Path): Boolean {
        return null != toCopyHere.find { it.getTopParentPath() == pathToFind }
    }

    private fun List<Object>.totalSize(): FileSize {
        return this.fold(FileSize(0)) { acc: FileSize, obj: Object -> acc + obj.size() }
    }

    private fun sizeAdded(): FileSize {
        return toCopyHere.totalSize() - toRemove.totalSize()
    }

    fun availableSpace(): FileSize {
        return initialAvailableSpace - sizeAdded().onDisk()
    }
}

