# MorphoTool – ImageJ + Python ML Pipeline

## Overview
**MorphoTool** is an end-to-end pipeline for image analysis and classification.  
It combines:
1. **Java + ImageJ** – for image preprocessing, segmentation, and feature extraction.
2. **Python + scikit-learn** – for applying a trained machine learning model to classify features and generate final CSV reports.

This repository is structured for **portability** so that any future user can clone, set up, and run the pipeline with minimal effort.

---
**Steps to Run**

**WARNING**
Your computer needs the following languages downloaded: Python, Java
Also, you need the following python libraries. Please install the libraries after you have installed python with this line of code: **python -m pip install pandas numpy scikit-learn joblib**


1. Unzip the provided ZIP file into a folder on your computer.
   
2. Open the folder so you can see all its contents.

3. On Windows: Double-click run.bat
   
   On Mac: Open Terminal, navigate to the folder, and run ./run.sh
   1. Open Terminal (you can find it via Spotlight Search by pressing Cmd + Space and typing "Terminal").
   2. Navigate to the folder where you unzipped the tool:
   3. If the folder is in your Downloads: cd ~/Downloads/MorphoTool
        _If it’s somewhere else, you can drag the folder into the Terminal window after typing cd (with a space). This will auto-fill the path. Example:_
        cd /Users/yourusername/Documents/MorphoTool
   4. Make the script executable (only needs to be done once):chmod +x run.sh
   5. Run the script: ./run.sh

5. When prompted, choose Yes or No if you want to view the preprocessed ImageJ images on your computer.

6. Select the directory containing the images you want the tool to preprocess.

7. Choose where you want the output .csv file to be saved.

8. When finished, close all open windows.

---

## Requirements

### Java:
- Java 8+ (Java 17+ if upgrading)
- Gradle **not required** if using the included Gradle wrapper

### Python:
- Python 3.9+
- `pip` package manager

---

## Installation

1. **Clone the repository**
```bash
git clone git@github.com:<your-username>/MorphoTool.git
cd MorphoTool

---
## Quick Run:
# Windows
run.bat

# macOS/Linux
./run.sh
```
---
## File Outputs

Intermediate CSVs: from Java (ImageJ feature extraction)

Final CSV: from Python (ML classification results)

---
## Important Places in the Code

1. Fixing the watershed algorithm in main/java/src/main/java/Image/ImageBinaryWatershed.java within the function watershed
2. Cropping algorithim in main/java/src/main/java/Image/SolidImageCropper.java
3. Pixel to Centimeter Calibration in main/java/src/main/java/Analysis/RegionAnalyzer.java at lines 53-55



