#!/usr/bin/env python
#
# This is for python 2 (legacy)
# Use it like in:
#   python KMLtoGPX.p2.py ./path/to/data.kml
#
import math
from xml.dom import minidom
import sys

NS = "NS"
EW = "EW"


def dec_to_hex(x, conv_type):
    name
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
    abs_degrees = abs(x)
    val_deg = math.floor(abs_degrees)
    val_min = math.floor(60 * (abs_degrees - val_deg))
    val_sec = round(3600 * (abs_degrees - val_deg - val_min / 60))
    if val_sec == 60:
        val_sec = 0
        val_min += 1
    if val_min == 60:
        val_min = 0
        val_deg += 1
    return "{}  {:02d}\xb0 {:02d}' {:02d}\"".format(name, val_deg, val_min, val_sec)


# Main part
def main(args):
    if len(args) < 2:
        print("Provide the kml file path as first parameter.")
        return
    kml_file_name = args[1]
    try:
        dot_index = kml_file_name.index('.kml')
    except ValueError:
        print("Extension '.kml' not found in file name {}".format(kml_file_name))
        return

    output_file_name = kml_file_name[:dot_index] + '.gpx'
    print("Processing {} into {} ...".format(kml_file_name, output_file_name))

    # try ../../../../kml-sample-pacific.kml
    try:
        xml_doc = minidom.parse(kml_file_name)
        document_list = xml_doc.getElementsByTagName('Document')
        print("Nb Document(s): {}".format(len(document_list)))
        if len(document_list) > 0:
            # Create output file
            gpx = open(output_file_name, "w")
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
            print("Doc description - 2: [{}]".format(first_child.wholeText.strip()))
            folder_node = document_list[0].getElementsByTagName('Folder')
            placemarks = folder_node[0].getElementsByTagName('Placemark')
            print("There are {} Placemark(s)".format(len(placemarks)))
            for pm in placemarks:
                timestamps = pm.getElementsByTagName('when')
                timestamp = timestamps[0].firstChild.data
                pt_coordinates = pm.getElementsByTagName('coordinates')
                str_coord = pt_coordinates[0].firstChild.data
                coord_array = str_coord.split(",")
                if len(coord_array) == 2:
                    lng = float(coord_array[0])
                    lat = float(coord_array[1])
                    gpx.write(
                        "      <trkpt lat=\"{}\" lon=\"{}\">\n        <time>{}</time>\n      </trkpt>\n".format(lat, lng, timestamp)
                        )
                else:
                    print("What kind of coord is that? [{}]".format(str_coord))
            print("Done")
            gpx.write("    </trkseg>\n  </trk>\n</gpx>")
            gpx.close()
        else:
            print("What kind of file is that? Is it really a KML doc?")
    except FileNotFoundError:
        print("File {} not found...".format(kml_file_name))
        return
    print("Bye")


if __name__ == '__main__':
    main(sys.argv)
