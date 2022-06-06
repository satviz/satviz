#!/bin/bash

if [ $# -lt 3 ]; then 
	echo "Usage: $0 [cnf] [drat] [ncontractions] [nodesize]"
	exit 0
fi

print_config() {
	TAB='  '
	echo {
	echo "$TAB"\"modeConfig\" : {
	echo "$TAB$TAB"\"mode\" : \"EMBEDDED\",
	echo "$TAB$TAB"\"source\" : \"PROOF\",
	echo "$TAB$TAB"\"sourcePath\" : \"$2\"
	echo "$TAB"},
	echo "$TAB"\"instancePath\" : \"$1\",
	echo "$TAB"\"noGui\" : false,
	echo "$TAB"\"videoTemplatePath\" : \"/home/iser/satviz/recordings/1654079807250video-{}.ogv\",
	echo "$TAB"\"recordImmediately\" : false,
	echo "$TAB"\"bufferSize\" : 10,
	echo "$TAB"\"weightFactor\" : \"RECIPROCAL\",
	echo "$TAB"\"heatmapImplementation\" : \"RECENCY\",
	echo "$TAB"\"windowSize\" : 1000,
	echo "$TAB"\"vigImplementation\" : \"RING\",
	echo "$TAB"\"contractionIterations\" : $3,
	echo "$TAB"\"period\" : 33,
	echo "$TAB"\"videoTimeout\" : 60,
	echo "$TAB"\"theme\" : {
	echo "$TAB$TAB"\"bgColor\" : \"#FFFFFF00\",
	echo "$TAB$TAB"\"edgeColor\" : \"#6b787f\",
	echo "$TAB$TAB"\"nodeSize\" : $4,
	echo "$TAB$TAB"\"hotColor\" : \"#ff005c\",
	echo "$TAB$TAB"\"coldColor\" : \"#323a8b\"
	echo "$TAB"}
	echo }
	# default: bgColor = #FFFFFF00, edgeColor = #2E2A2CFF, hotColor = #FF9139FF, coldColor = #388BE8FF
	# berry: coldColor = #323a8b, hotColor = #ff005c, bgColor = #23272A, edgeColor = #6b787f
}

#print_config $1 $2 $3 > tmp.json
#satviz config tmp.json
satviz config <(print_config $1 $2 $3 $4)
