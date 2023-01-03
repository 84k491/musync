import java.io.File
import java.nio.file.Path
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

    private fun fullPath(): Path {
        return absolutePrefix.resolve(path)
    }

    fun isDirectory(): Boolean {
        return File(fullPath().toString()).isDirectory // TODO refactor
    }

    fun getTopParent(): Object? {
        // TODO implement
        return null
    }

    fun pickIf(pickCondition: (Object) -> Boolean): List<Object> {
        val result = mutableListOf<Object>()
        foreach { if (pickCondition(it)) { result.add(it) } }
        return result
    }

    // TODO think about moving the whole directory (need to store only relative paths in the file)
    fun findByPath(path: Path): Object? {
        val checkSingleObject = { it: Object ->
            it.fullPath() == path
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
        return 0L // TODO implement
    }

    override fun toString(): String {
        return "Object: {$absolutePrefix + $path}"
    }
}
