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

    init {
        println("Source initialized: ${super.toString()}")
    }

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
    val to_remove = mutableListOf<Object>()
    val to_copy_here = mutableListOf<Object>()

    init {
        println("Destination initialized: $this, space available: ${availableSpace()}")
    }

    fun composeTarget(obj: Object): Path {
        return absolutePrefix.resolve(obj.path).toAbsolutePath()
    }

    fun plannedFilesContainParent(pathToFind: Path): Boolean {
        return null != to_copy_here.find { it.getTopParentPath() == pathToFind }
    }

    private fun rawAvailableSpace(): Long {
        return fullPath().toFile().usableSpace
    }

    private fun List<Object>.totalSize(): Long {
        return this.fold(0) {acc: Long, obj: Object -> acc + obj.size() }
    }

    fun sizeAdded(): Long {
        return to_copy_here.totalSize() - to_remove.totalSize()
    }

    fun availableSpace(): Long {
        return rawAvailableSpace() - sizeAdded()
    }

    fun planendSize(): Long {
        return size() + sizeAdded()
    }
}

