#!/usr/bin/env python

#This script is ugly, it gets the job done!

import sys, subprocess, shutil, os

if len(sys.argv) < 2:
    print >>sys.stderr, 'Usage %s <%s>' %(sys.argv[0], 'hosts file')
    sys.exit(1)

hfile = sys.argv[1]

SELF = os.path.split(os.path.realpath(sys.argv[0]))[0]
HOME = os.environ['HOME']

hosts = {}
with open(hfile) as f:
    for l in f:
        spl = l.split(':')
        hosts[spl[0]] = int(spl[1])

print 'Creating package... '
pack = SELF + os.sep + 'pack.tar.gz'
cmd = 'bash %s'%(SELF + os.sep + 'compact.sh')
retv = subprocess.call(cmd.split())
print 'Done!'

print
print 'Testing hosts: '
for h in hosts:
    cmd = 'ssh %s echo -n'%h
    retv = subprocess.call(cmd.split())
    if retv == 0:
        print '%s appears to be ONLINE'%h
    else:
        print '%s appears to be OFFLINE'%h

print
print 'Sending package: '
for h in hosts:
    dep_dir = HOME + '/.vod-crawler/' + h+"_"+str(hosts[h])
    try:
        os.makedirs(dep_dir)
    except:
        pass

    try:
        shutil.copy(pack, dep_dir)
        cmd = 'bash %s %s'%(SELF + os.sep + 'uncompact.sh', dep_dir)
        retv = subprocess.call(cmd.split())
        print 'Copied package to %s'%h
    except:
        print 'Unable to copy package to %s'%h

print
print 'Starting Hosts: '
for h in hosts:
    init_cmd = HOME + '/.vod-crawler/' + h+"_"+str(hosts[h])+ '/dist-crawl-server.sh'
    cmd = 'ssh %s %s %d'%(h, init_cmd, hosts[h])
    print cmd
