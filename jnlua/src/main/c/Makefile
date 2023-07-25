
GCC_IMAGE=gcc:11.3.0

JAVA_HOME=/usr/lib/jvm/java-11-openjdk-$(JAVA_ARCH)

LUA_VERSION?=53
LUA_INCLUDE?=lua/linux$(LUA_VERSION)/include

JAVA_INCLUDE=$(JAVA_HOME)/include
JAVA_INCLUDE_LINUX=$(JAVA_HOME)/include/linux

INCLUDE=-I$(JAVA_INCLUDE) -I$(JAVA_INCLUDE_LINUX) -I$(LUA_INCLUDE)
LDPATH=-Llua/linux$(LUA_VERSION)

CFLAGS=-c -fno-strict-aliasing -fPIC -O2 -Wall -DNDEBUG -D_REENTRANT -DLUA_USE_LINUX
LDFLAGS=-shared -ldl

all:
	make docker DOCKER_ARCH=amd64 JAVA_ARCH=amd64
	make docker DOCKER_ARCH=arm64 JAVA_ARCH=arm64

docker:
	docker run \
		--rm \
		--platform "linux/$(DOCKER_ARCH)" \
		--volume .:/opt \
		$(GCC_IMAGE) \
		bash -c  "\
			apt-get update && \
			apt-get install -y openjdk-11-jdk && \
			make -C /opt build/$(DOCKER_ARCH)/libjnlua.so DOCKER_ARCH=$(DOCKER_ARCH) JAVA_ARCH=$(JAVA_ARCH) \
		"

build/$(DOCKER_ARCH)/libjnlua.o: build/$(DOCKER_ARCH)
	cc $(CFLAGS) $(INCLUDE) -c jnlua/jnlua.c -o build/$(DOCKER_ARCH)/jnlua.o

build/$(DOCKER_ARCH)/libjnlua.so: build/$(DOCKER_ARCH)/libjnlua.o
	cc $(LDFLAGS) $(LDPATH) build/$(DOCKER_ARCH)/jnlua.o -o build/$(DOCKER_ARCH)/libjnlua.so

build:
	mkdir build

build/$(DOCKER_ARCH): build
	mkdir -p build/$(DOCKER_ARCH)

clean:
	rm -rf build
