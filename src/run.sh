#!/bin/bash

rm -f ru/ifmo/rain/tihonov/implementor/*.class

javac -cp /home/tihonovcore/IdeaProjects/Java-advanced/src/info.kgeorgiy.java.advanced.implementor.jar ru/ifmo/rain/tihonov/implementor/Implementor.java

java -cp /home/tihonovcore/IdeaProjects/Java-advanced/src/info.kgeorgiy.java.advanced.implementor.jar:/home/tihonovcore/IdeaProjects/Java-advanced/src -p . -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.tihonov.implementor.Implementor

exit
