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

    open fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }

    fun setActionRecursivelyDown(v: Action) {
        if (children.isNotEmpty()) {
            children.forEach{ it.setActionRecursivelyDown(v) }
        }
        state.setAction(v)
    }

    fun findByPath(argPath: Path): GhostFile? {
        return all().find { it.path == argPath }
    }

    open fun all(): Sequence<GhostFile> {
        return sequenceOf(this) + children.asSequence().flatMap { it.all() }
    }

    private fun getParent(): GhostFile? {
        if (path.nameCount <= 1) {
            return null
        }
        return GhostFile(absolutePrefix, path.parent, index)
    }

    fun updateParentsAction() {
        val parentFile = getParent() ?: return

        if (parentFile.updateActionByChildren()) {
            parentFile.updateParentsAction()
        }
    }

    private fun updateActionByChildren(): Boolean {
        val initAction = children.first().state.getAction()

        val newAction = children.fold(initAction) { acc: Action, file: GhostFile ->
            if (acc == file.state.getAction()) acc else Action.Mixed
        }
        val res = newAction != state.getAction()
        state.setAction(newAction)
        return res
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
