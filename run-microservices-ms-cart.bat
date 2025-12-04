@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Ruta del directorio donde está este .bat (raíz del proyecto)
set ROOT=%~dp0

echo ==========================================
echo  Lanzando microservicios Mil Sabores API
echo  Raiz: %ROOT%
echo ==========================================
echo.

REM 3) ms-cart (carrito)
start "ms-cart" cmd /k "cd /d %ROOT%ms-cart && mvn spring-boot:run"

endlocal
pause
