package interfaces

import FileSize
import GhostFile
import java.io.File
import java.nio.file.Path

abstract class IExistingFile(absolutePrefix: Path, path: Path, index: IIndex?)
    : GhostFile(absolutePrefix, path, index) {

    companion object {
        //fun build(ghost: GhostFile): IExistingFile? {
        //    return if (ghost.absolutePath().toFile().exists()) {
        //        IExistingFile(ghost.absolutePrefix, ghost.path, ghost.index)
        //    }
        //    else {
        //        null
        //    }
        //}
    }

    protected abstract fun isDirectory(): Boolean

    abstract fun usableSpace(): FileSize

    abstract fun size(): FileSize

    override fun toString(): String {
        val type = if (isDirectory()) "dir" else "file"
        return "ExistingFile: {${absolutePath()}; $type; size = ${size()}}"
    }
}