import java.nio.file.Path

class InitApplication(private val cwd: Path): Application() {
    override fun work(): Error? {
        val i = Index.load(cwd)
        if (null != i) {
            return Error("Init failed. There is an index file already: ${i.file.path}")
        }

        val filePath = Index.create() ?: return Error("Failed to create index file in $cwd")
        println("Index file created: $filePath")
        return null
    }
}

