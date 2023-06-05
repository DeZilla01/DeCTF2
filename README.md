# DeCTF2
Rewrite of DeCTF based on the now defunct McCTF plugin from McPvP/Brawl

This project is being created 1 month after the full shutdown of McBrawl, thus rendering McCTF defunct. At the current moment, this is somewhat of a side project with no ETA to bring some life back to McPvP's legacy. Project is currently private to lower expectations. Reason of writing this based on DeCTF rather than McCTF code is to somewhat keep ownership and being able to freely release the plugin when ready.

![alt text](https://dezilla.net/stuff/2023-05-30_14.13.01.png)

The project is currently private to lower expectations from the community. It will be public when I judge it to be ready.

Current Progress:
CTF is now functional and working as intented \o/. Going to work on BaseKit fairly soon so I can work on combat. Looking at maybe using OldCombatMechanics plugin as depency.

Version:
This may be a difficult choice for some, but I decided to start this project on MC 1.19. This may be a controversial choice due to combat differences with 1.8, but the fact is 1.8 is now heavily outdated. At the moment I write this I haven't started work on kit/balance, but I plan to introduce 1.8ish combat as far as it can realistically be.

Teams:
I want the plugin to be as versatile as possible. So the plan that I have for now is to allow from 2 to 16 teams. Notably only 2 teams gamemodes is going to be used (mostly), but I'd like to keep options open.

Gamemodes:
- Capture the Flag (Based on McCTF)
- Zone Control (Based on the McPvP gamemode of the same name. Take control of control points to win)
- Team Deathmatch (Just kill the other team for points)

Following gamemodes are not a priority, but are planned:
- Arena (Based on the TF2 gamemode of the same name. No respawn, last team standing win)
- Delivery (A single flag/package appear. Deliver it to your capture zone to win)
- Payload (Based on the TF2 gamemode of the same name. Push a minecraft to the end to win)

Kits:
I have a fair amount of ideas on how I want to handle kits. I also plan to incorporate the idea of kit variation/modifiers where the same kits can switch between multiple abilities/items to create different experiences. More on the subject will be explained when I get to developing kits. Each kits will also have it's own configuration file so balance can be adjusted without having to recompile the plugin everytime.
I also plan to add a Kit API so new kits can be developed/added externally.

Kit List:
- Heavy
- Soldier
- Medic
- Archer
- Pyro
- Ninja

Following kits are not a priority, but are planned:
- Engineer (both old and new seperately, likely naming new Engineer "Demoman")
- Chemist
- Mage
- Dwarf
- Elf
- Scout
- Necro
- Angel
- Wraith
- Paladin
- Dragger
- Shade
- Weirdo
- Fashionista

(FYI, developing each kits alone will take considerable time. It will take a long while before ALL kits are developed)
