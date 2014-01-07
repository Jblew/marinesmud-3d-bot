marinesmud-3d-bot
=================

Efficient bot framework for marinesmud-3d. Connects to alternativ-mud, does not need unity.


Installation
============
This app does not need to be installed. See next paragraph.

Setup
============
There is still no configuration outside the code. If you want to change settings (hostname of server) or configure MOBs, please edit src/main/java/net/alternativmud/m3dbot/Config.java

To compile, use:
 mvn clean install

To start this app, use:
  mvn exec:java -Dexec.mainClass=net.alternativmud.m3dbot.App

To stop the app, please type 'stop'.
