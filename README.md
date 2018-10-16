# Team info

All members of the Students team are able to add/remove other members. Feel free to add missing members to the team. 

# Videos

Until meeting 2:

* [Douglas Crockford: Quality](https://youtu.be/t9YLtDJZtPY)

Until meeting 3:

* [Rich Hickey: Clojure made simple](https://youtu.be/VSdnJDO-xdg)

Until meeting 4:

* [Rich Hickey: Simple made Easy](https://www.infoq.com/presentations/Simple-Made-Easy)

Until meeting 5:

* [Alf Kristian Stoeyle: Clojure, Scala, and Java 8](https://youtu.be/1z_XhbIpm4Q)
* [Henrik Kniberg: Agile Product Ownership in a Nutshell](https://youtu.be/502ILHjX9EE)

For the swedish speaking programmers:
* [Adam Killander: Fundamentalistisk Förvaltning](https://youtu.be/mY-CNwT7fuI) 28:40 - 39:12

Until meeting 6:

* [Robert Smallshire: The Unreasonable Effectiveness of Dynamic Typing for Practical Programs](https://vimeo.com/74354480)

Until meeting 7:

* [Rich Hickey: The Value of Values](https://youtu.be/-6BsiVyC1kM)

Until meeting 8:

* [Neal Ford: Functional Thinking](https://www.youtube.com/watch?v=7aYS9PcAITQ)

Until meeting 9:

* [Misko Hevery: Don't look for things!](https://youtu.be/RlfLCWKxHJ0)
* [Misko Hevery: Global state and singletons](https://youtu.be/-FRm3VPhseI)

Until meeting 10:

* [Rich Hickey: Simplicity Matters](https://youtu.be/rI8tNMsozo0)

Until meeting 11:

* [Derek Slager: ClojureScript for Skeptics](https://youtu.be/gsffg5xxFQI)

Until meeting 12:

* [Rich Hickey: Reducers](https://vimeo.com/45561411)

Until meeting 14:

* [Rich Hickey: Effective Programs](https://youtu.be/2V1FtfBDsLU)

## Stefan Nilssons lecture

Video with Robert Griesemer:
https://www.youtube.com/watch?v=0ReKdcpNyQg
https://talks.golang.org/2015/gophercon-goevolution.slide#1

A general text about API-design:
https://github.com/yourbasic/api

Some examples:
http://programming.guide/go/inheritance-object-oriented.html
https://godoc.org/github.com/yourbasic/graph

About functional programmering in Go (advanced):
https://github.com/yourbasic/func


# firestone in Clojure

There are currently two popular Clojure development setups: Emacs + Cider and Intellij + Cursive. We recommend the IntelliJ setup, which we describe how to get started with below.

## Getting started with Cursive and Intellij

We recommend using Cursive and IntelliJ when developing Clojure. Both are freely available for students and non-commercial projects.

1. Install [IntelliJ](https://www.jetbrains.com/idea/download/)
1. Install [Cursive](https://cursive-ide.com/)
1. Make sure you have the code template downloaded on your computer.
1. In IntelliJ, Import project (from existing sources). Select your project root directory. Choose "Import project from external model" and select Leiningen. When prompted to choose a Project SDK, select Java 1.7 or above.
1. Make sure you can start a REPL. To run a REPL, right click on the project (in the Project space usually to the left) and select the option "Run 'REPL for firestone'". You can also configure a Run task: Run -> Edit Configurations... -> + -> Clojure REPL -> Local.
1. Configure keyboard shortcuts. Under preferences -> Keymap, on the right side, Main menu -> Tools -> REPL: Create shortcut for "Run test in current NS" in REPL and "Send form before caret to REPL".

We recommend using the built-in format functionality in the editor. See https://www.jetbrains.com/help/idea/reformatting-source-code.html .

You might also want to disable "structured editing" or learn additional commands such as slurp and barf.

Sometimes it is nice to be able to reset the REPL (especially if you are working with multimethods). You can use the following commands in the REPL:

```clojure
(require 'clojure.tools.namespace.repl)
(clojure.tools.namespace.repl/refresh-all)
```

## Designing applications in clojure

* Keep the state of the application in ONE map.
* The state should be information not the rules. Do not put functions in the state.
* Test basic functionality in the meta data of all functions. This is a good way of documenting the function.
* The functions should be free of side effects.
* Use the operators ->, ->> and as-> in order to make the code more readable.

# firestone-sprint-1

The first sprint of the game.

In this sprint we'll focus on getting the core of the game right.
Keep in mind that we will extend the game in the next sprints, so keep your code simple.

Please open GitHub issues in this repo if you have any issues with the code provided.

### Some notes

* This video instruction shows the basic rules of the game https://www.youtube.com/watch?v=TevkeE-Qy9Y
* When the game is started, the starting player gets three cards. The other players gets four cards. This process is called mulligan. Our requirements is simply to take `x` number of cards of the deck. Also, we do not have any coin card for the moment.
* Keep in mind that the player hero takes damage if the deck is empty and the player tries to draw a card. This is called fatigue. This can be useful to know while constructing tests.
* Create tests that assures that the functionality works.
* All functions should have tests unless there is nothing to test.

### Goals for the sprint 1

* Be able to create the state of the game.
* Be able to play minion cards.
* A minion should be able to attack another minion.
* A minion should be able to attack the other hero.
* A player should be able to end the turn.
* Hand/Deck mechanics such as drawing cards and fatigue. 
* All the functionality required for the minions listed below.

Observe that hero powers, spells and weapons are not included in sprint 1.

#### Cards for the first sprint:
* Imp (http://hearthstone.gamepedia.com/Imp)
* Chillwind Yeti (http://hearthstone.gamepedia.com/Chillwind_Yeti)
* Guardian of Kings (http://hearthstone.gamepedia.com/Guardian_of_Kings)
* Nightblade (http://hearthstone.gamepedia.com/Nightblade)
* Gnomish Inventor (http://hearthstone.gamepedia.com/Gnomish_Inventor)

### Goals for sprint 2

* Be able to play weapon cards.
* Be able to play spell cards.
* Effects should trigger in order of play.
* Battlecries only trigger when played, not on summoned.
* Aura buffs introduced.

Observe that hero powers are not included in sprint 1-2.

#### Cards for the second sprint:
* Fiery War Axe (http://hearthstone.gamepedia.com/Fiery_War_Axe)
* Friendly Bartender (http://hearthstone.gamepedia.com/Friendly_Bartender)
* Earthen Ring Farseer (http://hearthstone.gamepedia.com/Earthen_Ring_Farseer)
* Y'Shaarj, Rage Unbound (http://hearthstone.gamepedia.com/Y'Shaarj,_Rage_Unbound)
* Ravenholdt Assassin (http://hearthstone.gamepedia.com/Ravenholdt_Assassin)
* Sylvanas Windrunner (http://hearthstone.gamepedia.com/Sylvanas_Windrunner)
* Sneed's Old Shredder (http://hearthstone.gamepedia.com/Sneed's_Old_Shredder)
* Acolyte of Pain (http://hearthstone.gamepedia.com/Acolyte_of_Pain)
* Shieldmaiden (http://hearthstone.gamepedia.com/Shieldmaiden)
* Ancient Watcher (http://hearthstone.gamepedia.com/Ancient_Watcher)
* Armorsmith (http://hearthstone.gamepedia.com/Armorsmith)
* Tundra Rhino (http://hearthstone.gamepedia.com/Tundra_Rhino)
* Savannah Highmane (http://hearthstone.gamepedia.com/Savannah_Highmane)
* Shield Block (http://hearthstone.gamepedia.com/Shield_Block)
* Knife Juggler (http://hearthstone.gamepedia.com/Knife_Juggler)
* Mana Tide Totem (http://hearthstone.gamepedia.com/Mana_Tide_Totem)
* Fire Elemental (http://hearthstone.gamepedia.com/Fire_Elemental)
* Shadow Word: Pain (http://hearthstone.gamepedia.com/Shadow_Word:_Pain)

### Goals for sprint 3

* Be able to play the game in the view.

#### Cards for the third sprint:
* Ironbeak Owl (http://hearthstone.gamepedia.com/Ironbeak_Owl)
* Young Priestess (http://hearthstone.gamepedia.com/Young_Priestess)
* Unearthed Raptor (http://hearthstone.gamepedia.com/Unearthed_Raptor)
* Acidic Swamp Ooze (http://hearthstone.gamepedia.com/Acidic_Swamp_Ooze)
* Toxic Sewer Ooze (http://hearthstone.gamepedia.com/Toxic_Sewer_Ooze)
* Blood Imp (http://hearthstone.gamepedia.com/Blood_Imp)
* Gruul (http://hearthstone.gamepedia.com/Gruul)
* Shattered Sun Cleric (http://hearthstone.gamepedia.com/Shattered_Sun_Cleric)
* Crystalline Oracle (http://hearthstone.gamepedia.com/Crystalline_Oracle)
* Barnes (http://hearthstone.gamepedia.com/Barnes)
* Silithid Swarmer (http://hearthstone.gamepedia.com/Silithid_Swarmer)
* Gnomeferatu (http://hearthstone.gamepedia.com/Gnomeferatu)
* Bloodsail Corsair (http://hearthstone.gamepedia.com/Bloodsail_Corsair)
* Patches the Pirate (http://hearthstone.gamepedia.com/Patches_the_Pirate)
* Southsea Deckhand (http://hearthstone.gamepedia.com/Southsea_Deckhand)
* Luckydo Buccaneer (http://hearthstone.gamepedia.com/Luckydo_Buccaneer)
* Bloodsail Raider (http://hearthstone.gamepedia.com/Bloodsail_Raider)
* Golakka Crawler (http://hearthstone.gamepedia.com/Golakka_Crawler)
* Truesilver Champion (http://hearthstone.gamepedia.com/Truesilver_Champion)

### Goals for sprint 4

We need you to provide a report regarding the quality of the implementation given to you (it will be used as feedback 
for the other team). It should be about five pages. In order to get familiar with the new code base you will
implement the following new cards:

* N'Zoth's First Mate (http://hearthstone.gamepedia.com/N'Zoth's_First_Mate)
* Mass Dispel (http://hearthstone.gamepedia.com/Mass_Dispel)
* Blackguard (http://hearthstone.gamepedia.com/Blackguard)
* Gluttonous Ooze (http://hearthstone.gamepedia.com/Gluttonous_Ooze)
* Southsea Squidface (http://hearthstone.gamepedia.com/Southsea_Squidface)
* Blackwater Pirate (http://hearthstone.gamepedia.com/Blackwater_Pirate)
* Naga Corsair (http://hearthstone.gamepedia.com/Naga_Corsair)
* Captain Greenskin (http://hearthstone.gamepedia.com/Captain_Greenskin)
* Upgrade! (http://hearthstone.gamepedia.com/Upgrade!)

We would like you to test the game and make sure that it works with the view as expected. Focus on the design choices 
made. 
Here are some examples of interesting aspects to consider:
Is the logic tested? Are the cards tested? Is there any logic misplaced? Is the state well modelled. Do functions 
have well defined responsibilities? Is there any duplication of code? How well did you understand the design from reading the SAD (software architecture design) document?

We are thinking of buying this code base for half a million. Should we? Explain why.

#### Course evaluation and common reflections

Provide an individual course evaluation and send your thoughts to lwiener@kth.se and tomase@kth.se. It should discuss 
the following topics

* What do you think about the course?
* During this course, to what extent have you acquired useful methods/knowledge/theory to approach a large code base?
* How can we achieve better discussions about the videos?
* How did you like the Firestone project?
* How was the collaboration in the group?
* What was your preferred programming language before this course?
* How familiar were you with the concepts of functional programming before this course? Separation of data and logic? 
Avoiding mutation and not creating any intermediate steps?
* After taking this course, do you think that you have better knowledge in evaluating tools, like programming languages 
and dependencies?
* When you make changes to your code (in this course), how confident are you that it works afterwards?
* (Starting from the second sprint.) How do you like the LISP syntax?
* How hard was it for you to learn Clojure, given that you didn't already know it?
* Any additional comments?

#### Sharing the repositories

Make the person under your team a collaborator of your repository so that they can add the rest of their team. From now on the original team is prohibited to commit to the repository. If you are not getting access then remind the owner of the repository.

jonatber/firestone
kashmirk/Firestone-AEK
jstuart/DD2487
emiper/dd2487
przybysz/DD2487_Project
kwap/DD2487
engeli/storutv
svebrant/lads
joohls/clojurestone
jakobiv/firestone
bogaeus/stony-hearth
rohin/HearthStoneAintDoTA
wbolin/firestone
fij/DD2487-Storutv
lartigau/Firestone
schwerm/DD2487-firestone
robertwb/storutv
raksanyi/storutv
danekl/Storutv17-Lab
jonatber/firestone

Create a new branch from the version 3.0.x. Call it sprint4. Do your changes in this branch. Add the report with the 
name feedback.pdf to the branch and tag the code with version 4.0.0 when you are done.

In the report we would like the names of all persons in your team and your own repository name, i.e. username/reponame.

If you don't get access before the lecture on the 11th of December, do some actions about it.

### Goals for the optional sprint 5 (no time limit)

Write sprint 1-4 in another language. We are very interested in a future lunch for discussions about your solution.