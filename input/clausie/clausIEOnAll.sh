#!/bin/sh

for filename in data/*.txt; do
    NEWFILE="${filename##*/}"
    ./clausie.sh -vf "$filename" -o "tmp/${NEWFILE%.*}-out.txt"
done
