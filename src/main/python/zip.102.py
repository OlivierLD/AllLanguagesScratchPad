#
# From https://www.w3schools.com/python/ref_module_zipfile.asp
# Read a zip
#
import zipfile
import os

z = zipfile.ZipFile('example.zip')

for filename in z.namelist():
    print(f"...Managing {filename}")
    if not os.path.isdir(filename):
        # read the file
        print(f"-- Content of {filename} --")
        for line in z.open(filename):
            print(line.decode('utf-8'))

print("------")
for line in z.open('index.html'):
    print(line.decode('utf-8'))

z.close()                # Close the file after opening it
del z
