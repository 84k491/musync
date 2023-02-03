import java.nio.file.Path

// TODO rename scanner? // TODO use companion object instead?
class Launcher(indexFile: Index) {
    val source = Source(indexFile.sourceFullPath())
    val destinations = indexFile.destinationPaths.map { Destination(Path.of(it)) }
    init {
        source.updatePermissionsGetUndef(indexFile.permissions)
    }
}