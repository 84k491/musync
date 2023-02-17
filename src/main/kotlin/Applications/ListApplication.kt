class ListApplication(i: Index, private val filter: String?): IndexedApplication(i) {
    private fun decodeFilter(v: String): Action? {
        return when (v) {
            "new" -> Action.Undefined
            "added" -> Action.Include
            "removed" -> Action.Exclude
            else -> null
        }
    }

    override fun work(): Error? {
        val action = decodeFilter(filter ?: "new") ?: return Error("Unknown action <${filter}>")
        index.indexedFiles()
            .filter { it.state.getAction() == action }
            .forEach { println(it) }
        return null
    }
}

