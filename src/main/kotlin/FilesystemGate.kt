import interfaces.IExistingFile
import interfaces.IFilesystemGate
import java.io.IOException

class FilesystemGate : IFilesystemGate {
    override fun build(ghost: GhostFile): IExistingFile? {
            return if (ghost.absolutePath().toFile().exists()) {
                ExistingFile(ghost.absolutePrefix, ghost.path, ghost.index)
            }
            else {
                null
            }
    }

    override fun remove(file: IExistingFile): Boolean {
        if (file !is ExistingFile) {
            println("Panic! File $file was expected to exist")
            return false
        }
        return file.file.delete()
    }
    override fun copy(from: IExistingFile, where: GhostFile): Boolean {
        if (from !is ExistingFile) {
            println("Panic! File $from was expected to exist")
            return false
        }
        return try {
            from.file.copyTo(where.toPotentialFile()).exists()
        } catch (e: Exception) {
            when (e) {
                is FileAlreadyExistsException -> true
                else -> { println("Exception on copying file ${from.path}. Error: $e;"); false }
            }
        }
    }
}