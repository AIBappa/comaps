#!/usr/bin/env bash

set -e

#Volumes/paths for downloads:
#home/planet/planet/planet.o5m
#home/planet/planet/planet.o5m.md5
#PLANET_COASTS_URL:file:///home/planet/planet/
  #home/planet/planet/latest_coasts.geom and latest_coasts.rawgeom
#SUBWAY_URL: file:///home/planet/subway/beta.json
  #home/planet/subway/beta.json
#HOTELS_URL:/home/planet/planet/kayak/
  #home/planet/planet/kayak/
#SRTM_PATH:/home/planet/SRTM-patched-europe/
#ISOLINES_PATH:/home/planet/planet/isolines/
#ADDRESSES_PATH:/home/planet/planet/tiger/
#UK_POSTCODES_URL:/home/planet/postcodes/gb-postcode-data/gb_postcodes.csv
#US_POSTCODES_URL:/home/planet/postcodes/us-postcodes/uszips.csv

echo "<$(date +%T)> Starting..."

# Prepare paths
#
# Already created by Dockerfile:
#   /root/OM
#   /root/OM/organicmaps
#   /root/OM/maps_build
#   /home/planet
#
mkdir -p /root/.config/CoMaps # Odd mkdir permission errors in generator_tool in Docker without these
chmod -R 777 /root/.config
mkdir -p ~/.config/rclone
mkdir -p ~/OM/maps_build
mkdir -p ~/OM/omim-build-release
mkdir -p ~/OM/osmctools
mkdir -p /home/planet/planet/isolines/
mkdir -p /home/planet/planet/kayak/
mkdir -p /home/planet/planet/tiger/
mkdir -p /home/planet/postcodes/gb-postcode-data/
mkdir -p /home/planet/postcodes/us-postcodes/
mkdir -p /home/planet/SRTM-patched-europe/
mkdir -p /home/planet/subway

echo "Writing rclone config..."
echo "[r2]" > ~/.config/rclone/rclone.conf
echo "type = s3" >> ~/.config/rclone/rclone.conf
echo "provider = Cloudflare" >> ~/.config/rclone/rclone.conf
echo "access_key_id = $S3_KEY_ID" >> ~/.config/rclone/rclone.conf
echo "secret_access_key = $S3_SECRET_KEY" >> ~/.config/rclone/rclone.conf
echo "region = auto" >> ~/.config/rclone/rclone.conf
echo "endpoint = $S3_ENDPOINT" >> ~/.config/rclone/rclone.conf
# S3_BUCKET is used below during uploading

echo "<$(date +%T)> Running ./configure.sh ..."
cd ~/OM/organicmaps
./configure.sh --skip-map-download --skip-generate-symbols

echo "<$(date +%T)> Compiling tools..."
cd ~/OM/organicmaps
./tools/unix/build_omim.sh -R generator_tool
./tools/unix/build_omim.sh -R world_roads_builder_tool
./tools/unix/build_omim.sh -R mwm_diff_tool
cd tools/python/maps_generator
python3 -m venv /tmp/venv
/tmp/venv/bin/pip3 install -r requirements_dev.txt

echo "<$(date +%T)> Copying map generator INI..."
cp var/etc/map_generator.ini.prod var/etc/map_generator.ini

#TODO: may be duplicated by maps_generator at "osmctools are not found, building from the sources"
#echo "<$(date +%T)> Prebuild some tools so we can make an o5m file or run update_planet..."
#cd ~/OM/organicmaps/tools/osmctools
#gcc osmupdate.c -l z -o ~/OM/osmctools/osmupdate
#gcc osmconvert.c -l z -o ~/OM/osmctools/osmconvert

# May be unnecessary when running world
# /tmp/venv/bin/python -m maps_generator --coasts
# save to /path/to/coasts WorldCoasts.geom as latest_coasts.geom and WorldCoasts.rawgeom latest_coasts.rawgeom
# (from https://github.com/mapsme/omim/issues/11994)

