# SteveSus - A Minecraft Spigot mini-game.
A murder mystery mini-game based on the famous and trending impostor game.

# Known issues
-


# WiKi  

## How to disable a language  
Each language file contains an option called 'enable' on the first line. Just set it to false. This option is ignored if the language is set as server's default language.

## What's the multi-arena lobby protection coverage?  
List: blocks TNTs, blocks entity explosions, blocks entity spawn (only CUSTOM and SPAWNER_EGG spawn reason are allowed), disables block breacking and placing, disables block fading (ice melting etc.), disables fire interact, disables bucket fill and empty, disables breaking hanging entities (paintings etc.), disables entities from changing blocks (like players jumping on wheat), disables player food change level, disables pvp and other entity damage, blocks rain. 

## Available placeholders in stats GUIs  
- {first_play}, {last_play}, {games_played}

## Placeholders available almost everywhere:  
- {vault_prefix}, {vault_suffix}

## Placeholders that can be used in TeleporterGUI item commands or/ and names/ lores
- {player} holder display name, {player_raw} holder raw name, {player_uuid} holder uuid  
- And if it is a teleporter item: {target} display name, {target_raw} raw name, {target_uuid} target uuid.

## Placeholders that can be used in arena display item name and lore  
- {name} arena display name, {template} arena template name, {status} game state, {on} current players (spectators excluded),
 {max} player limit, {spectating} among of players spectating, {game_tag}, {game_id}
 
## Placeholders that can be used in scoreboard
- {name} arena display name, {template} arena template name, {status} game state, {on} current players (spectators excluded),
 {max} player limit, {spectating} among of players spectating, {game_tag}, {game_id}, {date}, {player}, {player_raw}, {server_name},
{countdown} for countdown (available at starting/ ending).

## Game countdown sound configuration  
- path for second 5 -> `count-down-tick-5: ENTITY_CHICKEN_EGG`.
- or configure a sound in a range. example from 5 to 1 `count-down-tick-from-5-to-1: ENTITY_CHICKEN_EGG`

# Developers  
This plugin has a really rich API. You can even create custom Arenas.

## Maven Repo  
repo here

### Other dependencies  
```xml
        <!-- DataBase API -->
        <dependency>
            <groupId>com.andrei1058.dbi</groupId>
            <artifactId>DataBaseInterface</artifactId>
            <version>[0.1-SNAPSHOT,)</version>
        </dependency>
```

## Getting Started  
How to get the API.

## How to create a custom SetupSession  
If you want to handle a setup session yourself, but you don't want to break anything please continue reading.  
But what is a [SetupSession](link-to-docs)? A setup session lets the plugin now a world is in use and prevents its usage by admins or other. Using a SetupSession for your custom setup will handle world load/ unload/ save for you using the current [WorldAdapter](link-to-docs). It will detect player-quit for you and will close the setup session and trigger a method so you can decide what to do .

add code example and how to register active sessions

# Custom configuration notes:  
- If you want custom messages for a template add in language file:
  - arena-display-item-waiting-name-[templateName]
  - arena-display-item-waiting-lore-[templateName]
  - arena-display-item-starting-name-[templateName]
  - arena-display-item-starting-lore-[templateName]
  - arena-display-item-playing-name-[templateName]
  - arena-display-item-playing-lore-[templateName]
  - arena-display-item--name-[templateName]
  - arena-display-item-ending-lore-[templateName]
  
 - Custom selector name:
   - arena-selector-gui-name-[nameHere]
   
 - Where to set selector replacement items name and lore?
   - selector-[selectorName]-replacement-[replacementCharacter]-name
   - selector-[selectorName]-replacement-[replacementCharacter]-lore
   
 - Custom stats gui name:
   - statsGUI-gui-name-[nameHere] accepted placeholders: {player} display name, {name} raw name
    
 - Where to set statsGUI replacement items name and lore?
   - statsGUI-[guiName]-replacement-[replacementCharacter]-name
   - statsGUI-[guiName]-replacement-[replacementCharacter]-lore
   
 - Custom teleporter gui name:
   - teeleporterGUI-gui-name-[nameHere] accepted placeholders: {player} display name, {name} raw name
    
 - Where to set statsGUI replacement items name and lore?
   - teeleporterGUI-[guiName]-replacement-[replacementCharacter]-name
   - teeleporterGUI-[guiName]-replacement-[replacementCharacter]-lore

- Custom count down title/ subtitles:  
You can add custom title and subtitle for certain seconds at language paths: `count-down-title-` and `count-down-subtitle-`. So a custom title for second 32 would be `count-down-title-32: "It is 32"`;

- Custom count down sounds per second:  
add a new path in sounds file for a single second `count-down-tick-5: ENTITY_CHICKEN_EGG, volume, pitch` or for a range of numbers `count-down-tick-from-20-to-10:`

## Common placeholders PAPI

player_count_global, spectator_count_global, online_count_global (Get amount of users playing or spectating from all arenas), arena_count_global, 
player_count_game_TAG (will return 0 if not found), spectator_count_game_TAG (will return 0 if not found), user_count_game_TAG, game_state_TAG
 (translated in player's language)
 PLACEHOLDERS TO BE RE-DOCUMENTED
 
## Custom scoreboard per template  
Just copy a scoreboard and add -tempalteName to its path.

## Custom scoreboard task string per template
Just copy "game-task-scoreboard-format" and add -templateName to its path.

## Scoreboard task string placeholders
{task_name}, {task_stage}, {stage_stages}

## 3rd party dependencies
- spigot-maps by JohnnyJayJay