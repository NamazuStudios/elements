FROM gcc

# Creates the build directories
ARG LUA_VERSION

RUN mkdir -p /opt/jnlua/build

# Installs dependencies

RUN apt-get update
RUN apt-get install -y openjdk-8-jdk

# Copies all the dependent sources over to the container
COPY lua /opt/jnlua/lua
COPY jnlua /opt/jnlua/jnlua
COPY Makefile.docker /opt/jnlua/Makefile

RUN make LUA_VERSION=${LUA_VERSION} --directory /opt/jnlua
