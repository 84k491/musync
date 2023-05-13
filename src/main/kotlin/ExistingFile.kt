import interfaces.IExistingFile
import interfaces.IIndex
import java.io.File
import java.nio.file.Path
import kotlin.io.path.relativeTo

// TODO make constructor private
class ExistingFile(absolutePrefix: Path, path: Path, index: IIndex?)
    : IExistingFile(absolutePrefix, path, index) {

    override fun usableSpace(): FileSize {
        return FileSize(absolutePath().toFile().usableSpace)
    }

    val file: File by lazy { absolutePath().toFile() }
    override fun isDirectory(): Boolean {
        return file.isDirectory
    }

    override val children: List<ExistingFile> by lazy {
        absolutePath().toFile().listFiles()
            ?.map { ExistingFile(absolutePrefix, it.toPath().relativeTo(absolutePrefix), index) } ?: listOf()
    }

    override fun size(): FileSize {
        // TODO directories can use 0 space on disk
        val size = absolutePath().toFile().length()
        return FileSize(size)
    }

    override fun all(): Sequence<ExistingFile> {
        return sequenceOf(this) +
                children
                    .asSequence()
                    .flatMap { it.all() }
    }

    override fun toString(): String {
        val type = if (isDirectory()) "dir" else "file"
        return "ExistingFile: {${absolutePath()}; $type; size = ${size()}}"
    }
}