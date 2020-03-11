#!/usr/bin/env bash

read -p "Artifact: " artifact

mvn -B archetype:generate \
    -DarchetypeGroupId=org.apache.maven.archetypes \
    -DgroupId=com.namazustudios.socialengine \
    -DartifactId=${artifact}

mvn_status=$?

if [[ ${mvn_status} -ne 0 ]]
then
    echo "Artifact generation failed."
    exit 1
fi

pom_xml=$(pwd)/pom.xml
pom_xml_tmp=$(mktemp)

XMLLINT_INDENT="    " xmllint --format pom.xml > ${pom_xml_tmp}

diff ${pom_xml} ${pom_xml_tmp}

read -p "Apply reformatting of pom? (Y/n)" apply

find ${artifact} -name "*.java"
read -p "Strip boilerplate Java source code files? (Y/n)" strip

if [[ ${apply} =~ ^[Yy]$ ]] || [[ -z ${apply} ]]
then
    cp ${pom_xml_tmp} ${pom_xml}
else
	echo "Skipping"
fi

if [[ ${strip} =~ ^[Yy]$ ]] || [[ -z ${strip}  ]]
then
    find ${artifact} -name "*.java" -exec rm {} \;
else
	echo "Skipping"
fi

git status

find ${artifact}
read -p "Add to git? (Y/n)" add

if [[ ${add} =~ ^[Yy]$ ]] || [[ -z ${add} ]]
then
    find ${artifact} -exec git add {} \;
else
	echo "Skipping"
fi
