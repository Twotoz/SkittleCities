# SkittleCities

A comprehensive claim and city management plugin for Minecraft servers with advanced economy, lease systems, and world separation features.

## ✨ Features

### 🏘️ Claim System
- **Multiple Claim Types**
  - `FOR_SALE` - One-time purchase claims
  - `FOR_HIRE` - Time-based lease system with auto-renewal
  - `PRIVATE` - Admin-only protected areas
  - `SAFEZONE` - Protected zones with custom rules

- **Trust System** - Share your claims with other players
- **Flag System** - Customize claim behavior (PVP, block-break, entity-spawn, etc.)
- **Visual Selection** - WorldEdit-style selection tool with particle visualization

### 💰 Economy System
- **Integrated Economy** - Built-in currency system
- **Lease Management** - Automatic lease expiry and renewal
- **Balance Caching** - High-performance balance lookups
- **Admin Commands** - Full economy management (give/take/set)

### 🌍 World Separation
- **Separate Inventories** - City world has its own inventory system
- **Crash-Safe Storage** - Inventories saved to GZIP-compressed files
- **Automatic Switching** - Seamless inventory transitions between worlds
- **Command Restrictions** - Whitelist-based command blocking in city world

### ⚡ Performance
- **Highly Optimized**
  - Smart caching (balance, regions, messages)
  - Block-only movement detection
  - Folia-compatible threading
  - Efficient database operations

### 🛡️ Admin Tools
- **Debug Visualization** - Real-time particle display of all claims
- **Comprehensive GUI** - Manage all claims with pagination
- **Bypass Commands** - Ignore protections and command restrictions
- **Auto-Recovery** - Automatic sign recreation for missing claim signs

## 📦 Installation

1. Download the latest `SkittleCities.jar` from releases
2. Place in your `plugins/` folder
3. Restart the server
4. Configure `config.yml` to your needs
5. Set city spawn with `/setcityspawn`

## 🎮 Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/city` | Teleport to city spawn | `skittlecities.player` |
| `/cbal` | Check your balance | `skittlecities.player` |
| `/cclaims` | View and manage your claims | `skittlecities.player` |
| `/ctrust <player>` | Trust a player in your claim | `skittlecities.trust` |
| `/cuntrust <player>` | Remove trust from a player | `skittlecities.trust` |
| `/cautoextend` | Toggle auto-renewal for leased claims | `skittlecities.player` |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/ctool` | Get the region selection tool | `skittlecities.admin` |
| `/cregioncreate` | Create a region from selection | `skittlecities.admin` |
| `/cadmin` | Manage all claims (opens GUI) | `skittlecities.admin` |
| `/cflags` | Manage claim/world flags | `skittlecities.admin` |
| `/setcityspawn` | Set the city spawn point | `skittlecities.admin` |
| `/ceconomy <give\|take\|set> <player> <amount>` | Manage player economy | `skittlecities.admin` |
| `/cignoreclaims` | Toggle bypassing claim protection | `skittlecities.admin` |
| `/citycommandbypass` | Toggle command restrictions | `skittlecities.commandbypass` |
| `/cdebugtool` | Visualize claims with particles | `skittlecities.debug` |

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `skittlecities.admin` | Full admin access | OP |
| `skittlecities.player` | Basic player commands | Everyone |
| `skittlecities.trust` | Trust/untrust players | Everyone |
| `skittlecities.debug` | Debug visualization | Everyone |
| `skittlecities.commandbypass` | Bypass command restrictions | OP |

**Permission Hierarchy:**
- `skittlecities.admin` includes all other permissions

## ⚙️ Configuration

### Basic Configuration
```yaml
world-name: "City"  # The city world name

city-spawn:
  x: 0.0
  y: 64.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0

default-balance: 1000.0  # Starting balance for new players

selection-tool: WOODEN_AXE  # Item used for region selection
```

### Command Whitelist
```yaml
allowed-commands-in-city:
  - cbal
  - cclaims
  - ctrust
  - cuntrust
  - city
  - cautoextend
  # Add other allowed commands here
```

### Flag Configuration
```yaml
default-world-flags:
  pvp: true
  block-break: false
  block-place: false
  entity-spawn: true
  # More flags available...

default-claim-flags:
  pvp: false
  block-break: true  # Owners can build
  block-place: true
  # Flags inherit from world unless overridden
```

### Task Intervals
```yaml
lease-check-interval: 5      # Minutes between lease expiry checks
sign-check-interval: 30      # Seconds between sign recovery checks
```

## 🎨 Features in Detail

### Claim Creation Workflow
1. Get selection tool: `/ctool`
2. Left-click block for position 1
3. Right-click block for position 2
4. Run `/cregioncreate`
5. Configure claim type, price, and flags in GUI
6. Confirm to create

### Lease System
- Players can lease claims for a specified number of days
- Auto-renewal automatically extends leases if player has sufficient funds
- Expired leases return to FOR_HIRE status
- Original owner is notified via console logs

### World Separation
- Players entering the city world get their city inventory
- Players leaving the city world get their regular inventory
- Inventories are saved to disk (crash-safe)
- Format: `/plugins/SkittleCities/inventories/{uuid}_city.inv`

### Performance Optimizations
- **Balance Cache**: In-memory balance storage, updated on transactions
- **Region Cache**: Location-based cache with 5-second TTL
- **Message Cache**: Status bar messages cached per player
- **Movement Optimization**: Only triggers on block boundary crossings

## 🔧 Folia Compatibility

SkittleCities is fully compatible with:
- ✅ Spigot 1.21+
- ✅ Paper 1.21+
- ✅ Folia 1.21+ (multi-threaded)

All schedulers use backwards-compatible BukkitScheduler API that works on all platforms.

## 📊 Database

Uses SQLite for data persistence:
- **Location**: `/plugins/SkittleCities/database.db`
- **Tables**: regions, region_flags, region_trust, economy
- **Automatic Schema**: Created on first run
- **Migration Safe**: Compatible across plugin updates

## 🐛 Troubleshooting

### Signs not appearing
- Check `/cadmin` to recreate signs manually
- Signs auto-recover every 30 seconds (configurable)
- FOR_HIRE/FOR_SALE only - owned claims don't have signs

### Commands blocked in city
- Check `allowed-commands-in-city` in config
- Admins can use `/citycommandbypass` to bypass temporarily
- Commands work normally outside city world

### Performance issues
- Reduce `sign-check-interval` if needed
- Increase `lease-check-interval` for large servers
- Check cache hit rates in debug mode


