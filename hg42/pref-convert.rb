#!/bin/ruby

require 'find'

#puts "test"

constants_file = "../app/src/main/java/com/machiav3lli/backup/Constants.kt"

constant = {}

File.open(constants_file).each do |line|

  line.chomp!

  #puts line

  if line.match(%r((PREFS_\w+)\s+=\s+"(\w+)")) then
    constant[$1] = $2
  end

end

#puts constant

Find.find(File.realpath("../app/src/main/java/com/machiav3lli/backup")) do |path|
  name = File.basename(path)
  if FileTest.directory?(path)
    if name[0] == ?.
      Find.prune       # Don't look any further into this directory.
    else
      next
    end
  else
    if name.end_with?(".kt") then
      text = File.open(path).read()
      text0 = text.clone
      #puts text.split(%r(\n))[0..3]
      constant.keys.reverse.each do |name|
        value = constant[name]
        text.gsub!(%r((import\s*.*)\.#{name}.*?), "\\1.preferences.pref_#{value}")
        text.gsub!(%r(OABX.pref\w+\(#{name},.+?\)), "pref_#{value}.value")
        #text.gsub!(%r(OABX.setPref\w+\(#{name},\s*(.+?)\s*\)), "pref_#{value}.value = \\1")
        text.gsub!(%r(pref\w+\(#{name},.+?\)), "pref_#{value}.value")
        text.gsub!(%r(val\s+((?i)#{value})Pref\s*=\s*), "val pref_#{value} = ")
        text.gsub!(%r("\s*\+\s*#{name}), "#{value}\"")
        text.gsub!(name, "PREF_#{value}")
      end
      if text != text0 then
        puts "-"*100
        puts path
        puts "-"*100
        puts text.lines.grep(%r(pref_|PREF_|PREFS_|key|BooleanPref))
        File.open(path, "w").write(text)
      end
    end
  end
end
