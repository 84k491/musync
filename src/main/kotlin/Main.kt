/*
permission index file to be (de)serialized to map<file, permission>.
index file is searched recursively above cwd
fail if no index file found. Suggest to init some directory
all paths are relative to index file
index file is only for files, permissions for directories will be calculated live based on children files.
recursive ls to get all the files as a tree
permission map to update permissions
every command will act like a new update permission map
permissions are: Undefined, Included, Excluded for files and directories. Mixed only for directories.
permissions for directories are used to aggregate status printout
use Gson for serialization
algo: get files in pool with all "new" permissions, read file, deserialize it to map, update permissions in pool
*/

fun main(args: Array<String>) {
    val (app, initErr) = ApplicationFactory(args).buildApp()
    val appErr = app?.work()
    initErr?.let { println("Init error: ${it.message}") }
    appErr?.let { println("Work error: ${it.message}") }
}