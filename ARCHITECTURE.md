# DotOS Widget Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Flutter App (Dart)                       │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  lib/widgets.dart                                          │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │ │
│  │  │ ClockCard│  │BatteryCard│ │StorageCard│ │WeatherCard│  │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │ │
│  │       ↓              ↓              ↓              ↓       │ │
│  │  In-App Preview (Real-time Flutter Widgets)               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              ↓                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  lib/core/components.dart                                  │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  WidgetService                                        │ │ │
│  │  │  - addWidget(type: String)                           │ │ │
│  │  │  - Uses MethodChannel('widget_channel')              │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    MethodChannel Bridge
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Android Native (Kotlin)                       │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  MainActivity.kt                                           │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  MethodChannel Handler                               │ │ │
│  │  │  - Receives addWidget(type) calls                    │ │ │
│  │  │  - Maps type to widget provider class                │ │ │
│  │  │  - Calls requestPinWidget()                          │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              ↓                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Widget Providers (5 separate classes)                    │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  ClockWidgetProvider                                 │ │ │
│  │  │  - onUpdate() → renderClock()                        │ │ │
│  │  │  - Updates every 60 seconds                          │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  BatteryWidgetProvider                               │ │ │
│  │  │  - onUpdate() → renderBatteryWidget()                │ │ │
│  │  │  - Gets real battery data from BatteryManager        │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  StorageWidgetProvider                               │ │ │
│  │  │  - onUpdate() → renderStorageWidget()                │ │ │
│  │  │  - Gets real storage data from StatFs               │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  WeatherWidgetProvider                               │ │ │
│  │  │  - onUpdate() → renderWeatherWidget()                │ │ │
│  │  │  - Placeholder data (ready for API)                  │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  MusicWidgetProvider                                 │ │ │
│  │  │  - onUpdate() → renderMusicWidget()                  │ │ │
│  │  │  - Placeholder data (ready for MediaSession)         │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              ↓                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  NothingRenderer (Rendering Engine)                       │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  renderBatteryWidget(pct, isCharging)                │ │ │
│  │  │  - Draws background + dot grid                       │ │ │
│  │  │  - Draws "BATTERY" label                             │ │ │
│  │  │  - Draws percentage value                            │ │ │
│  │  │  - Draws battery icon with fill level                │ │ │
│  │  │  - Returns Bitmap (800x400px)                        │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  renderStorageWidget(gbUsed, gbTotal, percent)       │ │ │
│  │  │  - Draws background + dot grid                       │ │ │
│  │  │  - Draws "STORAGE" label + total                     │ │ │
│  │  │  - Draws used storage value                          │ │ │
│  │  │  - Draws progress bar                                │ │ │
│  │  │  - Returns Bitmap (800x400px)                        │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  renderWeatherWidget(temp, condition, location)      │ │ │
│  │  │  - Draws background + dot grid                       │ │ │
│  │  │  - Draws "WEATHER" label                             │ │ │
│  │  │  - Draws temperature + degree symbol                 │ │ │
│  │  │  - Draws cloud icon + location                       │ │ │
│  │  │  - Returns Bitmap (800x400px)                        │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  renderMusicWidget(track, artist)                    │ │ │
│  │  │  - Draws background + dot grid                       │ │ │
│  │  │  - Draws "NOW PLAYING" label                         │ │ │
│  │  │  - Draws album art placeholder                       │ │ │
│  │  │  - Draws track name (red accent)                     │ │ │
│  │  │  - Draws artist name                                 │ │ │
│  │  │  - Returns Bitmap (800x400px)                        │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Android Home Screen                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │  Clock   │  │ Battery  │  │ Storage  │  │ Weather  │        │
│  │  Widget  │  │  Widget  │  │  Widget  │  │  Widget  │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
│  (Bitmap)      (Bitmap)      (Bitmap)      (Bitmap)             │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

### Widget Addition Flow

