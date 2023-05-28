// TODO show more info when its not enough space in destinations. How much left to copy and how much space available now. How much is dispatched already

// ^^^^^^^^^^top prio
// vvvvvvvvvvlow prio

// TODO make it possible for algo to change destination if there is no space in one
// TODO space command must be able to check show much space available at destinations per one and total

// TODO add "Synced" and "NotSynced" state to know how much size to sync

// TODO save more data in index file because scanning all the files every time takes too long.
// TODO Save destination data (total space, available space) to be able to dispatch without destinations connected
// TODO Destinations can be not connected

fun main(args: Array<String>) {
    val (app, initErr) = ApplicationFactory(args).buildApp()
    val appErr = app?.work()
    initErr?.let { println("Init error: ${it.message}") }
    appErr?.let { println("Work error: ${it.message}") }
}
