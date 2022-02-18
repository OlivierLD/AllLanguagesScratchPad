#!/usr/bin/env python3
#
# This is for python3
# Use it like in:
#   python3 KMLtoGPX.py ./path/to/data.kml
#
from typing import Any
import math
from xml.dom import minidom
# from io import TextIOWrapper
from typing import TextIO
import sys
from typing import List

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


# Main part
def main(args: List[str]) -> None:
    # print(f"{len(args)} arguments:")
    # for arg in args:
    #     print(f"\t{arg}")
    if len(args) < 2:
        print("Provide the kml file path as first parameter.")
        return
    kml_file_name: str = args[1]
    try:
        dot_index: int = kml_file_name.index('.kml')
        # print(f"Extension found index {dot_index}")
    except ValueError:
        print(f"Extension '.kml' not found in file name {kml_file_name}")
        return

    output_file_name: str = kml_file_name[:dot_index] + '.gpx'
    print(f"Processing {kml_file_name} into {output_file_name} ...")

    # try ../../../../kml-sample-pacific.kml
    try:
        xml_doc: Any = minidom.parse(kml_file_name)
        document_list: Any = xml_doc.getElementsByTagName('Document')
        print(f"Nb Document(s): {len(document_list)}")
        if len(document_list) > 0:
            # Create output file
            gpx: TextIO = open(output_file_name, "w")
            # gpx: Any = open(output_file_name, "w")
            # print(f"file is a {type(gpx)}")
            # Header
            gpx.write("<?xml version=\"1.0\"?>\n" +
                      "<gpx version=\"1.1\" creator=\"OpenCPN\" \n" +
                      "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                      "     xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
                      "     xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" \n" +
                      "     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" \n" +
                      "     xmlns:opencpn=\"http://www.opencpn.org\">\n" +
                      "  <trk>\n" +
                      "    <extensions>\n" +
                      "      <opencpn:guid>21180000-44ac-4218-a090-ed331f980000</opencpn:guid>\n" +
                      "      <opencpn:viz>1</opencpn:viz>\n" +
                      "    </extensions>\n" +
                      "    <trkseg>\n")
            #
            description = document_list[0].getElementsByTagName('description')
            first_child = description[0].firstChild
            print(f"Doc description - 2: [{first_child.wholeText.strip()}]")
            folder_node = document_list[0].getElementsByTagName('Folder')
            placemarks = folder_node[0].getElementsByTagName('Placemark')
            print(f"There are {len(placemarks)} Placemark(s)")
            for pm in placemarks:
                timestamps = pm.getElementsByTagName('when')
                timestamp: str = timestamps[0].firstChild.data
                pt_coordinates = pm.getElementsByTagName('coordinates')
                str_coord: str = pt_coordinates[0].firstChild.data
                coord_array = str_coord.split(",")
                if len(coord_array) == 2:
                    lng: float = float(coord_array[0])
                    lat: float = float(coord_array[1])
                    # print(f"{timestamp}: {dec_to_hex(lat, NS)} / {dec_to_hex(lng, EW)}")
                    gpx.write(
                        f"      <trkpt lat=\"{lat}\" lon=\"{lng}\">\n        <time>{timestamp}</time>\n      </trkpt>\n")
                else:
                    print(f"What kind of coord is that? [{str_coord}]")
            print("Done")
            gpx.write("    </trkseg>\n  </trk>\n</gpx>")
            gpx.close()
        else:
            print("What kind of file is that? Is it really a KML doc?")
    except FileNotFoundError:
        print(f"File {kml_file_name} not found...")
        return
    print("Bye")


if __name__ == '__main__':
    main(sys.argv)
