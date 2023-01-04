import java.io.File
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.relativeTo

// TODO rename FileExt
open class Object(val absolutePrefix: Path, val path: Path) {
    var action = Action.Undefined
        set(v) {
            if (isDirectory()) {
                children.forEach{ it.action = v }
            }
            if (field != v) {
                println("Setting action $v for file: ${fullPath()}")
                field = v
            }
            else {
                println("Nothing to change for file: ${fullPath()}")
            }
        }

    var children: List<Object> = fullPath().toFile().listFiles()?.
            map { Object(absolutePrefix, it.toPath().relativeTo(absolutePrefix)) }?: listOf()

    fun fullPath(): Path {
        return absolutePrefix.resolve(path)
    }

    fun file(): File {
        return fullPath().toFile()
    }

    fun isDirectory(): Boolean {
        return File(fullPath().toString()).isDirectory
    }

    fun getTopParentPath(): Path? {
        println("Finding top parent for $path (name count = ${path.nameCount}) ${fullPath()}")
        var currentPath = path
        while (currentPath.nameCount > 1) {
            println("path on current interation: $currentPath")
            currentPath = currentPath.parent
        }
        val result = if (currentPath.isDirectory()) currentPath else null
        println("Top parent is: $result")
        return result
    }

    fun findByPath(path: Path): Object? {
        val checkSingleObject = { it: Object ->
            it.path == path
        }

        if (checkSingleObject(this)) {
            return this
        }
        else {
            children.forEach{if (checkSingleObject(it)) return@findByPath it }
            return null
        }
    }

    fun all(): List<Object> {
        val res = mutableListOf<Object>()
        foreach { res.add(it) }
        return res
    }

//     TODO is it still needed? There is "all()" method
    fun foreach(cb: (Object) -> Unit) {
        cb(this)
        if (isDirectory()) {
            children.forEach{ it.foreach(cb) }
        }
    }

    open fun size(): Long {
        return fullPath().toFile().length() + children.fold(0) {acc: Long, obj: Object -> acc + obj.size() }
    }

    override fun toString(): String {
        val type = if (isDirectory()) "dir" else "file"
        return "Object: {${fullPath()}; $type; size = ${size()}}"
    }
}
