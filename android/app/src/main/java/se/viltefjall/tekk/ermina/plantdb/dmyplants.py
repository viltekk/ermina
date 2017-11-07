#!/usr/bin/python

import random

xml = "<plants>\n";

for i in range(1, 21):
    lo = random.randrange(49)
    hi = lo + random.randrange(51)
    
    xml += "\t<plant>\n"
    xml += "\t\t<name>Plant {0}</name>\n".format(i)
    xml += "\t\t<latin>Plantus dumminus {0}</latin>\n".format(i)
    xml += "\t\t<hi>{0}</hi>\n".format(hi)
    xml += "\t\t<lo>{0}</lo>\n".format(lo)
    xml += "\t</plant>\n"

xml += "</plants>\n"
    
fp = open("plants.xml", "w")
fp.write(xml)
fp.close()
