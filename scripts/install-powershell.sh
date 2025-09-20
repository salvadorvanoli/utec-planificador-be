#!/bin/bash
###################################
# Instalar PowerShell en SO basados en Unix
# Extraido de: https://learn.microsoft.com/en-us/powershell/scripting/install/install-ubuntu?view=powershell-7.5

# Actualizar la lista de paquetes
sudo apt-get update

# Instalar paquetes requeridos.
sudo apt-get install -y wget apt-transport-https software-properties-common

# Obtener la version de Ubuntu
source /etc/os-release

# Descargar las claves del repositorio de Microsoft
wget -q https://packages.microsoft.com/config/ubuntu/$VERSION_ID/packages-microsoft-prod.deb

# Registrar las claves del repositorio de Microsoft
sudo dpkg -i packages-microsoft-prod.deb

# Eliminar el archivo de claves del repositorio de Microsoft
rm packages-microsoft-prod.deb

# Actualizar la lista de paquetes después de agregar packages.microsoft.com
sudo apt-get update

# Instalar PowerShell
sudo apt-get install -y powershell
