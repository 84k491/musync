import java.io.File
import java.nio.file.Path

// TODO rename FileExt
open class Object(val prefix: Path, inputPath: Path) {
    var path: Path = inputPath.resolve(prefix)
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
            map { Object(prefix, it.toPath()) }?: listOf()

    fun fullPath(): Path {
        return prefix.resolve(path)
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

    fun findByPath(path: Path): Object? {
        val checkSingleObject = { it: Object -> it.path == path}
        if (checkSingleObject(this)) {
            return this
        }
        else {
            for (it in children) {
                if (checkSingleObject(it)) {
                    return it
                }
            }
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
        return "Object: {$prefix + $path}"
    }

    fun get_action(): Action {
        //for (ch in children) {
        //    if (ch.get_action() != action) {
        //        return Action.Mixed
        //    }
        //}

        return action
    }
}
