import kotlinx.serialization.Serializable

@Serializable
class FileSyncState(
    private var action: Action = Action.Undefined,
    var synced: Boolean = false,
){
    fun setAction(v: Action) {
        if (v != action) {
            synced = false
        }
        action = v
    }
    fun getAction(): Action {
        return action
    }
}

/*
    "action" stands for what we want it to be. And "synced" marks if current state meets our expectations

    present in source, absent in any dest, we want it to copy to any dest
    {action: Include, synced = false}

    present in source, present in ONE dest, we want to DO NOTHING
    {action: Include, synced = true}

    present in source, absent in any dest, we don't know what to do yet
    {action: Undefined, synced = false}

    invalid action!
    {action: Undefined, synced = true}

    present in source, absent in any dest, we DON'T want it to be at dest
    {action: Exclude, synced = true}

    ABSENT/PRESENT in source, present in one dest, we want it to be removed from dest
    {action: Exclude, synced = false}

    present in source, present in MANY dests, we want to keep just one of them // not implemented
    absent in source, present in any dest, we want it to be copied in source // not implemented
* */


