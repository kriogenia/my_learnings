curl "https://www.gutenberg.org/cache/epub/5500/pg5500.txt" |
  awk '/\*\*\* START.* \*\*\*/,/\*\*\* END.* \*\*\*/' |
  sed '1,1d' |
  sed '$d' >INPUT_RAW.txt
