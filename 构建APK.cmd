@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME if exist "D:\Android\AndroidStudio\jbr\bin\java.exe" set "JAVA_HOME=D:\Android\AndroidStudio\jbr"
if not exist local.properties if exist "D:\Android\sdk\platforms\android-35\android.jar" echo sdk.dir=D:/Android/sdk>local.properties
if not exist local.properties if exist "D:\Android\android-sdk\platforms\android-35\android.jar" echo sdk.dir=D:/Android/android-sdk>local.properties
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo 构建失败，请保留上面的报错信息。工程完整路径需使用英文。
) else (
    echo 构建成功：app\build\outputs\apk\debug\app-debug.apk
)
pause
endlocal
