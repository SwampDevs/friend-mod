# Friend Mod 🤝

A Fabric mod for Minecraft 1.21.8 that adds a friend system with death notifications and coordinate sharing.

## Features

- `/friend add <name>` — Add a player to your friends list
- `/friend remove <name>` — Remove a player from your friends list
- **Death notifications** — Your friends get notified with coordinates when you die
- **`(c)` in chat** — Broadcasts your current coordinates to everyone

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.8
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `friend-mod-x.x.x.jar` into your `mods/` folder

## Building from source

```bash
git clone https://github.com/YOUR_USERNAME/friend-mod.git
cd friend-mod
./gradlew build
# JAR will be in build/libs/
```

## Automatic builds

Every push to `main` triggers a GitHub Actions build.  
Download the latest JAR from the **Actions** tab → select the latest run → **Artifacts**.

To create a release with the JAR attached, push a tag:
```bash
git tag v1.0.0
git push origin v1.0.0
```

## Friend data

Friends are stored per-player in `.minecraft/config/friendmod/<uuid>.json`.

## License

MIT
