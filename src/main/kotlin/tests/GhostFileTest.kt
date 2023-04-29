package tests

import FileSyncState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tests.mocks.MockIndex

class GhostFileTest {

    fun getMockIndex() : MockIndex {
        val index = MockIndex()
        index.permissions["album1"] = FileSyncState(Action.Undefined, false)
        index.permissions["album1/song1"] = FileSyncState(Action.Undefined, false)
        index.permissions["album1/song2"] = FileSyncState(Action.Undefined, false)
        return index
    }

    @Test
    fun getChildrenTest() {
        // assertEquals(1,1)
        val index = getMockIndex()
        assertEquals(3, index.indexedFiles().fold(0) {acc, _ -> acc + 1 })
    }
}