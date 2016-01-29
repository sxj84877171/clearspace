REM build and generate all package 
REM get latest code from svn server
REM Sometimes you need clean gen/bin file in eclipse project
set app_dir=../cleanspace

REM generate update.xml
python config.py 1.4.70

pushd ..
pushd cleanspace
rem svn update 

rem ant clean

REM deploy
ant deploy
popd ..
popd

