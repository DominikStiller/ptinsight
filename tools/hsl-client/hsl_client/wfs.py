from owslib.wfs import WebFeatureService

wfs = WebFeatureService(url='https://kartta.hel.fi/ws/geoserver/avoindata/wfs', version='2.0.0')
# print([operation.name for operation in wfs.operations])

# print(list(wfs.contents))
res=wfs.getfeature('avoindata:rakennusala').read()
print(res.decode())
