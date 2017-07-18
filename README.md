# YouTube Gaming Mighty Bot

A modular YouTube Gaming bot. Currently has three modules:

* Saves the current time in a file on your computer (usable in OBS).
* Saves your sub count in a file on your computer (usable in OBS).
* Announces new subs in chat using your account with customizable random announcements.

## Usage

Download the latest release here: https://github.com/AllInOneMighty/ytg-mighty-bot/releases

Unzip the file anywhere on your computer, then edit the `mighty.properties` file and follow the instructions inside. You **need to enable each module** by changing the corresponding module line from:

```
<module>.enable = false
```

to:

```
<module>.enable = true
```

Each module also has its set of properties that you want to modify as well.

Finally, double-click on the jar file.

## Troubleshooting

### The bot just doesn't launch

You need Java to run the bot, since it's a Jar file. Download Java here: http://www.oracle.com/technetwork/java/javase/downloads/index.html

Most users need the JRE. If you want to help developing the bot, you need the JDK.

### The bot window opens and immediately closes.

That is very likely because your properties file is incorrect. In that case, the bot displays an error message and immediately closes. In order to display the error, start a command line, navigate to the directory where the bot is, then launch the bot from there.

Example on Windows:
```cmd
D:
cd "path/where/the/bot/is"
java -jar mighty-bot-x.x.x-SNAPSHOT.jar
```

### I'm on Windows and I want to prevent the window from closing if an error occurs

To do that, create a Windows shortcut with this target:

```C:\Windows\System32\cmd.exe /K C:\ProgramData\Oracle\Java\javapath\java.exe -jar "C:\path\to\the\bot\mighty-bot-x.x.x-SNAPSHOT.jar"```

Also set the *Start in* folder to: ```"C:\path\to\the\bot"```.

### I deleted my mighty.properties file. I need a new one.

You can find the default mighty.properties file here: https://github.com/AllInOneMighty/ytg-mighty-bot/blob/master/src/main/defaults/mighty.properties
