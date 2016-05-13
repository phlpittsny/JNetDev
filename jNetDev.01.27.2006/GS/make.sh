#!/bin/sh

export OS=${OS:-none}
case "$OS" in
  linux)
    make -f Makefile.linux $*
    ;;						
  win32)
    make -f Makefile.win32 $*
    ;;						
  osx)
    make -f Makefile.osx $*
    ;;
  * )						
    echo "Environment variable OS not set or set wrong."
    echo "Type: export OS=osname; make"
    echo "  where osname is one of linux, win32, or osx"
    ;;						
esac
