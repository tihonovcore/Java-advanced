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

function setTask() {
    case ${task} in
#    "1" )
#        mainClass="Walk"
#        jar=""
#    ;;
    "2" )
        mainClass="ArraySet"
        package="arrayset"
    ;;
    "3" )
        mainClass="StudentDB"
        package="student"
    ;;
    "4" )
        mainClass="Implementor"
        package="implementor"
    ;;
    "5" )
        mainClass="Implementor"
        package="implementor"
    ;;
    "6" )
        mainClass="Implementor"
        package="implementor"
    ;;
    "7" )
        mainClass="IterativeParallelism"
        package="concurrent"
    ;;
    esac

    classDot="${baseClassDot}.${package}.${mainClass}"
    classSlash="${basePackagePath}/${baseClassSlash}/${package}/${mainClass}.java"
    runJar="${baseRunJar}.${package}"
    classPath="${basePackagePath}:${baseTestPath}:${baseLibPath}:${baseTestPath}/${runJar}.jar"

    echo "*******************************"
    echo "classDot: ${classDot}"
    echo "classSlash: ${classSlash}"
    echo "runJur: ${runJar}"
    echo "classPath: ${classPath}"
}

while [[ ${input} != "4" ]]
do
    echo "*******************************"
    echo "1 - Choose task"
    echo "2 - Execute"
    echo "3 - Help"
    echo "4 - Exit"
    echo "*******************************"
    echo "Choose command: "
    read input

    case ${input} in
        "1" )
        echo "Choose task:"
        read task
        if [[ 1 -le task ]] && [[ task -le 7 ]]
        then
            setTask
        else
            echo "Wrong task number: ${task}"
        fi
    ;;
        "2" )
        compile=0
        run=0

        echo "Enter execute argument (c/r/cr):"
        read args

        case ${args} in
        "r" )
            run=1
        ;;
        "c" )
            compile=1
        ;;
        "cr" )
            compile=1
            run=1
        ;;
        esac

        if (( compile == 1 ))
        then
            rm -f ${classSlash}/*.class
            echo "Compiling..."
            javac -cp ${classPath} ${classSlash}
        fi

        if (( run == 1 ))
        then
            echo "Enter run arg:"
            read runArg

            java -cp ${classPath} -p . -m ${runJar} ${runArg} ${classDot}
        fi
    ;;
        "3" )
            echo "*******************************"
            echo "Execute arguments: "
            echo "r - run"
            echo "c - compile"
            echo "cr - compile, then run"
    ;;
    esac
done

exit
