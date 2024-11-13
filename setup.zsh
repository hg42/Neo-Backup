#!/bin/zsh

variant=hg42

hour=00
setup="
  $hour:00  special
  $hour:05  system
  $hour:25  user
"
setup=$(echo $setup | grep -v '^$')

echo $setup
echo $setup | while read time schedule; do
  #echo "---------- $time $schedule"
  command=(adbsu am broadcast -a reschedule -e name $schedule -e time $time -n com.machiav3lli.backup.$variant/com.machiav3lli.backup.services.CommandReceiver)
  echo $command
done | zsh
