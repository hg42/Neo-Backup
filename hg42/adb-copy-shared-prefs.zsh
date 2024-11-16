#!/bin/zsh

base=com.machiav3lli.backup

opts=(
  -d            # usb
)

pkgs=(
  $base
  $base.neo
  $base.debug
  $base.hg42
  $base.hg42.rel
  $base.hg42.debug
)

mkdir -p preferences

for p in $pkgs; do

  #mkdir -p $p/shared_prefs

  echo "cat /data/user/0/$p/shared_prefs/${p}_preferences.xml" | adb $opts shell su > preferences/${p}_preferences.xml

done
