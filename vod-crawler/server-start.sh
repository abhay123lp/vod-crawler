#!/bin/bash

findself() {
    SELF=`dirname $0`
}
findself

$SELF/dist-crawl-server.sh $* > out.log 2> err.log