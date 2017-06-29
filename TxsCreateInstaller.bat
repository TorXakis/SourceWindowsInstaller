@ECHO off

IF [%1]==[] GOTO InvalidNumberArguments		REM TORXAKIS_VERSION
IF [%2]==[] GOTO InvalidNumberArguments		REM Z3 FOLDER
IF [%3]==[] GOTO InvalidNumberArguments		REM CVC4 FOLDER

set TORXAKIS_VERSION=%1
set Z3_FOLDER=%2
set CVC4_FOLDER=%3
set NOCACHE=%4
IF [%4]==[] set NOCACHE=0

SET ORIGINAL_PATH=%PATH%
SET ORIGINAL_LOC=%cd%
set TAG_NAME=v%TORXAKIS_VERSION%

:RemoveExistingRepo
IF %NOCACHE%==1 IF exist %TAG_NAME%  (
	echo removing existing tag folder...
	rmdir %TAG_NAME% /s /q
	mkdir %TAG_NAME%
)

cd %TAG_NAME%

:CheckoutSpecifiedVersion
echo checking out tagged version %TAG_NAME%
IF NOT EXIST TorXakis (
	git clone https://github.com/torxakis/torxakis.git
)
cd TorXakis
git checkout tags/%TAG_NAME%
ECHO Finished.

:CreateTorXakisVersionInfo
ECHO Creating TorXakis VersionInfo.hs file
SET VERSIONINFOFILE=sys/core/src/VersionInfo.hs
ECHO at %VERSIONINFOFILE%
echo {- > %VERSIONINFOFILE%
type copyright.txt >> %VERSIONINFOFILE%
echo -} >> %VERSIONINFOFILE%
echo module VersionInfo >> %VERSIONINFOFILE%
echo where >> %VERSIONINFOFILE%
echo version :: String >> %VERSIONINFOFILE%
echo version = "%TORXAKIS_VERSION%" >> %VERSIONINFOFILE%
ECHO Finished.

:CheckoutPlugins
cd ..
echo Checking out plugins
IF NOT EXIST SupportEclipse (
	echo Checking out Eclipse plugin...
	git clone https://github.com/TorXakis/SupportEclipse.git
	cd SupportEclipse
	git checkout master
	cd ..
) ELSE (
	ECHO Using local SupportEclipse repo
)
IF NOT EXIST SupportNotepadPlusPlus (
	echo Checking out NPP plugin...
	git clone https://github.com/TorXakis/SupportNotepadPlusPlus.git
	cd SupportNotepadPlusPlus
	git checkout master
	cd ..
) ELSE (
	ECHO Using local SupportNotepadPlusPlus repo
)
ECHO Finished!

:BuildTorXakis
cd torxakis
ECHO Removing old server and ui binaries
del bin\txsserver.exe 2>nul
del bin\txsui.exe     2>nul

ECHO Creating BuildInfo.hs
copy %ORIGINAL_LOC%\buildinfo.bat .
call buildinfo.bat > sys/core/src/BuildInfo.hs
ECHO Building Torxakis executable
stack build

REM no easy way to assign a variable from command's result, so we do this
for /f "tokens=* usebackq" %%f in (`stack.exe path --local-install-root`) do (
	copy %%f\bin\txsserver.exe bin\txsserver.exe
	copy %%f\bin\txsui.exe bin\txsui.exe
)

:CheckTorXakisBuild
IF NOT EXIST bin\txsserver.exe GOTO TorXakisBuildFailure
IF NOT EXIST bin\txsui.exe GOTO TorXakisBuildFailure
cd %ORIGINAL_LOC%
ECHO Building TorXakis finished.

:EnsureWxsGenerator
IF %NOCACHE%==1 IF EXIST WxsGenerator.jar (
	rm WxsGenerator.jar
)
if not exist WxsGenerator.jar (
	ECHO Can't find WxsGenerator.jar - Building...
	call buildWxsGenerator.bat
)

:CreateInstallerWxs
ECHO Generating Wxs file.
IF EXIST %TAG_NAME%\WindowsInstaller (
	ECHO Removing old "%TAG_NAME%\WindowsInstaller" folder
	rmdir %TAG_NAME%\WindowsInstaller /s /q
)
java -jar WxsGenerator.jar %Z3_FOLDER% %CVC4_FOLDER% %TORXAKIS_VERSION% TorXakis SupportEclipse SupportNotepadPlusPlus
ECHO Finished.

:CopyInstallerImages
echo Copying installer images
copy *.bmp %TAG_NAME%\WindowsInstaller\
echo Done

:CompileWxs
ECHO Compiling Wxs File
candle -o %TAG_NAME%\WindowsInstaller\ %TAG_NAME%\WindowsInstaller\TorXakis.wxs 
ECHO Compiled.

:LinkWxs
ECHO Linking Wxs File
light -o %TAG_NAME%\WindowsInstaller\TorXakis.msi -ext WixUIExtension %TAG_NAME%\WindowsInstaller\TorXakis.wixobj
ECHO Linked.

CD %ORIGINAL_LOC%

GOTO END

:InvalidNumberArguments
ECHO Usage:	TxsCreateInstaller TorXakisVersionNumber Z3Folder CVC4Folder [NoCache]
GOTO END

:TorXakisBuildFailure
ECHO Build Failure 

:END