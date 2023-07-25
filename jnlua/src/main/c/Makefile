
JAVA_HOME?=/usr/lib/jvm/java-11-openjdk-amd64

LUA_VERSION?=53
LUA_INCLUDE?=lua/linux$(LUA_VERSION)/include

JAVA_INCLUDE=$(JAVA_HOME)/include
JAVA_INCLUDE_LINUX=$(JAVA_HOME)/include/linux

INCLUDE=-I$(JAVA_INCLUDE) -I$(JAVA_INCLUDE_LINUX) -I$(LUA_INCLUDE)
LDPATH=-Llua/linux$(LUA_VERSION)

CFLAGS=-c -fno-strict-aliasing -m64 -fPIC -O2 -Wall -DNDEBUG -D_REENTRANT -DLUA_USE_LINUX
LDFLAGS=-shared -ldl

build/libjnlua.so: build/jnlua.o
	cc $(LDFLAGS) $(LDPATH) build/jnlua.o -o build/libjnlua.so

build:
	mkdir build

build/jnlua.o: build
	cc $(CFLAGS) $(INCLUDE) -c jnlua/jnlua.c -o build/jnlua.o

clean:
	rm -rf build
