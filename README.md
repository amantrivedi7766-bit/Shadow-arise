# SoloArise Plugin

A Minecraft Spigot/Paper plugin based on Solo Leveling's Arise power system.

## Features

- ✦ Unique task system for each new player (60+ tasks)
- ⏰ 1-hour time limit for tasks
- 👻 Soul capture system with 3 attempts per mob
- 📊 Soul ranking system (Normal, Warrior, Elite, BOSS, Player)
- ⚡ Soul energy system
- 🔄 Soul work commands
- ⚔️ Soul attack system
- 📋 Scoreboard panel showing soul collection
- ✨ Particle effects for all actions
- 💾 SQLite/MySQL database support
- ⚙️ Fully configurable

## Commands

| Command | Description |
|---------|-------------|
| `/arise` | Unlock arise power or capture souls |
| `/arisework <soul> <order>` | Command souls to work |
| `/soulrelease <soul/group>` | Release captured souls |
| `/soulattack <player>` | Attack another player with souls |
| `/soulcome` | Recall all souls to shadow state |
| `/soultask <soul/group> <task>` | Assign tasks to souls |

## Permissions

- `solarise.use` - Allows use of all Arise commands (default: true)

## Configuration

Edit `config.yml` to customize:
- Capture chances
- Soul energy costs
- Max souls per player
- Particle effects
- Database settings

## Installation

1. Download the plugin JAR
2. Place in your server's `plugins` folder
3. Restart or reload the server
4. Configure `plugins/SoloArise/config.yml` if needed

## Requirements

- Spigot/Paper 1.21 - 1.21.11
- Java 21 or higher

## Support

For issues or suggestions, please contact the plugin author.
