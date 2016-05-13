#!/bin/sh

export OS=${OS:-none}
case "$OS" in
  linux)
    cd jNetDev; make -f Makefile.linux $*; cd ..
    cd src; make -f Makefile.linux $*; cd ..
    ;;						
  win32)
    cd jNetDev; make -f Makefile.win32 $*; cd ..
    cd src; make -f Makefile.win32 $*; cd ..
    ;;						
  osx)
    cd jNetDev; make -f Makefile.osx $*; cd ..
    cd src; make -f Makefile.osx $*; cd ..
    ;;
  * )						
    echo "Environment variable OS not set or set wrong."
    echo "Type: export OS=osname; make"
    echo "  where osname is one of linux, win32, or osx"
    ;;						
esac
