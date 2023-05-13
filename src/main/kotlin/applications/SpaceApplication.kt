import interfaces.IFilesystemGate

class SpaceApplication(i: Index, private val filter: String?, private val fileBuilder: IFilesystemGate): IndexedApplication(i) {
    private fun decodeFilter(v: String): Action? {
        return when (v) {
            "new" -> Action.Undefined
            "added" -> Action.Include
            "removed" -> Action.Exclude
            else -> null
        }
    }

    override fun work(): Error? {
        val action = decodeFilter(filter?:"new") ?: return Error("Unknown action <${filter}>")
        val totalSize =
            index.getSource().all()
                .filter { it.state.getAction() == action }
                .mapNotNull { it: GhostFile -> fileBuilder.build(it) }
                .fold(FileSize(0)) { acc: FileSize, file ->
                    acc + file.size().onDisk()
        }
        println(totalSize)
        return null
    }
}
