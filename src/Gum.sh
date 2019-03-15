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
        if [[ "$line" == "" ]]
        then
            break
        fi

        array[$index]="$line"
        index=$(($index+1))
    done < gump

    if [[ ${array[0]} != "" ]]; then
        name=${array[0]}
    fi

    if [[ ${array[1]} != "" ]]; then
        baseTestPath=${array[1]}
    fi

    if [[ ${array[2]} != "" ]]; then
        baseLibPath=${array[2]}
    fi

    if [[ ${array[3]} != "" ]]; then
        basePackagePath=${array[3]}
    fi

    if [[ ${array[4]} != "" ]]; then
        input=${array[4]}
    fi
}

function info() {
    echo "*INFO*****************************"
    echo "classDot: ${classDot}"
    echo "classSlash: ${classSlash}"
    echo "runJur: ${runJar}"
    echo "classPath: ${classPath}"
    echo "**********************************"
}

function setTask() {
    classDot="${baseClassDot}.${package}.${mainClass}"
    classSlash="${basePackagePath}/${baseClassSlash}/${package}/${mainClass}.java"
    runJar="${baseRunJar}.${package}"
    classPath="${basePackagePath}:${baseTestPath}:${baseLibPath}:${baseTestPath}/${runJar}.jar"
}

function compile() {
    rm -f ${classSlash}/*.class
    echo "Compiling..."
    javac -cp ${classPath} ${classSlash}
}

function run() {
    echo "Enter run arg:"
    read runArg
    java -cp ${classPath} -p . -m ${runJar} ${runArg} ${classDot}
}

function help() {
    echo "*HELP*****************************"
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
