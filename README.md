Enigma
======

Enigma is a Java Console Library written by Ethan Royael Nicholas.
I discovered it on SourceForge but looked long dead, so I've revived it on Github using Eclipse.
BTW the link http://www.ethannicholas.com/enigma/ is dead and domain ethannicholas.com has a squatter.

I may or may not continue using it as it's GPL.



*Enigma Console API*
*Copyright (C) 2002 Ethan Royael Nicholas*
                    

About the Enigma Console API
----------------------------

Enigma is a Java-based operating system which is currently in the very 
early stages of development.  The Enigma Console API is the first
component of Enigma to be completed.  It is a full-featured text-mode 
API for Java which can be used on any platform, with any program.  It 
will ultimately serve as Enigma's text-mode API, but will remain pure 
Java and therefore be compatible with all Java-capable platforms.  
Please see http://www.ethannicholas.com/enigma/ for more information 
about the status and goals of the Enigma project.



Version
-------

This is version beta-0.2 of the Enigma Console API, dated 09/12/2002.
Please see http://www.ethannicholas.com/enigma/ to find the latest
release.



Changes
-------

Changes since beta-0.1:

- Due to overwhelming demand, the Console API now runs under Java 1.3.  
  The rest of Enigma, including the command-line shell, will continue to
  require Java 1.4 or higher.

- ANSI support is now available.  Type "ansi on" from the command-line
  shell to enable it, or check out the source for 
  enigma.shells.commandline.commands.Ansi to see an example of how to 
  enable and disable it in your own code.
  
- Remote console support has been added.  This support is currently 
  incomplete, poorly documented, and does not provide support for
  TextWindow, but you may find it useful regardless.  Support will be
  much better in beta-0.3.
  
- Numerous bugs fixed.


Contents
--------

This archive contains the following files:

README                          the file you are reading now
LICENSE                         the terms of licensing this software
lib/enigma-all_en_US.jar        all Enigma classes, localized for English
lib/enigma-console.jar          the Enigma Console API
lib/enigma-shell.jar            the Enigma Command-Line Shell
lib/enigma-i18n.jar             English localization data
enigma-src.jar                  source code
docs/*                          documentation



Licensing
---------

Enigma is copyrighted software licensed under the terms of the General 
Public License (GPL), a copy of which is contained in the file LICENSE.  
Please see http://www.gnu.org/copyleft/gpl.html for more information.



Getting Started
---------------

To get a quick demonstration of the Enigma Console API, run:

java -jar lib/enigma-shell.jar

substituting whatever path is appropriate for the lib/enigma-shell.jar
file.  This will launch a preview version of the Enigma Command-Line
Shell, which is in the early stages of development but serves as a
quick demo.


To run an arbitrary Java class in an Enigma console window, run:

java -classpath lib/enigma-console.jar enigma.loaders.ConsoleProxyLoader "Window Title" classname

You will need to substitute the appropriate path to 
lib/enigma-console.jar and add to the classpath as necessary to run 
the class you specify.  The class will be displayed in an Enigma
console window, but naturally will not take advantage of any of the 
Enigma console's extra features unless it is modified to use the 
Enigma Console API directly.


To modify a program to start its own console window:

Add the following import statements to your class:

import enigma.framework.*;
import enigma.text.*;

In your program's main() method (or other appropriate location),
add the line:

Console console = Enigma.getConsole("Window Title");

This will request a console window, and since your program is not 
already running in one, it will cause a new console window to be 
created.  You may then interact with the Console object to take 
advantage of the extra features of the Enigma Console API.



Building the Source Code
------------------------

You must have a copy of Ant installed in order to build Enigma.  Ant
may be downloaded from http://jakarta.apache.org/ant/.  Enigma has 
only been tested with Ant 1.5.

The Enigma source code is contained in enigma-src.zip, located in the 
root of the installation directory.  It must be unzipped in the root 
installation directory using a command such as "jar xvf enigma-src.jar".  
Once you have unzipped the source, ensure that Ant's bin directory is 
in your PATH and JAVA_HOME is set to the root of your Java 
installation.  Then you may build Enigma by running one of the 
following commands:

  ant          builds the Enigma JAR files in build/lib
  ant javadoc  builds the javadocs in build/docs/javadoc
  ant dist     builds the final distribution in the build directory
  ant clean    deletes the build directory



Comments / Feedback
-------------------

Please send all comments and feedback to ethan@ethannicholas.com.