cd /home/planet/planet
if [ ! -f planet-latest.osm.pbf ]; then
  echo "<$(date +%T)> Downloading planet-latest.osm.pbf..."
  curl -OL https://ftpmirror.your.org/pub/openstreetmap/pbf/planet-latest.osm.pbf
  echo "<$(date +%T)> Downloading planet-latest.osm.pbf.md5..."
  curl -OL https://ftpmirror.your.org/pub/openstreetmap/pbf/planet-latest.osm.pbf.md5
else
  echo "<$(date +%T)> planet-latest.osm.pbf exists, not downloading..."
fi

#curl -OL https://download.geofabrik.de/north-america/us-west-latest.osm.pbf
#curl -OL https://download.geofabrik.de/north-america/us-west-latest.osm.pbf.md5
# (rename us-west-latest to planet-latest and edit the md5 file accordingly)
if [ ! -f planet.o5m ]; then
  echo "<$(date +%T)> Converting planet-latest.osm.pbf to planet.o5m..."
  ~/OM/osmctools/osmconvert planet-latest.osm.pbf -o=planet.o5m
else
  echo "<$(date +%T)> planet.o5m exists, not converting..."
fi
# (currently unused:) ~/OM/organicmaps/tools/unix/update_planet.sh planet.o5m


echo "<$(date +%T)> Generating maps..."
cd ~/OM/organicmaps/tools/python
/tmp/venv/bin/python -m maps_generator --skip="MwmDiffs"
#/tmp/venv/bin/python -m maps_generator --skip="MwmDiffs" --continue

# do not use --production except for Kayak/recommendation/popularity/food data
#/tmp/venv/bin/python -m maps_generator --countries="World, WorldCoasts, US_Oregon_*, US_California_*, US_Washington_*" --production
#/tmp/venv/bin/python -m maps_generator --countries="US_Oregon_Portland" --skip="MwmDiffs"
#/tmp/venv/bin/python -m maps_generator --countries="Macedonia" --skip="MwmDiffs"

shopt -s nullglob
buildfolder=$(find ~/OM/maps_build/ -mindepth 1 -maxdepth 1 -iname 2* -type d | sort -n -r | head -1 | cut -d/ -f5)
builddate=$(find ~/OM/maps_build/*/ -mindepth 1 -maxdepth 1 -iname 2* -type d | sort -n -r | head -1 | cut -d/ -f6)
mwmfiles=( ~/OM/maps_build/$buildfolder/$builddate/*.mwm )

if (( ${#mwmfiles[@]} )); then
  echo "<$(date +%T)> Uploading maps to sftp..."
  # upload limited files via SFTP to Dreamhost (cdn-us-1.comaps.app)
  # Needs StrictHostKeyChecking=no otherwise new containers/SFTP_HOSTs will require a manual ssh attempt
sshpass -p $SFTP_PASSWORD sftp -o StrictHostKeyChecking=no $SFTP_USER@$SFTP_HOST:$SFTP_PATH <<EOF
  lcd ~/OM/maps_build/$buildfolder/$builddate
  mkdir maps/$builddate
  cd maps/$builddate
  put countries.txt
  put World.mwm
  put WorldCoasts.mwm
  cd ..
  rm latest
  ln -s $builddate latest
  cd ..
  lcd /home/planet/subway/
  put subway.json
  put subway.log
  put subway.transit.json
  lcd /home/planet/subway/subway/validator
  rm subway/js/*
  rmdir subway/js
  rm subway/*
  rmdir subway
  mkdir subway
  cd subway
  put *
  exit
EOF

  # upload all files via rclone to Cloudflare (R2)
  echo "<$(date +%T)> Uploading maps to cloudflare..."
  rclone --progress copy ~/OM/maps_build/$buildfolder/$builddate r2:$S3_BUCKET/maps/$builddate/

else
  echo "<$(date +%T)> No MWM files in ~/OM/maps_build/$buildfolder/$builddate/*.mwm, not uploading maps."
  echo "<$(date +%T)> Found: $(ls -alt ~/OM/maps_build/*)"
fi

echo "<$(date +%T)> Temporarily NOT Removing intermediate data..."
#rm -rf ~/OM/maps_build/*/intermediate_data

echo "<$(date +%T)> DONE"

