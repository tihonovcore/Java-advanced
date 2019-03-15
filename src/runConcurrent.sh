#!/bin/bash

rm -f ru/ifmo/rain/tihonov/concurrent/*.class

javac -cp /home/tihonovcore/IdeaProjects/Java-advanced/src/info.kgeorgiy.java.advanced.concurrent.jar ru/ifmo/rain/tihonov/concurrent/IterativeParallelism.java

java -cp /home/tihonovcore/IdeaProjects/Java-advanced/src/info.kgeorgiy.java.advanced.concurrent.jar:/home/tihonovcore/IdeaProjects/Java-advanced/src -p . -m info.kgeorgiy.java.advanced.concurrent $1 ru.ifmo.rain.tihonov.concurrent.IterativeParallelism

exit
