#!/usr/bin/env python

import sys, subprocess, shutil, os

SELF = os.path.split(os.path.realpath(sys.argv[0]))[0]
HOME = os.environ['HOME']
PACK = SELF + os.sep + 'pack.tar.gz'

def _test(h, p):
    cmd = 'ssh %s echo -n'%h
    retv = subprocess.call(cmd.split())
    if retv == 0:
        print '[%s] appears to be ONLINE for SSH'%h
    else:
        print '[%s] appears to be OFFLINE for SHH'%h

def _copy(h, p):
    dep_dir = HOME + '/.vod-crawler/' + h+"_"+str(p)
    try:
        os.makedirs(dep_dir)
    except:
        pass

    try:
        shutil.copy(PACK, dep_dir)
        cmd = 'bash %s %s'%(SELF + os.sep + 'uncompact.sh', dep_dir)
        retv = subprocess.call(cmd.split())
        print '[%s:%d] Copied package to folder' %(h, p)
    except:
        print '[%s:%d] Unable to copy package to folder' %(h, p)

def _status(h, p):
    stat_cmd = HOME + '/.vod-crawler/' + h+"_"+str(p)+ '/server-status.sh'
    cmd = '%s %d'%(stat_cmd, h, p)
    retv = subprocess.call(cmd.split())
    
    if retv == 0:
        print '[%s:%d] appears to be ONLINE' %h
    else:
        print '[%s:%d] appears to be OFFLINE'%h

def _start(h, p):
    init_cmd = HOME + '/.vod-crawler/' + h+"_"+str(p)+ '/server-start.sh'
    cmd = 'ssh %s %s %d'%(h, init_cmd, p)
    retv = subprocess.call(cmd.split())
    
    if retv == 0:
        print '[%s:%d] was STARTED' %h
    elif retv == 1:
        print '[%s:%d] was already ONLINE'%h
    else:
        print '[%s:%d] was not stated!'%h

def _stop(h, p):
    stop_cmd = HOME + '/.vod-crawler/' + h+"_"+str(p)+ '/server-stop.sh'
    cmd = '%s %d'%(stop_cmd, h, p)
    retv = subprocess.call(cmd.split())
    
    if retv == 0:
        print '[%s:%d] was STOPPED' %h
    elif retv == 1:
        print '[%s:%d] was already OFFLINE'%h
    else:
        print '[%s:%d] was not stopped!'%h

def _clean(h, p):
    dep_dir = HOME + '/.vod-crawler/' + h+"_"+str(p)
    try:
        shutil.rmtree(dep_dir)
        print '[%s:%d] folder was DELETED'%h
    except:
        print '[%s:%d] folder was already DELETED'%h

def _phelp():
    print >>sys.stderr, 'Usage %s < hosts file > < start | stop | restart | status | clean | copy | ssh-check > [-c recreate package]' %sys.argv[0]

#Main
if len(sys.argv) < 3:
    _phelp()
    sys.exit(1)

hfile = sys.argv[1]
action = sys.argv[2]
recreate = len(sys.argv) >= 4 and sys.argv[3] == '-c' 
switch = {
          'start':_start,
          'stop':_stop,
          'restart':_restart,
          'status':_status,
          'clean':_clean,
          'copy':_copy,
          'ssh-check':_test,
          }

hosts = {}
with open(hfile) as f:
    for l in f:
        spl = l.split(':')
        hosts[spl[0]] = int(spl[1])
        
if recreate:
    print 'Creating package... '
    cmd = 'bash %s'%(SELF + os.sep + 'compact.sh')
    retv = subprocess.call(cmd.split())

if action in swicth:
    print 'Performing %s action on all hosts: '%action
    for h in hosts:
        switch[action](h, hosts[h])
else:
    _phelp()
    sys.exit(1)