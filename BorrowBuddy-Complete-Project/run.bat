@echo off
title BorrowBuddy - Campus Borrow & Exchange Management Portal
echo ======================================================================
echo   BorrowBuddy - Campus Borrow & Exchange Management Portal
echo ======================================================================
echo.

REM Check Java version
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\java.exe" (
        set "JAVA_EXE=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\java.exe"
    ) else (
        echo [ERROR] Java 17 or later was not found in PATH or standard directory.
        echo Please install Java 17 and try again.
        pause
        exit /b 1
    )
) else (
    set "JAVA_EXE=java"
)

echo [INFO] Using Java: %JAVA_EXE%
echo [INFO] Launching BorrowBuddy Desktop Application...
echo.

if exist "target\borrowbuddy-1.0.0-jar-with-dependencies.jar" (
    "%JAVA_EXE%" -jar "target\borrowbuddy-1.0.0-jar-with-dependencies.jar"
) else if exist "borrowbuddy-1.0.0-jar-with-dependencies.jar" (
    "%JAVA_EXE%" -jar "borrowbuddy-1.0.0-jar-with-dependencies.jar"
) else (
    echo [ERROR] Standalone JAR not found. Please build with 'mvn package' first.
    pause
    exit /b 1
)

pause
