@echo off
cd /d %~dp0

REM === Check if Java is installed ===
java -version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in PATH.
    echo Please install it from https://adoptium.net or https://www.java.com
    pause
    exit /b
)

REM === Check if Python is installed ===
python --version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Python is not installed or not in PATH.
    echo Please install it from https://www.python.org
    pause
    exit /b
)

echo  All dependencies found.
echo  Running Java pipeline...
java -jar java\imagej_pipeline.jar

pause
