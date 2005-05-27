@echo off

REM ################################################################################
REM # CruiseControl, a Continuous Integration Toolkit
REM # Copyright (c) 2001, ThoughtWorks, Inc.
REM # 651 W Washington Ave. Suite 600
REM # Chicago, IL 60661 USA
REM # All rights reserved.
REM #
REM # Redistribution and use in source and binary forms, with or without
REM # modification, are permitted provided that the following conditions
REM # are met:
REM #
REM #     + Redistributions of source code must retain the above copyright
REM #       notice, this list of conditions and the following disclaimer.
REM #
REM #     + Redistributions in binary form must reproduce the above
REM #       copyright notice, this list of conditions and the following
REM #       disclaimer in the documentation and/or other materials provided
REM #       with the distribution.
REM #
REM #     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
REM #       names of its contributors may be used to endorse or promote
REM #       products derived from this software without specific prior
REM #       written permission.
REM #
REM # THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
REM # "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
REM # LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
REM # A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
REM # CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
REM # EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
REM # PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
REM # PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
REM # LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
REM # NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
REM # SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
REM ################################################################################

REM Set this if you're using SSH-based CVS
REM set CVS_RSH=

REM The root of the CruiseControl directory.  The key requirement is that this is the parent
REM directory of CruiseControl's lib and dist directories.
REM By default assume they are using the batch file from the local directory.
REM Acknowledgments to Ant Project for this batch file incantation
REM %~dp0 is name of current script under NT
set CCDIR=%~dp0

set ANT_HOME=%CCDIR%apache-ant-1.6.3

:setClassPath
set CRUISE_PATH=

:checkJava
if not defined JAVA_HOME goto noJavaHome
set CRUISE_PATH=%JAVA_HOME%\lib\tools.jar
goto setCruise

:noJavaHome
echo Warning: You have not set the JAVA_HOME environment variable. Any tasks relying on the tools.jar file (such as <javac>) will not work properly.

:setCruise
set LIBDIR=%CCDIR%lib

set CRUISE_PATH=%CRUISE_PATH%;%LIBDIR%\cruisecontrol.jar;%LIBDIR%\log4j.jar;%LIBDIR%\jdom.jar;%LIBDIR%\ant.jar;%LIBDIR%\ant-launcher.jar;%LIBDIR%\jasper-compiler.jar;%LIBDIR%\jasper-runtime.jar;%LIBDIR%\xml-apis.jar;%LIBDIR%\xmlParserAPIs-2.5.jar;%LIBDIR%\xercesImpl.jar;%LIBDIR%\xalan.jar;%LIBDIR%\jakarta-oro-2.0.3.jar;%LIBDIR%\mail.jar;%LIBDIR%\activation.jar;%LIBDIR%\commons-net-1.1.0.jar;%LIBDIR%\starteam-sdk.jar;%LIBDIR%\mx4j.jar;%LIBDIR%\mx4j-tools.jar;%LIBDIR%\mx4j-remote.jar;%LIBDIR%\smack.jar;%LIBDIR%\comm.jar;%LIBDIR%\x10.jar;%LIBDIR%\javax.servlet.jar;%LIBDIR%\org.mortbay.jetty.jar;%LIBDIR%\commons-logging.jar;%LIBDIR%\commons-el.jar;.

set EXEC="%JAVA_HOME%\bin\java" -cp "%CRUISE_PATH%" -Djavax.management.builder.initial=mx4j.server.MX4JMBeanServerBuilder CruiseControlWithJetty %*
echo %EXEC%
%EXEC%
