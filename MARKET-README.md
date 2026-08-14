# nahum-Inventory-Stuff — Quick Overview for Marketplaces

Make server moderation and inventory management painless. `nahum-Inventory-Stuff` is a lightweight plugin that gives staff the essential tools to inspect, transfer and clean player inventories and Ender Chests — safely and quickly.

## Key Features

- View player inventories and Ender Chests in a read-only viewer
- Transfer inventories / Ender Chests between players
- Clear inventory, Ender Chest, or both (online & offline support)
- Fast, minimal overhead; designed for production servers
- Permission-based access for fine-grained staff controls

## Why Install

- Quick moderation workflows: inspect suspected accounts without risk
- Prepare players for events by transferring or clearing storage
- Manage offline player data reliably

## Installation (Server Admins)

1. Download the plugin JAR from the project's releases.
2. Place the JAR in your server `plugins/` folder.
3. Restart the server.

No additional configuration is required for basic use.

## Basic Commands (examples)

- `/inventorytools see <player>` — View a player's inventory
- `/inventorytools transfer <recipient> <giver>` — Transfer inventory
- `/inventorytools clear <player>` — Clear player's inventory
- `/echesttools see <player>` — View a player's Ender Chest
- `/inventoryclean all <player>` — Clear inventory and Ender Chest

See `plugin.yml` for the full command list and permission nodes.

## Permissions

- `nahum.inventorytools` — Access inventory commands
- `nahum.echesttools` — Access Ender Chest commands
- `nahum.inventoryclean` — Access clearing commands

## Support & Notes

- Tested on Paper 1.21.x; compatible with Paper/Spigot forks.
- If you find a bug, please include server version, plugin version, and relevant logs when opening an issue.

---

Short, reliable, and focused for staff workflows — perfect for moderation, events, and server management.
