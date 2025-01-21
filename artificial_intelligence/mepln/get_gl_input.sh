curl "https://gl.wikipedia.org/w/api.php?action=query&prop=extracts&exlimit=1&titles=Karl_Marx&explaintext=1&formatversion=2&format=json" |
  jq -r ".query.pages[0].extract" |
  sed -e 's/^=\+ \(.*\) =\+$/\1/g' |
  sed -e 's/ (pronunciado: \[.*\])//g' >gl_input.txt
