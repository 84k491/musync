class HelpApplication: Application() {
    override fun work(): Error? {
        println("Musync is a tool for synchronizing music files from a single storage to several destinations.\n" +
                "Files must be added to index file first\n" +
                "\n" +
                "Usage:\n" +
                "musync init: initialize (current) directory with empty index file\n" +
                "\n" +
                "musync scan: <todo describe>\n" +
                "\n" +
                "musync file [flag] <file(s)>: change policy for files\n" +
                "            |\n" +
                "            |-> add : Mark file as wanted to be present at destinations\n" +
                "            |-> remove : Mark file as unwanted at destinations\n" +
                "\n" +
                "musync list [optional flag] : list files of the specified type\n" +
                "            |\n" +
                "            |-> new : (Default). Files that are not added to index\n" +
                "            |-> added : Files that will be copied to destinations\n" +
                "            |-> removed : Files that mustn't present in destinations\n" +
                "\n" +
                "musync space [<optional flag] : get overall size of files with specified type\n" +
                "             |\n" +
                "             |-> new : (Default). Size of not indexed files\n" +
                "             |-> added : Size of files that will be copied\n" +
                "             |-> removed : Size of files that are marked as unwanted\n" +
                "\n" +
                "musync destination [flag] <path(s)>: Add or remove destination\n" +
                "                   |\n" +
                "                   |-> add : Remember a path to be a destination\n" +
                "                   |-> remove : Forget a destination\n" +
                "\n" +
                "musync sync [optional flag] : perform the synchronization (ask to copy)\n" +
                "            |\n" +
                "            |-> --ask : (Default). Shows plans and ask before further actions\n" +
                "            |-> --dry : Dry run. Shows plans and exits\n" +
                "            |-> --force : Shows plans and proceeds with actions\n" +
                "")
        return null
    }
}

