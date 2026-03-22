import json
d=json.load(open('dests.json'))
for x in d:
  print(f'{x.get("nom", "")}: {x.get("thumbnailUrl", "")}')
