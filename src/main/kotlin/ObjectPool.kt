import java.nio.file.Path

// represents all objects contained below the prefix
// abstract for source or destination
abstract class ObjectPool(prefix: Path): Object(prefix, Path.of(".")) {
    // get objects that present here, but not it other pool
    fun exclusion(others: List<ObjectPool>): List<Object> {
        return pickIf {
            others.fold(true) { acc: Boolean, other: ObjectPool -> acc && null != other.findByPath(it.path) }
        }
    }

    override fun toString(): String {
        return "ObjectPool: {${super.toString()}}"
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

    fun available_space(): Long {
        return 0L
    }

    override fun size(): Long {
        var current_size = to_copy_here.fold(super.size(), {acc: Long, it: Object -> acc + it.size() })
        current_size -= to_remove.fold(0L, {acc: Long, it: Object -> acc + it.size() })
        return current_size
    }

}

