# ServerRestart Plugin

A Spigot 1.21 plugin that provides server restart functionality with a countdown timer.

## Features

- **`/resta`** - Starts a 10-second countdown timer and restarts the server
- **`/resta stop`** - Cancels an active restart countdown
- Broadcasts countdown messages to all players
- Permission-based access control

## Installation

1. Locate the compiled JAR file in the `target` folder:
   ```
   target/ServerRestart-1.0.0.jar
   ```

2. Copy the JAR file to your Spigot server's `plugins` folder

3. Restart your server or load the plugin with a plugin manager

## Usage

### Getting Help
Execute the command:
```
/resta help
```

This displays all available commands and information about the plugin.

### Starting a Restart
Execute the command:
```
/resta
```

This will:
- Broadcast "**Server is restarting in 10 seconds!**" to all players
- Display a countdown every second (10, 9, 8, 7... 1)
- Restart the server after the countdown completes
- **Requires `serverrestart.use` permission**

### Cancelling a Restart
Execute the command:
```
/resta stop
```

This will cancel an active restart countdown and notify all players.
- **Requires `serverrestart.use` permission**

## Permissions

- **`serverrestart.use`** - Required to use both `/resta` and `/resta stop` commands
  - Default: OP only
  - Grant to specific players/groups using your permissions plugin

## Requirements

- Spigot 1.21 or higher
- Java 17 or higher

## Building from Source

This project uses Maven. To build:

```bash
./mvnw clean package
```

The compiled JAR will be in the `target` folder.

## Configuration

When you first run the plugin, it will automatically create a folder at `plugins/ServerRestart/` with a `config.yml` file inside.

### Config Options:

```yaml
# Countdown duration in seconds (default: 10)
countdown-seconds: 10

# Customize all messages with color codes
messages:
  restart-announcement: "&c&lServer is restarting in {time} seconds!"
  countdown: "&eServer restarting in &c{time} &esecond{s}..."
  restart-now: "&c&lServer is restarting now!"
  restart-cancelled: "&a&lServer restart has been cancelled!"
  # ... and more!
```

**Available placeholders:**
- `{time}` - Shows the countdown time or total seconds
- `{s}` - Shows 's' for plural or nothing for singular (e.g., "1 second" vs "2 seconds")

**Color codes:** `&a`=green, `&c`=red, `&e`=yellow, `&f`=white, `&6`=gold, `&7`=gray, `&l`=bold

After editing the config, reload your server or use a plugin manager to reload the plugin.

## Support

For issues or questions, please check your server console logs for any error messages.

## Version

Current version: **1.0.0**
