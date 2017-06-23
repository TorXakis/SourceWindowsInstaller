@echo OFF


@echo {- 
type TorXakis\copyright.txt
@echo -}
echo module BuildInfo
echo where
echo buildTime :: String
echo buildTime = "%DATE% %TIME%"
REM echo svnVersion :: Integer

REM echo|set /p=svnVersion = 

REM Next line requires not only svn command line support, 
REM but also support for the --show-item flag. 
REM So at least svn version 1.9 must be installed.
REM svn info --show-item last-changed-revision https://esi-redmine.tno.nl/svn/torxakis/TorXakis/branches/Development
