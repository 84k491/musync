class HelpApplication: Application() {
    override fun work(): Error? {
        println("Musync is a tool for synchronizing files from a single storage to several destinations.\n" +
                "Files must be added to index file first\n" +
                "\n" +
                "Commands description:\n" +
                "musync init : initialize (current) directory with empty index file. All other commands will use this " +
                "file as a reference and store file data in it. Every command searching for this file in pwd and above, " +
                "so it's ok to invoke command in directories below the index file\n" +
                "\n" +
                "musync scan : updates indexing information of the files in the source and in all destinations " +
                "if they are available. Needs to be done when any file is added or removed in the filesystem. " +
                "Can take some time to iterate over all files\n" +
                "\n" +
                "musync file [subcommand] [flag] <file(s)> : change policy for files\n" +
                "            |            |\n" +
                "            |            |-> --new : substitutes this flag with all files, marked as new\n" +
                "            |\n" +
                "            |-> add : Mark file as wanted to be present at destinations\n" +
                "            |-> remove : Mark file as unwanted at destinations\n" +
                "\n" +
                "musync list [optional subcommand] : list files of the specified type\n" +
                "            |\n" +
                "            |-> new : (Default). Files that are not added to index\n" +
                "            |-> added : Files that will be copied to destinations\n" +
                "            |-> removed : Files that mustn't present in destinations\n" +
                "            |-> all : All indexed files\n" +
                "            |-> unsynced : Not synced yet files. Can be used to check what would be done on next sync\n" +
                "            |-> synced : Files that are synced with destinations\n" +
                "\n" +
                "musync space [optional subcommand] : get overall size of files with specified type\n" +
                "             |\n" +
                "             |-> new : (Default). Size of not indexed files\n" +
                "             |-> added : Size of files that will be copied\n" +
                "             |-> removed : Size of files that are marked as unwanted\n" +
                "\n" +
                "musync destination [subcommand] <path(s)> : Add or remove destination\n" +
                "                   |\n" +
                "                   |-> add : Remember a path to be a destination\n" +
                "                   |-> remove : Forget a destination\n" +
                "\n" +
                "musync sync [optional flag] : perform the synchronization (ask to copy)\n" +
                "            |\n" +
                "            |-> --ask : (Default). Shows plans and ask before further actions\n" +
                "            |-> --dry : Dry run. Shows plans and exits\n" +
                "            |-> --force : Shows plans and proceeds with actions\n" +
                "\n" +
                "--------------------------------------------------\n" +
                "\n" +
                "A possible usage scenario:\n" +
                "$ cd /source_dir ; echo 'changed cwd to the desired one, it will be used as a source'\n" +
                "$ musync init ; echo 'an index file created in cwd'\n" +
                "$ musync destination add /destination_1 ; echo 'added /destinaiton_1 as destination'\n" +
                "$ musync destination add /destination_2\n" +
                "$ musync destination add /destination_2\n" +
                "$ musync scan ; echo 'all files in source and destinations are scanned and their relations are saved'\n" +
                "$ musync list synced | wc -l ; echo 'Some files can be synced without any copying'\n" +
                "$ musync file add --new ; echo 'add all the files, that are not present in destionations'\n" +
                "$ musync file remove /source_dir/some_dir;" +
                "echo 'some_dir and all the containing files won't be copied to destionations'\n" +
                "$ musync sync --force ; echo 'Source is completely synchronized with destinations, " +
                "all the required files are copied'\n" +
                "$ musync list unsynced | wc -l ; echo 'No file should be left unsynced'\n" +
                "")
        return null
    }
}

