#!/bin/zsh

start=01:00
step=1
debug=
variant=hg42.debug
#variant=hg42

schedules=(
  special
  user
  system
  )

if [[ $1 == -d ]]; then
  debug=echo
  shift
  set -x
fi

if [[ -n $1 ]]; then
  start=$1
  shift
fi

if [[ -n $1 ]]; then
  step=$1
  shift
fi

if [[ -n $1 ]]; then
  variant=$1
  shift
fi

if [[ -n $* ]]; then
  schedules=($*)
fi

step=$(($step*60))
if [[ $start != *:* ]]; then
  start=$( date +%H:%M -d @$(( $(date +%s) + $(($start*60)) )) )
fi

time=$start

for schedule in $schedules; do
  $debug adbsu am broadcast -a reschedule -e name $schedule -e time $time -n com.machiav3lli.backup.$variant/com.machiav3lli.backup.services.CommandReceiver
  time=$( date +%H:%M -d @$(( $(date +%s -d $time) + $step )) )
done