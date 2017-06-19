@ECHO off

IF [%1]==[] GOTO InvalidNumberArguments		REM TAG_NAME
IF [%2]==[] GOTO InvalidNumberArguments		REM Z3 FOLDER
IF [%3]==[] GOTO InvalidNumberArguments		REM CVC4 FOLDER
IF [%4]==[] GOTO InvalidNumberArguments		REM TORXAKIS_VERSION
IF not [%5]==[] GOTO InvalidNumberArguments		REM ONLY 4 arguments

SET ORIGINAL_PATH=%PATH%
SET ORIGINAL_LOC=%cd%

:CheckoutSpecifiedVersion
ECHO Checking out tagged version %1
REM Retrieved the tagged version from the repository
svn checkout %TORXAKIS_REPOSITORY%/tags/%1 %TORXAKIS_SANDBOX%/%1
ECHO Finished.

:CreateTorXakisVersionInfo
ECHO Creating TorXakis VersionInfo.hs file
SET VERSIONINFOFILE=%TORXAKIS_SANDBOX%/%1/torxakis/sys/core/src/VersionInfo.hs
@echo {- > %VERSIONINFOFILE%
type %TORXAKIS_SANDBOX%\%1\torxakis\copyright.txt >> %VERSIONINFOFILE%
@echo -} >> %VERSIONINFOFILE%
@echo module VersionInfo >> %VERSIONINFOFILE%
@echo where >> %VERSIONINFOFILE%
@echo version :: String >> %VERSIONINFOFILE%
@echo version = "%4" >> %VERSIONINFOFILE%
ECHO Finished.

:BuildTorXakis
ECHO Building Torxakis executable
CD %TORXAKIS_SANDBOX%\%1\torxakis
stack.exe clean
call build.bat
FOR /F "tokens=* USEBACKQ" %%F IN (`stack.exe path --local-install-root`) DO (
  copy %%F\bin\txsserver.exe %TORXAKIS_SANDBOX%\%1\torxakis\bin\txsserver.exe
  copy %%F\bin\txsui.exe %TORXAKIS_SANDBOX%\%1\torxakis\bin\txsui.exe
)

:CheckTorXakisBuild
IF NOT EXIST %TORXAKIS_SANDBOX%\%1\torxakis\bin\txsserver.exe GOTO TorXakisBuildFailure
IF NOT EXIST %TORXAKIS_SANDBOX%\%1\torxakis\bin\txsui.exe GOTO TorXakisBuildFailure
ECHO Finished.

:CreateInstallerWxs
ECHO Generating Wxs file.
CD %TORXAKIS_SANDBOX%\%1\WindowsInstaller
java -jar WxsGenerator.jar %1 %TORXAKIS_SANDBOX% %2 %3 %4
ECHO Finished.

:CompileWxs
ECHO Compiling Wxs File
candle TorXakis.wxs
ECHO Finished.

:LinkWxs
ECHO Linking Wxs File
light -ext WixUIExtension TorXakis.wixobj
ECHO Finished.


CD %ORIGINAL_LOC%

GOTO END

:InvalidNumberArguments
ECHO Usage:	TxsCreateInstaller TagName Z3Folder CVC4Folder TorXakisVersionNumber
GOTO END

:TorXakisBuildFailure
ECHO Build Failure 

:END