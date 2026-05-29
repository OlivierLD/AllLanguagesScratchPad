#
# From https://www.w3schools.com/python/ref_module_zipfile.asp
# Write a zip
#
import zipfile

with zipfile.ZipFile('example.zip', 'w') as myzip:
  myzip.writestr('hello.txt', 'Hello, Machpro!')
  myzip.writestr('data.txt', 'Some data here')
  htmlContent: str = '<html>'
  htmlContent += '<head>'
  htmlContent += '<title>An Index!</title>'
  htmlContent += '</head>'
  htmlContent += '<body>'
  htmlContent += '<h1>Hi There!</h1>'
  htmlContent += '</body>'
  htmlContent += '</html>'
  myzip.writestr('index.html', htmlContent)
  myzip.writestr('subdir/index.2.html', htmlContent)

print('ZIP file created successfully')
