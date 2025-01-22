curl "https://gl.wikipedia.org/w/api.php?action=query&prop=extracts&exlimit=1&titles=Galicia&explaintext=1&formatversion=2&format=json" |
  jq -r ".query.pages[0].extract" |
  sed -e 's/^=\+ \(.*\) =\+$/\1/g' |
  sed -e '/^\t\+$/d' |
  cat -s >INPUT_RAW.txt
