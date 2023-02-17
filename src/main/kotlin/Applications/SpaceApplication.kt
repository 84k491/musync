class SpaceApplication(i: Index, private val filter: String?): IndexedApplication(i) {
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
                .mapNotNull { it: GhostFile -> it.toExisting() }
                .fold(FileSize(0)) { acc: FileSize, file: ExistingFile ->
                    acc + file.size().onDisk()
        }
        println(totalSize)
        return null
    }
}
