#!/bin/zsh

#set -x

variant=${1:-pumpkin}

Variant=${(C)variant}

startdir=$(pwd)
basedir="$(dirname $startdir)/$(basename $startdir)--build-clean/$variant"
builddir="$basedir/build"
outdir="$builddir/outputs/apk/$variant"
savedir="/z/src/android/Neo-Backup--etc/apks/"
pushdir="/sdcard/Download/NB-apk/"
marker="$basedir/.marker"

gitstatus=$(git status --porcelain)
echo $gitstatus

if [[ $(echo $gitstatus | wc -l) != 0 ]]; then
  print
  print "git status is not clean"
  print
  #exit 1
  timeout=4
  print -n "waiting $timeout sec..."
  repeat $timeout; do
    print -n "."
    sleep 1
  done
  print "go..."
  sleep 1
fi

#set -x

head=$(git rev-parse HEAD)

mkdir -p $basedir

if [[ ! -e $marker || $(cat $marker) != $head ]]; then

  cd $basedir
  if [[ $(pwd) == $basedir ]]; then

    rm --recursive -v $basedir/*(N) $basedir/.*(N)

    git clone $startdir . &&
        echo $head >$marker

    ./gradlew --no-daemon --no-build-cache --no-configuration-cache :assemble$Variant ||
        exit 1

  fi
fi

for apk in $outdir/*.apk(N); do
  cp -v $apk $savedir
done

adb devices | while read serial what; do
  if [[ $what == device ]]; then
    echo
    echo "--- $variant to $serial"
    echo
    for apk in $outdir/*.apk(N); do
      adb -s $serial install        $apk
      adb -s $serial shell mkdir -p       $pushdir
      adb -s $serial push           $apk  $pushdir
    done
  fi
done
