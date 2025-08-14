# MorphoTool – ImageJ + Python ML Pipeline

## Overview
**MorphoTool** is an end-to-end pipeline for image analysis and classification.  
It combines:
1. **Java + ImageJ** – for image preprocessing, segmentation, and feature extraction.
2. **Python + scikit-learn** – for applying a trained machine learning model to classify features and generate final CSV reports.

This repository is structured for **portability** so that any future user can clone, set up, and run the pipeline with minimal effort.

---


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

---
## File Outputs

Intermediate CSVs: from Java (ImageJ feature extraction)

Final CSV: from Python (ML classification results)

---
## Important Places in the Code

1. Fixing the watershed algorithm in main/java/src/main/java/Image/ImageBinaryWatershed.java within the function watershed
2. Cropping algorithim in main/java/src/main/java/Image/SolidImageCropper.java
3. Pixel to Centimeter Calibration in main/java/src/main/java/Analysis/RegionAnalyzer.java at lines 53-55
