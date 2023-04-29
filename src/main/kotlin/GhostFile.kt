import interfaces.IIndex
import java.io.File
import java.nio.file.Path
import kotlin.io.path.relativeTo

open class GhostFile(val absolutePrefix: Path, val path: Path, val index: IIndex?) {

    constructor(cliPath: Path, index: Index)
            : this(
                index.getSourceAbsolutePath(),
                cliPath.toAbsolutePath().relativeTo(index.getSourceAbsolutePath()),
                index)

    val state = index?.permissions()?.getOrPut(path.toString()) { FileSyncState() } ?: FileSyncState()

    protected open val children by lazy {
        index
            ?.let { it.permissions()
            .filter { (pathString, _) -> Path.of(pathString).parent == this.path }
            .map { (pathString, _) -> GhostFile(absolutePrefix, Path.of(pathString), index) }}
            ?: listOf()
    }

    fun toExisting(): ExistingFile? {
        return ExistingFile.build(this)
    }

    fun setActionRecursivelyDown(v: Action) {
        if (children.isNotEmpty()) {
            children.forEach{ it.setActionRecursivelyDown(v) }
        }
        state.setAction(v)
    }

    private fun resetSyncRecursivelyDown() {
        if (children.isNotEmpty()) {
            children.forEach{ it.resetSyncRecursivelyDown() }
        }
        state.synced = false
    }

    fun findByPath(argPath: Path): GhostFile? {
        return all().find { it.path == argPath }
    }

    open fun all(): Sequence<GhostFile> {
        return sequenceOf(this) + children.asSequence().flatMap { it.all() }
    }

    fun updateAggregatedPermissions() {
        val initAction = children.firstOrNull()?.state?.getAction() ?: return

        state.setAction(
            children.fold(initAction) { acc: Action, file: GhostFile ->
                if (acc == file.state.getAction()) acc else Action.Mixed
            })
    }

    fun absolutePath(): Path {
        return absolutePrefix.resolve(path)
    }

    fun toPotentialFile(): File {
        return absolutePath().toFile()
    }

    fun getTopParent(): GhostFile? {
        var currentPath = path
        if (currentPath.nameCount <= 1) {
            return null
        }
        while (currentPath.nameCount > 1) {
            currentPath = currentPath.parent
        }
        return GhostFile(absolutePrefix, currentPath, index)
    }

    override fun toString(): String {
        return "GhostFile: {$absolutePrefix; $path}"
    }
}
