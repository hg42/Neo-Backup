#!/bin/zsh

#set -x

variant=${1:-pumpkin}

Variant=${(C)variant}

startdir=$(pwd)
echo "startdir: $startdir"

basedir="$(dirname $startdir)/$(basename $startdir)--build-clean/$variant"
builddir="$basedir/build"
outdir="$builddir/outputs/apk/$variant"
savedir="/z/src/android/Neo-Backup--etc/apks/"
pushdir="/sdcard/Download/NB-apk/"
marker="$basedir/.marker"

gitdirty=0
git status --porcelain $startdir | while read line; do
  echo $line
  gitdirty=$((gitdirty+1))
done

if [[ $gitdirty != 0 ]]; then
  print
  print "git status is not clean"
  print
  #exit 1
  timeout=10
  print -n "waiting..."
  i=$timeout
  repeat $timeout; do
    print -n "$i."
    sleep 1
    i=$((i-1))
  done
  print "go..."
  sleep 1
else
  echo "git is clean"
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
