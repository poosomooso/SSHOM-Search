#!/bin/bash

cp out/artifacts/hom_generator_jar/hom-generator.jar "$1"
cp -r lib "$1"
cp -r varex-hom-finder/resources/mutantgroups "$1"
cp run-benchmarks.sh "$1"
mkdir "$1"/data