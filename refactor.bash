#!/usr/bin/env bash

NEW_PACKAGE="dev.getelements"
OLD_PACKAGE="com.namazustudios.socialengine"

OLD_MAVEN_GROUPID="com.namazustudios.socialengine"
NEW_MAVEN_GROUPID="dev.getelements"

AUDIT_DIRECTORY=${AUDIT_DIRECTORY:=$(mktemp -d --suffix=-audit)}

function refactor_java_source {

    old_file=$1

    old_package=$2
    new_package=$3

    audit_directory=$4

    new_package_path_subst="${old_package//.//}"
    old_package_path_subst="${new_package//.//}"

    new_file=${old_file/$new_package_path_subst/$old_package_path_subst}

    new_directory="$(dirname "$new_file")"
    sed_package_expression="s#$old_package#$new_package#"

    diff_audit_file="$audit_directory/$new_file.diff"
    diff_audit_directory=$(dirname "$diff_audit_file")

    if [ "$(git rev-parse --show-toplevel)" = "$(pwd)" ] && [ "$old_file" != "$new_file" ]
    then
      echo "Refactoring ${old_file}"
      echo mkdir -p "$diff_audit_directory"
      echo mkdir -p "${new_directory}"
      echo git mv "${old_file}" "${new_file}"
      echo sed -i".orig" "$sed_package_expression" "$new_file"
      echo diff "${new_file}.orig" "$new_file" ">" "$diff_audit_file"
      echo rm "${new_file}.orig"
    fi

}

export -f refactor_java_source

echo "Renaming ${OLD_PACKAGE} -> ${NEW_PACKAGE}"
echo "Renaming Artifact Groups ${OLD_MAVEN_GROUPID} -> ${NEW_MAVEN_GROUPID}"
echo "Using Audit Directory ${audit_directory}"

find . -ipath "*/src/*/*.java" -type f -exec bash -c 'refactor_java_source "$1" "$2" "$3" $4' -- {} "${OLD_PACKAGE}" "${NEW_PACKAGE}" "${AUDIT_DIRECTORY}" \;
#find . -ipath "*/src/*/*.java" -type f -exec bash -c 'echo refactor_java_source "$1" "$2" "$3" && refactor_java_source "$1" "$2" "$3"' -- {} ${OLD_PACKAGE} ${NEW_PACKAGE} \;
#find . -ipath "*/src/*" -type f f -exec bash -c 'echo git mv $0 $(echo "$0" | sed s#com/namazustudios/socialengine#dev/eci#)' {} \;
#find . -name -not "pom.xml" -type f -exec bash -c 'echo sed -i.bak #com.namazustudios.socialengine#dev.eci $0' {} \;
