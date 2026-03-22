import json
d=json.load(open('dests.json'))
for x in d:
  print(f'ID={x["id"]}, Name={x["nom"]}, Thumb={x.get("thumbnailUrl", "")}, Medias={[m["url"] for m in x.get("medias", [])]}')