```
User taps "Add Widget" in Flutter app
         ↓
WidgetService.addWidget("battery")
         ↓
MethodChannel sends message to Android
         ↓
MainActivity receives call
         ↓
Maps "battery" → BatteryWidgetProvider.class
         ↓
Calls AppWidgetManager.requestPinAppWidget()
         ↓
Android shows widget picker
         ↓
User places widget on home screen
         ↓
BatteryWidgetProvider.onUpdate() called
         ↓
Gets real battery data from BatteryManager
         ↓
Calls NothingRenderer.renderBatteryWidget()
         ↓
Creates 800x400px bitmap with Nothing design
         ↓
Sets bitmap to RemoteViews
         ↓
Widget appears on home screen
```

### Widget Update Flow

```
Timer triggers (based on updatePeriodMillis)
         ↓
Android calls WidgetProvider.onUpdate()
         ↓
Provider gets fresh data (battery, storage, time, etc.)
         ↓
Calls appropriate NothingRenderer function
         ↓
Renderer creates new bitmap with updated data
         ↓
Sets bitmap to RemoteViews
         ↓
AppWidgetManager.updateAppWidget()
         ↓
Widget refreshes on home screen
```

### Widget Click Flow

```
User taps widget on home screen
         ↓
PendingIntent fires
         ↓
Opens MainActivity (DotOS app)
         ↓
App launches to main screen
```

## Component Responsibilities

### Flutter Layer

**WidgetService**
- Provides API for adding widgets from Flutter
- Bridges to Android via MethodChannel
- Simple interface: `addWidget(type: String)`

**Widget Cards (ClockCard, BatteryCard, etc.)**
- In-app preview of widgets
- Real-time updates using Flutter state management
- Match Android widget design exactly
- Used in widget detail screens

### Android Layer

**MainActivity**
- Handles MethodChannel communication
- Maps widget types to provider classes
- Requests widget pinning via AppWidgetManager

**Widget Providers**
- Extend `AppWidgetProvider`
- Handle widget lifecycle (onUpdate, onEnabled, onDisabled)
- Fetch real system data
- Call renderer functions
- Update RemoteViews

**NothingRenderer**
- Pure rendering logic
- Creates bitmaps using Canvas API
- Implements Nothing Design System
- Widget-specific rendering functions
- No state, no data fetching

### Android System

**AppWidgetManager**
- System service for widget management
- Handles widget updates and lifecycle
- Manages widget IDs and instances

**RemoteViews**
- Describes widget UI for home screen
- Limited to specific view types
- We use ImageView with bitmap

## Widget Instance Management

### Problem (Before Fix)
```
User adds 3 Battery widgets
         ↓
All 3 call same renderWidget("BATTERY", "85%")
         ↓
All 3 look identical (same hardcoded value)
         ↓
No way to differentiate instances
```

### Solution (After Fix)
```
User adds 3 Battery widgets
         ↓
Each gets unique appWidgetId (e.g., 1, 2, 3)
         ↓
onUpdate() loops through all IDs
         ↓
for (appWidgetId in appWidgetIds) {
    updateBatteryWidget(context, appWidgetManager, appWidgetId)
}
         ↓
Each instance:
  - Gets same real battery data (85%)
  - Renders independently
  - Has own RemoteViews
  - Updates separately
         ↓
All 3 show same data but are separate instances
```

## Design System Integration

### Nothing Design Principles

```
┌─────────────────────────────────────────────────────────────┐
│  NothingRenderer.drawBackground()                           │
│  - OLED black (#111111)                                     │
│  - 48px corner radius                                       │
│  - Dot grid pattern (32px spacing, 40% alpha)               │
└─────────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────────┐
│  NothingRenderer.drawLabel()                                │
│  - Space Mono, 28sp                                         │
│  - #999999 (text-secondary)                                 │
│  - ALL CAPS                                                 │
│  - 0.08 letter spacing                                      │
└─────────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────────┐
│  Widget-specific rendering                                  │
│  - Primary value (Doto-style, 72-180sp, white)              │
│  - Secondary context (Mono, 24-28sp, #E8E8E8)               │
│  - Icons and visual elements                                │
│  - Matches Flutter design exactly                           │
└─────────────────────────────────────────────────────────────┘
```

## File Structure

