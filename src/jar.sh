#!/bin/bash

src="/home/tihonovcore/IdeaProjects/Java-advanced/src"
tihonov="ru/ifmo/rain/tihonov/implementor"
geo="info/kgeorgiy/java/advanced/implementor"

jar xf $src/info.kgeorgiy.java.advanced.implementor.jar $geo/Impler.class $geo/ImplerException.class $geo/JarImpler.class
jar cfm ru.ifmo.rain.tihonov.implementor.jar $tihonov/MANIFEST.MF $tihonov/Implementor.class $geo/Impler.class $geo/ImplerException.class $geo/JarImpler.class
rm -r info

exit
