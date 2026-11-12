# DOOM ARCADE — MASTER TECHNICAL, ARCHITECTURE, TESTING & RELEASE DOCUMENTATION

**Application Name:** Doom Arcade  
**Production Application ID:** `com.aistudio.doomarcade.fghtrx`  
**Version:** `1.0.0-release` (`versionCode = 8`)  
**Target SDK / Compile SDK:** `36` (`minSdk = 24`)  

---

## 1. Architecture & Module Ownership

- **`com.example.model.GameModels` / `GameCatalog`**:
  - Data-driven fighter definitions (**Kairo**, **Vex**, **Solara**, **Bront**, and unlockable **Nyx**) with distinct combat stats, reach, critical basis points, and unique Special (`300,000` energy) & Ultimate (`1,000,000` energy) `AbilityDefinition` profiles.
  - Data-driven arena definitions (**Neon District**, **Ember Temple**, **Frost Forge**, and unlockable **Sky Temple** requiring 3 Arcade Ladder wins).
- **`com.example.combat.CombatCalculator` & `CombatEngine`**:
  - Deterministic integer & basis-point (`10,000 bp = 100%`) combat math.
  - 11-state fighter state machine (`IDLE`, `MOVING`, `JUMPING`, `ATTACKING`, `BLOCKING`, `DODGING`, `TAKING_DAMAGE`, `SPECIAL_ATTACK`, `ULTIMATE_ATTACK`, `STUNNED`, `DEFEATED`, plus `VICTORY`).
  - Input buffering, startup/active/recovery frame enforcement, single-hit registration per `attackInstanceId`, and anti-infinite combo cap (`MAX_COMBO_HITS = 8`).
- **`com.example.ai.AiController`**:
  - Extensible CPU opponent controller supporting **Easy**, **Normal**, **Hard**, and **Expert** difficulties plus configurable **Practice Dummy** behaviors (`IDLE`, `BLOCK_ALL`, `DODGE_REACTION`, `COUNTER_ATTACK`, `AI_SPARRING`).
- **`com.example.managers.ProgressionCalculator` & `SaveManager`**:
  - Implements XP awards (`Win = 100 XP`, `Loss = 35 XP`), level progression (`requiredXP = currentLevel * 250`), versioned JSON schema (`v2`) with `v1 -> v2` migration, and automatic `.bak` backup recovery when primary save data is corrupted.
- **`com.example.managers.TournamentManager` & `ProceduralAudioEngine`**:
  - Single-elimination Tournament bracket progression (Semifinals → Grand Finals → Champion reward).
  - Real-time procedural `AudioTrack` PCM synthesizer for swings, impacts, blocks, dodges, specials, ultimates, and K.O. chimes.
- **`com.example.ui.screens.*`**:
  - Responsive Landscape & Portrait Jetpack Compose UI with hardware-accelerated 2.5D Canvas stage rendering, multi-touch arcade controls, Practice HUD, Settings, Profile, Fighter Codex, and QA Verification Matrix.
