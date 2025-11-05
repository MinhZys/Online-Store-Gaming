@echo off
echo Building Online Store Application...
echo.

echo Checking Java version:
java -version
echo.

echo Checking project structure:
if exist "online-store-war\web\WEB-INF\beans.xml" (
    echo ✓ beans.xml found
) else (
    echo ✗ beans.xml missing
)

if exist "online-store-war\web\WEB-INF\web.xml" (
    echo ✓ web.xml found
) else (
    echo ✗ web.xml missing
)

if exist "online-store-war\web\WEB-INF\faces-config.xml" (
    echo ✓ faces-config.xml found
) else (
    echo ✗ faces-config.xml missing
)

if exist "online-store-war\src\java\a23088\controller\LoginBean.java" (
    echo ✓ LoginBean.java found
) else (
    echo ✗ LoginBean.java missing
)

if exist "online-store-war\web\login.xhtml" (
    echo ✓ login.xhtml found
) else (
    echo ✗ login.xhtml missing
)

echo.
echo Project structure check completed.
echo.
echo To build and deploy:
echo 1. Open NetBeans IDE
echo 2. Open this project
echo 3. Right-click project -> Clean and Build
echo 4. Right-click project -> Run
echo.
pause
