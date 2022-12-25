
key = ""
File.open("devpref.txt").each do |line|

  line.chomp!

  if line.match(%r(android:key="(.*?)")) then
    key = $1
  else
    line.gsub!(%r(summary="@string/prefs_.*?"))  { 'summary="@string/prefs_' + key.downcase + '_summary"' }
    line.gsub!(%r(title="@string/prefs_.*?"))  { 'title="@string/prefs_' + key.downcase + '"' }
  end

  puts line

end
