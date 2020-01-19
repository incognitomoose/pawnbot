# Pawnbot
Pawnbot is a Discord bot that personifies Pornhub's commenters.

## Description
Pawnbot is written in [Kotlin](https://kotlinlang.org/) and is using [Spring Boot](https://spring.io/projects/spring-boot), [JDA](https://github.com/DV8FromTheWorld/JDA) and [LavaPlayer](https://github.com/sedmelluq/lavaplayer), among other third party libraries.

### Background
Pawnbot was originally written for a closed social group based on a random idea about having a bot react to media and links in an NSFW channel with random Pornhub video comments. Features have been gradually added since then.

### Features
- Reacts to media, links and mentions with random Pornhub video comments
- Configurable ratio of straight to gay videos
- Censorship of text using replacement phrases
- Per-server configuration of features
- Voice chat support using [Google Cloud Text-to-speech](https://cloud.google.com/text-to-speech/)
- File-based H2 database for comments and persistent server state
- Asynchronously updated precache of Pornhub comments to limit load on Pornhub as much as possible

## Installation

### Requirements
- A Java JRE >= version 11
- A dedicated Discord bot user
- (For TTS / Voice chat support) A Google Cloud account with access to the Google Cloud Text-to-speech API

### Creating and inviting a Discord bot user
- Create an Application for the bot in the [Discord Developer Portal](https://discordapp.com/developers/applications/).
- Set the name as you want it. The other settings don't really matter for now.
- Click the "Bot" tab on the left
- Click "Add bot" on the page that shows up.
- Set the bot's username and icon as desired. You might also want to disable "Public bot" to prevent anyone but you to add the bot to their server.
- Copy the token using the "Copy" button, and keep it somewhere safe. You'll need this later. Don't share this token with anyone.
- Click the "OAuth2" tab on the left.
- Under "Scopes", select "bot"
- In the "Bot Permissions" tab that shows up, select "Send Messages" under "Text permissions". If you want voice chat support, also select "Connect" and "Speak" under "Voice permissions"
- Click the "Copy" button next to the URL right above "Bot Permissions". This URL allows a user to invite the bot to their server.
- Open a new tab in your browser and go to the URL to add the bot to your server.

### Setting up for Google Cloud Text-to-speech
*Note: This is only required if you want TTS support.*
Follow the "Before you begin" steps in [Google's quickstart guide](https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries) until you get a json file for authenticating. Store it somewhere safe.

### Running the bot
Download the pawnbot jar file from the [latest release](https://github.com/incognitomoose/pawnbot/releases/latest).

Set the following environment variables in a command line window or shell:
- `DISCORD_BOT_TOKEN` to your Discord Bot user's token
- *(Only required for TTS support)* `GOOGLE_APPLICATION_CREDENTIALS` to the location path of your Google Cloud authentication json file

Then simply run the jar file you downloaded using java.  
Example if named pawnbot-1.2.3.jar:
```bash
java -jar pawnbot-1.2.3.jar
```

## Usage
With the bot online in a channel, type `!pbhelp` to get started. All setup is done through Discord commands.

## Contributing
Feel free to add any issues on GitHub for bug fixes or feature requests.

## Authors and acknowledgment
Written by incognitomoose, which is totally my only GitHub account and not at all a pseudonym to prevent this from showing up to future employers.  
Based on an idea by Goochie.

## License
[GNU GPLv3](https://choosealicense.com/licenses/gpl-3.0/)
