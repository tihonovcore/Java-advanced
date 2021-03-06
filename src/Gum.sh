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

#out path
outPath="${basePackagePath}/out"

#git
git="https://www.kgeorgiy.info/git/geo/java-advanced-2019"

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

    if [[ "${array[6]}" != "" ]]; then
        outPath=${array[6]}
    fi

    if [[ "${input}" == "" ]]; then
        input="8" #default task
    fi
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
    task="${input}"

    baseClassDot="ru.ifmo.rain.${name}"
    baseClassSlash="ru/ifmo/rain/${name}"
    baseRunJar="info.kgeorgiy.java.advanced"

    runJar="${baseRunJar}.${package}"
    classDot="${baseClassDot}.${package}.${mainClass}"
    classSlash="${basePackagePath}/${baseClassSlash}/${package}/${mainClass}.java"
    classPath="${basePackagePath}:${baseTestPath}:${baseLibPath}:${baseTestPath}/${runJar}.jar:${outPath}"
    runArg=""

    if [[ "$input" == "7" ]]; then
        classPath="${classPath}:${baseTestPath}/${baseRunJar}.mapper.jar"
    fi

    if [[ "$input" == "8" ]]; then
        classDot="${classDot},${baseClassDot}.concurrent.IterativeParallelism"
        classPath="${classPath}:${baseTestPath}/${baseRunJar}.concurrent.jar"
    fi

    echo "Task ${input} selected"
}

function compile() {
    echo "Compiling..."
    javac -cp ${classPath} -d ${outPath} ${classSlash}

    if [[ "${task}" == "8" ]]; then
        javac -cp ${classPath} -d ${outPath} "${basePackagePath}/${baseClassSlash}/concurrent/IterativeParallelism.java"
    fi
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
        java -cp ${classPath} -p ${classPath} -m ${runJar} ${runArg} ${classDot}
    else
        java -cp ${classPath} -p ${classPath} -m ${runJar} ${runArg} ${classDot} ${salt}
    fi
}

function clone() {
    rm -R java-advanced-2019
    git clone ${git}
}

function help() {
    echo "**********************************"
    echo "<number> - set current task"
    echo "r - run current task"
    echo "c - compile current task"
    echo "cr - compile and run current task"
    echo "gump - считывает файл 'gump' заново"
    echo "clone - клонит реп с тестами в текущую директорию, копирует небходимые либы и кладёт их рядом со скриптом"
    echo "info - выдаёт информацию о classpath и других переменных скрипта"
    echo "help - выдаёт короткую справку"
    echo "exit - завершает работу"
    echo "**********************************"
}

function check() {
    echo "**********************************"
    echo "Last changing ${runJar}.jar: "
    stat -c%z ${baseTestPath}/${runJar}.jar
    echo "**********************************"
}

gump
help
while [[ true ]]
do
    case ${input} in
    "1" )
        mainClass="RecursiveWalk"
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
    "8" )
        mainClass="ParallelMapperImpl"
        package="mapper"
        setTask
    ;;
    "9" )
        mainClass="WebCrawler"
        package="crawler"
        setTask
    ;;
    "10s" )
        mainClass="HelloUDPServer"
        package="hello"
        setTask
    ;;
    "10c" )
        mainClass="HelloUDPClient"
        package="hello"
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
    "clone" )
        clone
    ;;
    "check" )
        check
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
