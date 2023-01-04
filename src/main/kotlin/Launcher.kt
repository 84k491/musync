import java.nio.file.Path

// TODO rename scanner? // TODO use companion object instead?
class Launcher(private val indexFile: Index) {
    val source = Source(indexFile.file.toPath().toAbsolutePath().parent)
    val destinations = indexFile.destinationPaths.map { Destination(Path.of(it)) }
    val undefined = source.updatePermissionsGetUndef(indexFile.permissions)
}