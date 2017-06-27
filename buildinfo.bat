@echo OFF


@echo {- 
type copyright.txt
@echo -}
echo module BuildInfo
echo where
echo buildTime :: String
echo buildTime = "%DATE% %TIME%"
REM TODO: Add SHA1 of the git commit node
