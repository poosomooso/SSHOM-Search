#!/bin/bash

java -jar hom-generator.jar naive > data/naive.txt
java -jar hom-generator.jar ga > data/ga.txt
java -jar hom-generator.jar varex > data/varex.txt