```
DotOS/
├── lib/
│   ├── main.dart                    # App entry, BatteryCard, StorageCard, MusicCard
│   ├── widgets.dart                 # ClockCard, WeatherCard, CalendarCard
│   └── core/
│       ├── components.dart          # WidgetService, NothingCard, NothingButton
│       └── theme.dart               # Nothing Design System colors/fonts
│
├── android/
│   └── app/
│       └── src/
│           └── main/
│               ├── AndroidManifest.xml          # Widget provider registrations
│               ├── kotlin/com/example/dotos/
│               │   ├── MainActivity.kt          # MethodChannel handler
│               │   └── widgets/
│               │       ├── ClockWidgetProvider.kt      # Clock widget
│               │       └── OtherWidgetProviders.kt     # Battery, Storage, Weather, Music
│               │
│               └── res/
│                   ├── layout/
│                   │   └── widget_layout.xml    # Single ImageView for all widgets
│                   ├── values/
│                   │   └── strings.xml          # Widget descriptions
│                   └── xml/
│                       ├── widget_clock_provider.xml
│                       ├── widget_battery_provider.xml
│                       ├── widget_storage_provider.xml
│                       ├── widget_weather_provider.xml
│                       └── widget_music_provider.xml
│
└── Documentation/
    ├── QUICK_START.md               # Quick testing guide
    ├── WIDGET_FIXES_SUMMARY.md      # Technical breakdown
    ├── WIDGET_DESIGN_GUIDE.md       # Design system details
    └── ARCHITECTURE.md              # This file
```

## Key Design Decisions

### Why Bitmap Rendering?

**Pros:**
- Complete design control
- Matches Flutter design pixel-perfect
- No RemoteViews limitations
- Custom fonts and effects
- Nothing Design System compliance

**Cons:**
- Higher memory usage
- No native Android widget animations
- Requires manual text rendering

**Decision:** Bitmap rendering chosen for design consistency and Nothing aesthetic.

### Why Separate Renderer Functions?

**Before:** One `renderWidget(label, value)` for all widgets
**After:** Five specific functions (renderBatteryWidget, renderStorageWidget, etc.)

**Reasons:**
1. Each widget has unique layout requirements
2. Different data types (percentage, GB, temperature, track name)
3. Different visual elements (battery icon, progress bar, cloud, album art)
4. Easier to maintain and extend
5. Better matches Flutter component structure

### Why 800x400px Bitmap Size?

- 2:1 aspect ratio (standard widget proportion)
- High resolution for crisp rendering
- Scales well on different screen densities
- Matches typical widget sizes (250-300dp wide)
- Enough space for Nothing Design System spacing

## Performance Considerations

### Update Intervals

| Widget | Interval | Reason |
|--------|----------|--------|
| Clock | 60s | Balance between accuracy and battery |
| Battery | 60s | Battery changes slowly |
| Storage | 30min | Storage rarely changes |
| Weather | 30min | API rate limits |
| Music | 5s | Track changes need quick updates |

### Memory Usage

- Each widget bitmap: ~1.25 MB (800×400×4 bytes)
- 5 widgets: ~6.25 MB total
- Acceptable for modern Android devices
- Bitmaps recycled on update

### Battery Impact

- Minimal: widgets update on schedule
- No continuous polling
- Battery widget uses broadcast receiver (efficient)
- Storage widget uses cached StatFs data

## Future Enhancements

### Phase 1: Data Integration
- [ ] Weather API integration
- [ ] MediaSession for music widget
- [ ] Location services for weather
- [ ] Network state monitoring

### Phase 2: Customization
- [ ] Widget configuration activities
- [ ] User-selectable colors
- [ ] Custom refresh intervals
- [ ] Size variants (1x1, 2x1, 2x2, 4x2)

### Phase 3: Advanced Features
- [ ] Widget preview images
- [ ] Interactive elements (play/pause, refresh)
- [ ] Notification integration
- [ ] Widget groups/collections

---

**Architecture Status:** ✅ Implemented and documented  
**Design System:** Nothing-inspired  
**Platform:** Android (Flutter + Kotlin)
