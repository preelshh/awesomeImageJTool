#!/bin/bash

# === Change to the script's directory ===
cd "$(dirname "$0")"

# === Check if Java is installed ===
if ! command -v java &> /dev/null; then
    echo "Java is not installed or not in PATH."
    echo "Please install it from https://adoptium.net or https://www.java.com"
    read -p "Press Enter to exit..."
    exit 1
fi

# === Check if Python is installed ===
if ! command -v python &> /dev/null; then
    echo "Python is not installed or not in PATH."
    echo "Please install it from https://www.python.org"
    read -p "Press Enter to exit..."
    exit 1
fi

echo "All dependencies found."
echo "Running Java pipeline..."
java -jar java/imagej_pipeline.jar

# Pause before exit
read -p "Press Enter to exit..."
