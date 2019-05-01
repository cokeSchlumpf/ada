#!/bin/sh

BIN_DIR=`dirname "$0"`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# memory settings
HEAP_OPTS="-XX:TieredStopAtLevel=1 -Xverify:none -Xms2g -Xmx2g"

# garbage collection
GC_OPTS="-XX:+UseConcMarkSweepGC -XX:+UseParNewGC"

JAVA_OPTS="-Dfile.encoding=UTF-8 $GC_OPTS $HEAP_OPTS"

java -jar ${DIR}/ada-vcs.jar "$@"
