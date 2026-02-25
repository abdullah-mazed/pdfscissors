#!/usr/bin/env bash
set -e

LIB="lib/itextpdf-5.5.9.jar:lib/jgoodies-forms-1.10.jar:lib/jpedal-4.37b36.jar"
MAIN_CLASS="bd.amazed.pdfscissors.main.PdfscissorsMain"

echo "Compiling..."
mkdir -p bin
find src -name "*.java" > sources.txt
javac -cp "$LIB" -d bin @sources.txt
rm sources.txt

echo "Copying resources..."
cp -r res bin/

echo "Packaging JAR..."
mkdir -p dist
jar cfe dist/pdfscissors.jar "$MAIN_CLASS" -C bin .

echo "Build complete: dist/pdfscissors.jar"
