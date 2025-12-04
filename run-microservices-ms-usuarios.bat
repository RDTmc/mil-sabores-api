@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Ruta del directorio donde está este .bat (raíz del proyecto)
set ROOT=%~dp0

echo ==========================================
echo  Lanzando microservicios Mil Sabores API
echo  Raiz: %ROOT%
echo ==========================================
echo.

REM 2) ms-usuarios (auth + JWT)
start "ms-usuarios" cmd /k "cd /d %ROOT%ms-usuarios && mvn spring-boot:run"

echo.
echo Se han abierto 4 ventanas CMD, cada una con un microservicio.
echo Para detenerlos, cierra cada ventana o haz Ctrl+C en ellas.
echo.

endlocal
pause
