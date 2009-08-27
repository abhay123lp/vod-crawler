#!/bin/bash

findself() {
    SELF=`dirname $0`
}
findself

shopt -s huponexit
bash $SELF/dist-crawl-server.sh $* > $SELF/out.log 2> $SELF/err.log &