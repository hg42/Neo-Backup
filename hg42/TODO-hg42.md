
### WIP

### next

### bug

* special items missing in custom list

### check

* prove, if installing an apk changes the ignored directories to the new app user
* which directories are like lib-main (which seems to be handled cache)

### mystery

### improve

* base features
    # oabx should restore own settings
    * encrypt/decrypt single/all backups (per buttons)
    ? file watchers instead of scanning all the time
    * special packages paths and size display
    * shell command onStart, onStop (?) e.g. for rclone mount (how about service?)
    * shell command before and after complete backup/restore
    * check the basic shell commands at start and complain if they do not work (check toybox version etc.)
    * check shell features and choose corresponding code
    ? if busybox available use it and it's features [not feature complete for us]

    * genericRestorePermissions
      * cache and also lib-main and similar
        * have a group u0_aXXX_cache which is XXX+20000
        * u0_aXXX is XXX+10000
        * it would be possible to extract the XXX from the directory
        * only change the XXX for all files while keeping the offset
        * exclude uids <10000
        * though this would need to scan all files, to be faster it could be done in a script

* interaction
    * launcher shortcut
    * more intents / remote control
     
* organize
    ? separate apk + data counts
    * lock app backup (prevent deleting, exclude from count?)
    * apk pool, no duplicates, simulate hardlinks
        add incremental:
        * backup: add the relative path of the apk to the properties file without using it
        * restore: use the entry to get the apk on restore
        * restore: use a default if the entry is missing
        final step:
        * use a different path (apk-pool) + add the cleanup procedure
        apks could be deleted after each refresh:
            * read the pool contents to a list
            * scan all properties and remove every mentioned apk from the list
            * delete the remaining apks in the list
        It's basically like garbage collection.
        no additional io:
        * the file lists are already read in
        * for this it doesn't matter in which directory the apks are found
        * the number of files is reduced (removing duplicated apks)
        * the properties files are also read in
        just storing a reference into the apk pool and a kind of garbage collector
        (one sweep on the properties in RAM).
        Theroretically, it would also be possible to add the number of references to each apk
        (weird way: store it in file name,
         more traditional: in a single properties file for the apk pool).
        This way you don't even need to read the pool files,
        only delete the apk file if the reference becomes zero.
        The apk name should contain the version, so it does not interfere.

* ui
    * distance of checkboxes in backup list

    * apk/data status

        we may arrive at a matrix:
        [ ] [ ] [ ] [ ] Exists in System
        [ ] [ ] [ ] [ ] Should be backed up
        [ ] [ ] [ ] [ ] Count of backups
        or some intelligent way to put this all in one...

        may be like this:
        the tag we have now could have these states:
        - not existing in system (blank?)
        - disabled (crossed, touching the icon toggles this)
        - no backup yet (gray or dark icon color or a warning color?)
        - backup ok (normal color)
        no backup could also be symbolized by a circle around the icon (warning, mnemonic an >O<pen task)

        * "backup of x is not necessary but it is done".

### SOLVED

* tasker integration
* intent interface
