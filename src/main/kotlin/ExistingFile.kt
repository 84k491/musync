import java.io.File
import java.nio.file.Path
import kotlin.io.path.relativeTo

// TODO make constructor private
open class ExistingFile(absolutePrefix: Path, path: Path, index: Index?)
    : GhostFile(absolutePrefix, path, index) {

    companion object {
        fun build(cliPath: Path, index: Index): ExistingFile? {
            val absPath = cliPath.toAbsolutePath().normalize()
            return if (absPath.toFile().exists()) {
                ExistingFile(
                    index.getSourceAbsolutePath(),
                    absPath.relativeTo(index.getSourceAbsolutePath()),
                    index
                )
            }
            else null
        }

        fun build(ghost: GhostFile): ExistingFile? {
            return if (ghost.absolutePath().toFile().exists()) {
                ExistingFile(ghost.absolutePrefix, ghost.path, ghost.index)
            }
            else {
                null
            }
        }
    }

    init {
        if (!absolutePath().toFile().exists()) {
            println("PANIC! Existing file ${absolutePath()} does not exist!")
        }
    }

    val file: File by lazy { absolutePath().toFile() }
    private val isDirectory by lazy { file.isDirectory }

    override val children: List<ExistingFile> by lazy {
        absolutePath().toFile().listFiles()
            ?.map { ExistingFile(absolutePrefix, it.toPath().relativeTo(absolutePrefix), index) } ?: listOf()
    }

    open fun size(): FileSize {
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
        val type = if (isDirectory) "dir" else "file"
        return "ExistingFile: {${absolutePath()}; $type; size = ${size()}}"
    }
}