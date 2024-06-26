#!/bin/zsh

variant=$1

Variant=${(C)variant}
outdir="./app/build/outputs/apk/$variant"
savedir="/z/src/android/Neo-Backup--etc/apks/"
pushdir="/sdcard/Download/NB-apk/"

rm -v $outdir/*

./gradlew --no-daemon --no-build-cache --no-configuration-cache :app:assemble$Variant || exit 1

for apk in $outdir/*.apk(N); do
  cp -v $apk $savedir
done

adb devices | while read serial what; do
  if [[ $what == device ]]; then
    echo
    echo "--- $Variant to $serial"
    echo
    for apk in $outdir/*.apk(N); do
      adb -s $serial install        $apk
      adb -s $serial shell mkdir -p       $pushdir
      adb -s $serial push           $apk  $pushdir
    done
  fi
done
