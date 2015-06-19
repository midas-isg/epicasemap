#!/bin/bash

RE='\bebola\b'

# geo-tagged
# columns: timestamp, longitude, latitude (WGS84)
cat $1/pre/20{13,14,15}-*.geo.tsv \
    | cut -d'	' -f2,3,9,10 \
    | egrep -i $RE \
    | cut -d'	' -f1,3,4 \
    | pv \
    | xz -7 \
    > ebola_geo.tsv.xz

# location strings
# columns: timestamp, user location, user time zone
cat $1/pre/20{13,14,15}-*.all.tsv \
    | cut -d '	' -f2,3,7,8 \
    | egrep -i $RE \
    | cut -d'	' -f1,3,4 \
    | grep -Pv '\t\t$' \
    | pv \
    | xz -7 \
    > ebola_location-tz.tsv.xz
