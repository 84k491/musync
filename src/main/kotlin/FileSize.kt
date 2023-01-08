const val allocationUnitSize = 4096

open class FileSize(private val value: Long) {
    fun bytes(): Long {
        return value
    }

    override fun toString(): String {
        val sizeGb = value.toDouble() / (1024 * 1024 * 1024)
        if (sizeGb > 1) {
            return "$sizeGb Gb"
        }
        val sizeMb = value.toDouble() / (1024 * 1024)
        if (sizeMb > 1) {
            return "$sizeMb Mb"
        }
        val sizeKb = value.toDouble() / 1024
        if (sizeKb > 1) {
            return "$sizeKb Kb"
        }
        return "$value b"
    }

    fun onDisk(): FileSize {
        return FileSize(value + (allocationUnitSize - value % allocationUnitSize))
    }

    operator fun plus(other: FileSize): FileSize {
        return FileSize(value + other.value)
    }

    operator fun minus(other: FileSize): FileSize {
        return FileSize(value - other.value)
    }
}