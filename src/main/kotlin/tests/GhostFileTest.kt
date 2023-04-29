package tests

import FileSyncState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import tests.mocks.MockIndex
import java.nio.file.Path

class GhostFileTest {

    private fun getMockIndex() : MockIndex {
        val index = MockIndex()
        index.permissions["artist1"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album11"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album11/song111"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album11/song112"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album11/song113"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album12"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album12/song121"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album12/song122"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album12/song123"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist1/album12/song124"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album21"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album21/song211"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album21/song212"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album21/song213"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album21/song214"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album22"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album22/song221"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album22/song222"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album22/song223"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album22/song224"] = FileSyncState(Action.Undefined, false)
        index.permissions["artist2/album22/song225"] = FileSyncState(Action.Undefined, false)
        return index
    }

    @Test
    fun getChildrenBasicTest() {
        val index = getMockIndex()
        val filesSeq = index.indexedFiles()

        // all entries
        assertEquals(22, filesSeq.fold(0) {acc, _ -> acc + 1 })
        // files
        assertEquals(16, filesSeq.fold(0) {acc, f -> if (!f.hasChildren()) {acc + 1} else acc })
        // directories
        assertEquals(6, filesSeq.fold(0) {acc, f -> if (f.hasChildren()) {acc + 1} else acc })

        val topParent = filesSeq.filter { it.path.toString() == "artist1/album12/song123"  }.first().getTopParent()

        assertTrue(topParent != null)
        assertTrue("artist1" == topParent?.path.toString())
    }

    @Test
    fun setPermissionsTest() {
        val index = getMockIndex()
        val filesSeq = index.indexedFiles()

        val file = filesSeq.filter { it.path.toString() == "artist2/album22/song225" }.first()
        val topParent = file.getTopParent()
        assertTrue(topParent != null)
        if (topParent == null) {
            fail("No top parent")
        }

        file.state.setAction(Action.Include)
        // topParent.updateAggregatedPermissions()
        // assertEquals(topParent.state.getAction(), Action.Mixed)
    }

    @Test
    fun findByPathTest() {
        val index = getMockIndex()
        val filesSeq = index.indexedFiles()

        val file = filesSeq.filter { it.path.toString() == "artist2" }.first()
        val found = file.findByPath(Path.of("artist2/album22/song225"))
        assertTrue(found != null)
        val notFound = file.findByPath(Path.of("artist1/album11/song113"))
        assertTrue(notFound == null)
    }

    @Test
    fun setActionRecursivelyTest() {
        val index = getMockIndex()
        val filesSeq = index.indexedFiles()

        val artistDir = filesSeq.filter { it.path.toString() == "artist1" }.first()
        artistDir.setActionRecursivelyDown(Action.Include)
        val file = filesSeq.filter { it.path.toString() == "artist1/album11/song112" }.first()
        assertEquals(Action.Include, file.state.getAction())
        val includedCount = filesSeq.fold(0) {acc, f -> acc + if (f.state.getAction() == Action.Include) 1 else 0 }
        assertEquals(10, includedCount)
    }
}