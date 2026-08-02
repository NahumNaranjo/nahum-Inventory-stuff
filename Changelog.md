# Changelog :D
## Version 1.0.0 SNAPSHOT ;D
This first snapshot added the core system needed to start the plugin and the following commands too!
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