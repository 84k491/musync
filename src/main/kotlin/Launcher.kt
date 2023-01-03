// TODO rename scanner? // TODO use companion object instead?
class Launcher(private val indexFile: Index) {
    val source = Source(indexFile.file.toPath().toAbsolutePath().parent)
    val destinations = indexFile.destinationPaths.map { Destination(it) }
    private val undefined = source.updatePermissionsGetUndef(indexFile.permissions)

    fun checkForSyncAndGetError(): String? {
        if (undefined.isNotEmpty()) {
            return "There are ${undefined.size} files with undefined permissions"
        }

        return null
    }
}