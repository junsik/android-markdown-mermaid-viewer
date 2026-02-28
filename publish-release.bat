@echo off
echo ==============================================
echo Markdown Mermaid Viewer - Release ^& Publish
echo ==============================================

set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
echo [INFO] Using Java from: %JAVA_HOME%

echo [INFO] Starting build and publish process...
call gradlew.bat publishBundle --no-daemon --console=plain

if %ERRORLEVEL% equ 0 (
    echo.
    echo ==============================================
    echo [SUCCESS] Release successfully published to Google Play!
    echo ==============================================
) else (
    echo.
    echo ==============================================
    echo [ERROR] Publish failed. Please check the logs above.
    echo ==============================================
)
pause
