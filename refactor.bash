#!/usr/bin/env bash

NEW_PACKAGE=${NEW_PACKAGE:="dev.eci.elements"}
OLD_PACKAGE=${OLD_PACKAGE:="com.namazustudios.socialengine"}

NEW_MAVEN_GROUPID=${NEW_MAVEN_GROUPID:="dev.eci.elements"}
OLD_MAVEN_GROUPID=${OLD_MAVEN_GROUPID:="com.namazustudios.socialengine"}

function refactor_java_source {

    old_file=$1

    old_package=$2
    new_package=$3

    audit_directory=$4

    new_package_path_subst="${old_package//.//}"
    old_package_path_subst="${new_package//.//}"

    new_file=${old_file/$new_package_path_subst/$old_package_path_subst}

    new_directory="$(dirname "$new_file")"
    old_directory="$(dirname "$old_file")"

    sed_package_expression="s#$old_package#$new_package#"

    diff_audit_file="$audit_directory/$new_file.diff"
    diff_audit_directory=$(dirname "$diff_audit_file")

    git_root="$(pushd "$old_directory" > /dev/null && git rev-parse --show-toplevel && popd > /dev/null)"

    if [ "${git_root}" = "$(pwd)" ] && [ "$old_file" != "$new_file" ]
    then
      echo "echo Refactoring ${old_file}"
      echo mkdir -p "$diff_audit_directory"
      echo mkdir -p "${new_directory}"
      echo git mv "${old_file}" "${new_file}"
      echo sed -i".orig" "$sed_package_expression" "$new_file"
      echo diff "${new_file}.orig" "$new_file" ">" "$diff_audit_file"
      echo rm "${new_file}.orig"
    fi

}

function refactor_maven_pom {

    pom_file=$1
    directory=$(dirname "$pom_file")

    old_group_id=$2
    new_group_id=$3

    audit_directory=$4

    sed_group_id_expression="s#$old_group_id#$new_group_id#"

    diff_audit_file="$audit_directory/$pom_file.diff"
    diff_audit_directory=$(dirname "$diff_audit_file")

    git_root="$(pushd "$directory" > /dev/null && git rev-parse --show-toplevel && popd > /dev/null)"

    if [ "${git_root}" = "$(pwd)" ]
    then
      echo "echo Refactoring ${pom_file}"
      echo mkdir -p "$diff_audit_directory"
      echo sed -i".orig" "$sed_group_id_expression" "$pom_file"
      echo diff "${pom_file}.orig" "$pom_file" ">" "$diff_audit_file"
      echo rm "${pom_file}.orig"
    fi

}

export -f refactor_maven_pom
export -f refactor_java_source

AUDIT_DIRECTORY=${AUDIT_DIRECTORY:=$(mktemp -d --suffix=-audit)}

echo "#!$(which bash)"
echo "echo Renaming ${OLD_PACKAGE} -> ${NEW_PACKAGE}"
echo "echo Renaming Artifact Groups ${OLD_MAVEN_GROUPID} -> ${NEW_MAVEN_GROUPID}"
echo "echo Using Audit Directory ${AUDIT_DIRECTORY}"

find . -ipath "*/src/*/*.java" -type f -exec bash -c 'refactor_java_source "$1" "$2" "$3" $4' -- {} "${OLD_PACKAGE}" "${NEW_PACKAGE}" "${AUDIT_DIRECTORY}" \;
find . -ipath "*/pom.xml" -type f -exec bash -c 'refactor_maven_pom "$1" "$2" "$3" $4' -- {} "${OLD_PACKAGE}" "${NEW_PACKAGE}" "${AUDIT_DIRECTORY}" \;

echo
echo "echo Review changes and diffs in ${AUDIT_DIRECTORY}"

echo
echo "# NOTE: This script generates the operations to refactor. Use '$0 | bash' to apply operations."
echo
