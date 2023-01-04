import java.nio.file.Path

abstract class ObjectPool(prefix: Path): Object(prefix, Path.of(".")) {
    fun exclusion(others: List<ObjectPool>): List<Object> {
        val ex = all().filter { thisObject ->
            others.fold(true) {acc, objectPool ->
                acc && null == objectPool.findByPath(thisObject.path)
            }
        }
        return ex
    }
}

class Source(prefix: Path) : ObjectPool(prefix) {
    val toCopyOut = mutableListOf<Object>()

    fun copyOutSize(): Long {
        return toCopyOut.fold(super.size()) { acc: Long, it: Object -> acc + it.size() }
    }
    init {
        println("Source initialized: ${super.toString()}")
    }

    fun updatePermissionsGetUndef(permissions: Map<String, Action>): List<Object> {
        val undefined = mutableListOf<Object>()
        foreach { file ->
//            println("Updating permissions for file: ${file.toString()}")
            permissions[file.path.toString()].let { perm ->
                if (null != perm) {
                    file.action = perm
                }
                else {
                    undefined.add(file)
                }
            }
        }
        return undefined
    }

    fun getPermissions(): Map<String, Action> {
        return all().filter { !it.isDirectory() }.associateBy ( {it.path.toString()}, {it.action} )
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

