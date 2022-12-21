# McDiscordLink

## What is McDiscordLink?
McDiscordLink is a plugin for Minecraft that allows players to seamlessly connect their in-game chat with a Discord channel. 
With this plugin installed, all messages sent in Minecraft will be automatically forwarded to the designated Discord channel, 
allowing players to communicate with one another in real-time regardless of whether they are in-game or on Discord. 
In addition, all messages sent in the Discord channel will also be displayed in the in-game chat, giving players the option to 
communicate with others using their preferred platform.

This plugin can be especially useful for players who are part of a large server or community and want to keep in 
touch with one another outside of the game. It allows players to stay connected and communicate with each other in a 
more convenient and efficient way, without the need to constantly switch between different apps or platforms.

Overall, McDiscordLink is a useful tool for Minecraft players who want to stay connected with their friends and 
fellow players both in-game and on Discord. It can help to create a more cohesive and engaging community, and make it easier 
for players to communicate and collaborate with one another.

## How do you set it up?
Well, first you need the plugin. So go get that and put it on your server.

1. Go to https://discord.com/developers/applications and create a new application.
2. Under the "oAuth2" tab, click on "URL Generator" and select the "bot" scope. Choose the permissions you want to give the bot and click "copy".
3. Go to the "bot" tab and add a new bot to your application.
4. Enable the ALL settings under "Privileged Gateway Intents" in the bot settings. Without all of hem your bot will not start.
5. Paste the URL you copied earlier and use it to authorize the bot to join your server.
6. Reset the bot token and copy the new token.
7. In the McDiscordLink config file, enter the bot token in the "botToken" field.
8. Enable Developer Mode in Discord under "APP SETTINGS" and "Advanced".
9. Right-click on the Discord channel you want to link with Minecraft and copy the ID.
10. In the McDiscordLink config file, enter the Discord channel ID in the "channelId" field.
11. In the config file, enter the desired status for the bot in the designated field. You can also set the status in-game by running the "/status" command followed 
by the desired status.

## Help ERRORS!
There is a good chance you will get errors looking like:
>Error occurred while disabling McDiscordLink v1.0 (Is it up to date?)
java.util.concurrent.RejectedExecutionException: The Requester has been stopped! No new requests can be requested!
	at net.dv8tion.jda.internal.requests.Requester.request(Requester.java:109) ~[mcdiscordlink.jar:?]
	at net.dv8tion.jda.internal.requests.RestActionImpl.queue(RestActionImpl.java:200) ~[mcdiscordlink.jar:?]
  
 This means you have not set the config.yml file properly. That or you reloaded the server. DON'T RELOAD THE SERVER.
 To fix this have a look at the config.yml file provided [here](https://github.com/MJDaws0n/mcDiscordLink/blob/master/src/main/resources/config.yml).
 Also note this will probably only work on paper and not spigot or bukkit due the to advancemnts system being different.
 
## Reloading
 You better not reload your server. The plugin will try to restart the server if it thinks it is beaing reloaded however if there are to many changes it can't and
 will freeze. The only way to fix it is to kill your server. I mean, I warned you.
 
 
## config.yml
You can find the default config.yml [here](https://github.com/MJDaws0n/mcDiscordLink/blob/master/src/main/resources/config.yml).
The botToken is the bot token 🤯. To get that follow the How do you set it up? above. The same can we said about the channelId and for the botStatus you can set
this to what you want. You bot will say playing, then the botStatus. Note if it's blank you WILL get errors.

