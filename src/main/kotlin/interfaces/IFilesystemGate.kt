package interfaces

import GhostFile

interface IFilesystemGate {
    fun build(ghost: GhostFile): IExistingFile?
    fun remove(file: IExistingFile): Boolean
    fun copy(from: IExistingFile, where: GhostFile): Boolean
}