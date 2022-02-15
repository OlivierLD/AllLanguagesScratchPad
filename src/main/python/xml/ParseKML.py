#
# This is for python3
#
from typing import Any
import math
from xml.dom import minidom

NS = "NS"
EW = "EW"


def dec_to_hex(x: float, conv_type: str) -> str:
    name: str
    if x < 0:
        if conv_type == "NS":
            name = "S"
        else:
            name = "W"
    else:
        if conv_type == "NS":
            name = "N"
        else:
            name = "E"
    abs_degrees: float = abs(x)
    val_deg: int = math.floor(abs_degrees)
    val_min: int = math.floor(60 * (abs_degrees - val_deg))
    val_sec: int = round(3600 * (abs_degrees - val_deg - val_min / 60))
    if val_sec == 60:
        val_sec = 0
        val_min += 1
    if val_min == 60:
        val_min = 0
        val_deg += 1
    return f"{name}  {val_deg:02d}\xb0 {val_min:02d}' {val_sec:02d}\""


if __name__ == '__main__':
    xml_doc: Any = minidom.parse('../../../../kml-sample-pacific.kml')
    documentList: Any = xml_doc.getElementsByTagName('Document')
    print(f"Nb Document(s): {len(documentList)}")
    if len(documentList) > 0:
        description = documentList[0].getElementsByTagName('description')
        print(f"description is a {type(description)}")
        print(f"description[0] is a {type(description[0])}")
        firstChild = description[0].firstChild
        print(f"firstChild is a {type(firstChild)}")
        print(f"firstChild.data is a {type(firstChild.data)}")
        print(f"firstChild.nodeValue is a {type(firstChild.nodeValue)}")
        print(f"Doc description - 1: [{firstChild.data.strip()}]")
        print(f"Doc description - 2: [{firstChild.wholeText.strip()}]")
        folderNode = documentList[0].getElementsByTagName('Folder')
        print(f"Folder is a {type(folderNode)}")
        placemarks = folderNode[0].getElementsByTagName('Placemark')
        print(f"There are {len(placemarks)} Placemark(s)")
        for pm in placemarks:
            timestamps = pm.getElementsByTagName('when')
            timestamp: str = timestamps[0].firstChild.data
            ptCoordinates = pm.getElementsByTagName('coordinates')
            # print(f"Coord: {type(ptCoordinates)}")
            # print(f"coordinates: {ptCoordinates[0].firstChild.data}")
            strCoord: str = ptCoordinates[0].firstChild.data
            coordArray = strCoord.split(",")
            if len(coordArray) == 2:
                lng: float = float(coordArray[0])
                lat: float = float(coordArray[1])
                print(f"{timestamp}: {dec_to_hex(lat, NS)} / {dec_to_hex(lng, EW)}")
            else:
                print(f"What kind of coord is that? [{strCoord}]")
        print("Done")
    else:
        print("What kind of file is that? Is it really a KML doc?")
    print("Bye")
