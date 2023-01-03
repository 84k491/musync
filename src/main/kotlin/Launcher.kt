// TODO rename scanner?
class Launcher(private val indexFile: Index) {
    val source = Source(indexFile.file.toPath().toAbsolutePath().parent.relativize(indexFile.cwd.toAbsolutePath()))
    val destinations = indexFile.destinationPaths.map { Destination(it) }
    val undefined = source.updatePermissionsGetUndef(indexFile.permissions)

    fun checkForSyncAndGetError(): String? {
        if (undefined.isNotEmpty()) {
            return "There are ${undefined.size} files with undefined permissions"
        }

        return null
    }

}