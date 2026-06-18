#
# From https://www.w3schools.com/python/ref_module_zipfile.asp
# Read a zip
#
import zipfile
import os
import sys
from typing import List

BINARY = "application/octet-stream"
TEXT_PLAIN = "text/plain"
TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8"
TEXT_PLAIN_ISO_8859 = "text/plain; charset=ISO-8859-1"
TEXT_HTML = "text/html"
TEXT_XML = "text/xml"
APPLICATION_JSON = "application/json"
TEXT_JAVASCRIPT = "text/javascript"
TEXT_CSS = "text/css"
IMAGE_X_ICON = "image/x-icon"
IMAGE_PNG = "image/png"
IMAGE_GIF = "image/gif"
IMAGE_JPEG = "image/jpeg"
IMAGE_SVG_XML = "image/svg+xml"
APPLICATION_X_FONT_WOFF = "application/x-font-woff"
AUDIO_WAV = "audio/wav"
APPLICATION_PDF = "application/pdf"
APPLICATION_X_FONT_TTF = "application/x-font-ttf"
# ... and more to come!


def get_file_extension_os(url: str) -> str:
    _, file_extension = os.path.splitext(url)
    return file_extension

def get_mime_type(filename: str) -> str:
    extension = get_file_extension_os(filename)
    print(f"Extension is [{extension}]")
    encoding: str = BINARY   # Default
    if extension.upper() == '.HTML':
        encoding = TEXT_HTML
    elif extension.upper() == '.CSS':
        encoding = TEXT_CSS
    elif extension.upper() == '.JS':
        encoding = TEXT_JAVASCRIPT
    elif extension.upper() == '.XML':
        encoding = TEXT_XML
    elif extension.upper() == '.JSON':
        encoding = APPLICATION_JSON
    elif extension.upper() == '.JPG':
        encoding = IMAGE_JPEG
    elif extension.upper() == '.ICO':
        encoding = IMAGE_X_ICON
    elif extension.upper() == '.PNG':
        encoding = IMAGE_PNG
    elif extension.upper() == '.GIF':
        encoding = IMAGE_GIF
    elif extension.upper() == '.SVG':
        encoding = IMAGE_SVG_XML
    elif extension.upper() == '.WOFF':
        encoding = APPLICATION_X_FONT_WOFF
    elif extension.upper() == '.WAV':
        encoding = AUDIO_WAV
    elif extension.upper() == '.PDF':
        encoding = APPLICATION_PDF
    elif extension.upper() == '.TTF':
        encoding = APPLICATION_X_FONT_TTF

    return encoding


def main(args: List[str]) -> None:
    z: zipfile.ZipFile = zipfile.ZipFile('web.zip')
    print(f"The zip is a {type(z)}")

    # The file to extract (change at will)
    # filename: str = "css/bground.jpg"
    filename: str = "web/index.html"

    try:
        print(f"...Managing {filename}")
        if not os.path.isdir(filename):
            encoding: str = get_mime_type(filename)
            # read the file
            # print(f"-- Content of {filename} --")
            file_in_zip: zipfile.ZipExtFile = z.open(filename, "r")
            print(f"The zipped file is a {type(file_in_zip)}")
            content: bytes = file_in_zip.read()
            print(f"File content is a {type(content)}")

            # Dump content, as is.
            ext: str = get_file_extension_os(filename)
            with open("my_file" + ext, "wb") as binary_file:
                binary_file.write(content)

            # for line in file_in_zip:
            #     print(line.decode(encoding))

            file_in_zip.close()  # Close the file after opening it
        else:
            print(f'Directory "{filename}"')
        print("------")

        z.close()
    except zipfile.BadZipFile:
        print(f'Bad zip file: "{z}"')
    except IsADirectoryError:
        print(f'Directory, not file: "{z}"')
    except FileNotFoundError:
        print(f'File not found: "{z}"')
    # del z


if __name__ == '__main__':
    print("Python version {}.{}.{}".format(sys.version_info.major, sys.version_info.minor, sys.version_info.micro))
    main(sys.argv)
