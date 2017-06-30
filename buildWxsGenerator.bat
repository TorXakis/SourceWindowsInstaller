@echo OFF

cd nl.tno.torxakis.wxsgenerator\src
echo Building java classes...
javac .\nl\tno\torxakis\wxsgenerator\*.java
echo Done!
echo Building jar...
jar cmvf .\META-INF\MANIFEST.MF ..\..\WxsGenerator.jar .\nl\tno\torxakis\wxsgenerator\*.class
echo Done!
cd ..\..
