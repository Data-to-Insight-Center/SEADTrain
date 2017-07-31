#!/bin/sh

date=$(date -d "-1 days" +"%Y-%m-%d")
url="http://localhost:8083/sead-client/rest/streamro"
folder="/data/folder/"$date"/"
project="airbox"
creator="Test Creator"
abstract="test abstract1"
title="test title1"
repository="iusc-azure"
postString="{\"folder\" : \""$folder"\", \"project\" : \""$project"\", \"creator\": \""$creator"\", \"abstract\" : \""$abstract"\", \"title\" : \""$title"\", \"repository\" : \""$repository"\"}"

echo publishing data in $folder
curl -H "Content-Type: application/json" -X POST -d ''"$postString"'' $url
