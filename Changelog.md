# Changelog :D
## Version 1.0.0 ;D
This first version added the core system needed to start the plugin and the following commands too!
- ``/echesttools`` && `/inventorytools`: This one lets you choose between 3 different arguments, `clear`, `see` and 
`transfer` (arguments explained below).
- `/inventoryclear`: this one is a better version of the vanilla `/clear` command, letting you choose between clearing 
a player's enderchest with the `echest`, their inventory with the `inventory` arg or both with `all`.

### Arguments for /echesttools and /inventorytools
- `transfer`: Transfers giver's inventory/echest to recipient's one
````
Example:
    /echesttools transfer thegenathor17 nahum
````
In that case, thegenathor17 will receive nahum's echest.

- `clear`: Clear's the target's inventory/echest
````
Example:
    /inventorytools clear nahum
````
Nahum won't see his inventory again D:

- `see`: Shows the inventory/echest to the played that executed the command (must be a player, can't be done with the console)
````
Example:
    /inventorytools see nahum
````
You will be able to see nahum's inventory

## Version 1.0.1
This version added the offline function to echesttools, now you can use all arguments in echesttools to all players to have ever connected to your server instead of just the ones online. This was made by reading and modifying player data directly into the world's folder instead of relying on paper's tools.  
For the moment, the offlines options are unaviable for inventorytools and inventoryclear, i have to work on a centralized data manager for that so it isn't as chaotic as it is rn, see you next update ;D

## Version 1.1.0
Offline player support is finally universal, this version added not only offline support but also technical improvements to `EchestTools.java`, `InventoryTools.java` and `InventoryClear.java` (they got reorganized and refactored) and made some other minor technical changes such as `OfflinePlayerSync.java`, `NbtTags.java` and `FileManager.java` now storing different variables or containing certain reusable methods.
