Musync is a tool for synchronizing files from a single storage to several destinations.
Files must be added to index file first

Commands description:
musync init : initialize (current) directory with empty index file. All other commands will use this file as a reference and store file data in it. Every command searching for this file in pwd and above, so it's ok to invoke command in directories below the index file

musync scan : updates indexing information of the files in the source and in all destinations if they are available. Needs to be done when any file is added or removed in the filesystem. Can take some time to iterate over all files

musync file [subcommand] [flag] <file(s)> : change policy for files
            |            |
            |            |-> --new : substitutes this flag with all files, marked as new
            |
            |-> add : Mark file as wanted to be present at destinations
            |-> remove : Mark file as unwanted at destinations

musync list [optional subcommand] : list files of the specified type
            |
            |-> new : (Default). Files that are not added to index
            |-> added : Files that will be copied to destinations
            |-> removed : Files that mustn't present in destinations
            |-> all : All indexed files
            |-> unsynced : Not synced yet files. Can be used to check what would be done on next sync
            |-> synced : Files that are synced with destinations

musync space [optional subcommand] : get overall size of files with specified type
             |
             |-> new : (Default). Size of not indexed files
             |-> added : Size of files that will be copied
             |-> removed : Size of files that are marked as unwanted

musync destination [subcommand] <path(s)> : Add or remove destination
                   |
                   |-> add : Remember a path to be a destination
                   |-> remove : Forget a destination

musync sync [optional flag] : perform the synchronization (ask to copy)
            |
            |-> --ask : (Default). Shows plans and ask before further actions
            |-> --dry : Dry run. Shows plans and exits
            |-> --force : Shows plans and proceeds with actions

--------------------------------------------------

A possible usage scenario:
$ cd /source_dir ; echo 'changed cwd to the desired one, it will be used as a source'
$ musync init ; echo 'an index file created in cwd'
$ musync destination add /destination_1 ; echo 'added /destinaiton_1 as destination'
$ musync destination add /destination_2
$ musync destination add /destination_2
$ musync scan ; echo 'all files in source and destinations are scanned and their relations are saved'
$ musync list synced | wc -l ; echo 'Some files can be synced without any copying'
$ musync file add --new ; echo 'add all the files, that are not present in destionations'
$ musync file remove /source_dir/some_dir;echo 'some_dir and all the containing files won't be copied to destionations'
$ musync sync --force ; echo 'Source is completely synchronized with destinations, all the required files are copied'
$ musync list unsynced | wc -l ; echo 'No file should be left unsynced'

