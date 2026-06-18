#
# From https://www.w3schools.com/python/ref_module_zipfile.asp
# Read a zip
#
import zipfile
import os

z: zipfile.ZipFile = zipfile.ZipFile('example.zip')
print(f"The zip is a {type(z)}")

try:
    for filename in z.namelist():
        print(f"...Managing {filename}")
        if not os.path.isdir(filename):
            # read the file
            print(f"-- Content of {filename} --")
            # TODO Find how to get the full content ?
            for line in z.open(filename):
                print(line.decode('utf-8'))
        else:
            print(f'Directory "{filename}"')
    print("------")
    for line in z.open('index.html'):
        print(line.decode('utf-8'))

    z.close()                # Close the file after opening it
except zipfile.BadZipFile:
    print(f'Bad zip file: "{z}"')
except IsADirectoryError:
    print(f'Directory, not file: "{z}"')
except FileNotFoundError:
    print(f'File not found: "{z}"')
# del z
