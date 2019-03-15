    #!/bin/bash

#your surname
name="tihonov"

#package contains *.jar (tests)
baseTestPath="/home/tihonovcore/Документы/java-advanced-2019/artifacts"
#package contains *.jar (libs)
baseLibPath="/home/tihonovcore/Документы/java-advanced-2019/lib"
#package contains ru/ifmo/rain/...
basePackagePath="/home/tihonovcore/IdeaProjects/Java-advanced/src"

#prefix of class name
baseClassDot="ru.ifmo.rain.${name}"
#prefix of relative path
baseClassSlash="ru/ifmo/rain/${name}"
#prefix of jar name
baseRunJar="info.kgeorgiy.java.advanced"

function gump() {
    array=()
    index=0
    while read line;
    do
        array[$index]="$line"
        index=$(($index+1))
    done < gump

    name=${array[0]}
    baseTestPath=${array[1]}
    baseLibPath=${array[2]}
    basePackagePath=${array[3]}
    input=${array[4]}
    salt=${array[5]}
}

function info() {
    echo "**********************************"
    echo "Full class name: ${classDot}"
    echo "Absolute class path: ${classSlash}"
    echo "Test package: ${runJar}"
    echo "classpath: ${classPath}"
    echo "**********************************"
}

function setTask() {
    classDot="${baseClassDot}.${package}.${mainClass}"
    classSlash="${basePackagePath}/${baseClassSlash}/${package}/${mainClass}.java"
    runJar="${baseRunJar}.${package}"
    classPath="${basePackagePath}:${baseTestPath}:${baseLibPath}:${baseTestPath}/${runJar}.jar"
    runArg=""
}

function compile() {
    rm -f ${classSlash}/*.class
    echo "Compiling..."
    javac -cp ${classPath} ${classSlash}
}

function run() {
    if [[ "$runArg" == "" ]]; then
        echo "Enter run argument (easy/hard):"
        read runArg
    fi
    runTest
}

function runTest() {
    if [[ "$salt" == "" ]]; then
        java -cp ${classPath} -p . -m ${runJar} ${runArg} ${classDot}
    else
        java -cp ${classPath} -p . -m ${runJar} ${runArg} ${classDot} ${salt}
    fi
}

function help() {
    echo "**********************************"
    echo "<number> - set current task"
    echo "r - run current task"
    echo "c - compile current task"
    echo "cr - compile and run current task"
    echo "**********************************"
}

gump
help
while [[ true ]]
do
    case ${input} in
    "1" )
        mainClass="Walk"
        package="walk"
        setTask
    ;;
    "2" )
        mainClass="ArraySet"
        package="arrayset"
        setTask
    ;;
    "3" )
        mainClass="StudentDB"
        package="student"
        setTask
    ;;
    "4" )
        mainClass="Implementor"
        package="implementor"
        setTask
    ;;
    "5" )
        mainClass="Implementor"
        package="implementor"
        setTask
    ;;
    "6" )
        mainClass="Implementor"
        package="implementor"
        setTask
    ;;
    "7" )
        mainClass="IterativeParallelism"
        package="concurrent"
        setTask
    ;;
    "r" )
        run
    ;;
    "c" )
        compile
    ;;
    "cr" )
        compile
        run
    ;;
    "gump" )
        gump
        setTask
    ;;
    "info" )
        info
    ;;
    "help" )
        help
    ;;
    "exit" )
        exit
    ;;
    esac
    echo "Enter command: "
    read input
done
exit
