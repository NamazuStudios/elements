
JAVA_HOME?=$(/usr/libexec/java_home)

LUA_VERSION?=53
LUA_INCLUDE?=lua/darwin$(LUA_VERSION)/include

JAVA_INCLUDE=$(JAVA_HOME)/include
JAVA_INCLUDE_DARWIN=$(JAVA_HOME)/include/darwin

INCLUDE=-Ijnlua -I$(JAVA_INCLUDE) -I$(JAVA_INCLUDE_DARWIN) -I$(LUA_INCLUDE)

CFLAGS=-c -fno-strict-aliasing -m64 -fPIC -g -O0 -Wall -D_REENTRANT -DLUA_USE_MACOSX
LDFLAGS=-dynamiclib -undefined suppress -flat_namespace

all: install

install: build/libjnlua.dylib build/libjnlua.so
	cp  build/libjnlua.dylib ../resources/darwin
	cp  build/libjnlua.so ../resources/linux-x86-64

build/libjnlua.so:
	docker build --build-arg LUA_VERSION=$(LUA_VERSION) --tag jnlua_build:latest .
	docker run --name jnlua_build jnlua_build:latest
	docker cp jnlua_build:/opt/jnlua/build/libjnlua.so build/libjnlua.so
	docker rm jnlua_build

build:
	mkdir build

build/jnlua.o: build jnlua/jnlua.c
	cc $(CFLAGS) $(INCLUDE) -c jnlua/jnlua.c -o build/jnlua.o

build/libjnlua.dylib: build/jnlua.o
	cc $(LDFLAGS) $(LDPATH) build/jnlua.o -o build/libjnlua.dylib

clean:
	rm -rf build
	- docker rm jnlua_build
	- docker rmi jnlua_build:latest
