#
# minidom doc at https://docs.python.org/3/library/xml.dom.minidom.html
#
from xml.dom import minidom

xml_doc = minidom.parse('./xml/items.xml')
item_list = xml_doc.getElementsByTagName('item')
print(f"Nb Items: {len(item_list)}")
print("first name:", item_list[0].attributes['name'].value)
# Loop
for s in item_list:
    # print(f"node is a {type(s)}")
    print("name attribute:", s.attributes['name'].value, ", tag name:", s.tagName, ", value:", s.firstChild.nodeValue)
