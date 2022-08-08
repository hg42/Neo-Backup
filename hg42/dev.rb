
File.open("dev.txt").each do |line|

  line.chomp!

  (t,s) = line.split(" ", 2)

  l = t.downcase

  #print "#{t} - #{l} = #{s}\n"

  print "
    <string name=\"prefs_#{l}\" translatable=\"false\">#{t}</string>
    <string name=\"prefs_#{l}_summary\" translatable=\"false\">#{s}</string>"
end
