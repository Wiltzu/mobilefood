mobilefood application UI
=====

[![Build Status](https://travis-ci.org/Wiltzu/mobilefood.png)](https://travis-ci.org/Wiltzu/mobilefood)
[![Build Status](https://floobits.com/Wiltzu/mobilefood.png)](https://floobits.com/Wiltzu/mobilefood/redirect)


## Setting up a environment

### Needed projects

* use https://github.com/mosabua/maven-android-sdk-deployer to install android version 4 and 7 compability libraries to your maven repo locally

### Installing software

Install Eclipse with ADT and m2Eclipse android plugin
Download Maven 3.1.1 version and unpack it to some directory (this is used in the environment variable)

### Set up some environment variables:

* Use ”WINDOWS + PAUSE” or right-click My Computer and then properties to open Windows System Info
  * Click Advanced System Settings → Environment variables…
* Add following User variables:
  * JAVA_HOME: Java JDK directory like “C:\Program Files\java\jdk1.7.0_15”
  * ANDROID_HOME: Android sdk directory like “C:\android-sdks”
  * M2_HOME = Maven installion directory like “C:\Program Files (x86)\apache-maven-3.0.5” (not bin!)

#### Add a couple of paths to WINDOWS PATH variable

* Add Maven PATH variable: %M2_HOME%\bin. 
  * Test that this works by running ”mvn” on command line and be sure that you have restarted command line after setting  up environment variables. __If it didn't work__, try to put the whole maven path like “C:\Program Files (x86)\apache-maven-3.0.5\bin” instead to PATH and try again (REMEMBER TO RESTART COMMAND LINE!).
  * Add two Android PATH variables: %ANDROID_HOME%\tools and %ANDROID_HOME%\platform-tools. These are needed to be able to run android build commands from command line.

### Before getting the project

Open your Android SDK installation directory and use the SDK Manager to download the newest build tools or otherwise generating android sources won't work. At the moment the version in use is 19. In addiction to this, be sure that you have android version 19 and 8 SDK platform's installed. **You should have those three tools installed!**
