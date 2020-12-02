
### WIP

### next

* complete file permissions

### bug

* special items missing in custom list
* refresh when changing backup dir (or permissions etc.)

### check

* special.user not recursive?
* shell: a && b => su a && su b ?
* selinux attribute?

### mystery

* closed app without exception
    * how is it possible?
    * why is this dependent on old app data (after uninstall+install)?
    * clear storage?

### improve

* base features
    * tar command instead of TarUtils? store archive in cache instead of all the files (probably faster)
    * oabx restore own settings
    * encrypt/decrypt single/all backups
    * lsof STOP/CONT
    * shell commands via scripts?
    * file watchers
    * special packages paths and size display

* interaction
    * launcher shortcut
    * tasker integration
    * intent interface
    * more operations on apps  
        * run

* organize 
    * separate apk + data count
    * lock app backup (prevent deleting, exclude from count?)

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

* add modification time to tar

