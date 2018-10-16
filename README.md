# Team info

All members of the Students team are able to add/remove other members. Feel free to add missing members to the team. 

# Videos

See those talks preferably together in your team or with other programmers so that you can reflect upon the material together. 
Be prepared for each seminar to share your reflections. 

seminar 2:

* [Building Scalable, Highly Concurrent and Fault-Tolerant Systems: Lessons Learned by Jonas Bonér](https://youtu.be/DihXpZ9xR8E)
    * go concurrent
    * how to avoid mutable state (the root of all evil)
    * the problems with locks (is this what we have to deal with?)
    * go async (we have it naturally in Javascript - you can't block the thread)
    * reactive programming
    * how to recover from failure (do we have a way to find out?)

seminar 3:

* [Rich Hickey: The Value of Values](https://youtu.be/-6BsiVyC1kM)
    * information and facts (are mutable objects a good representations of these)
    * place oriented programming
    * do we still suffer from the history of hardware limitations?
    * The value propositions (independent of programming language)
    * How we communicate between systems
    * How to think about the past and make more powerful decisions. 

seminar 4:

* [Rich Hickey: Simple made Easy](https://www.infoq.com/presentations/Simple-Made-Easy)
    * Are we doing it easy for us as developers or do we make the programs simple.
    * Humans have a complexity limit. What can we do in order to handle more difficult requirements?
    * In order to change a program we must be able to reason about it.
    * Is it easy to change a program with lot of tests? (think about it)
    * TDD - is it "guard rail programming"
    * State is never simple - why?
    * Encapsulation of information - is it bad?
    * Simplicity as a choice - do you know when you choose complexity over simplicity?

seminar 5:

* [Douglas Crockford: Quality](https://youtu.be/t9YLtDJZtPY)
    * The software crisis - unsolved?
    * Computer Science has not taught us how to make software in the large effectively and reliable.
    * More difficult than we can make?
    * No metrics of quality and state of completeness.
    * Programmers optimization is where they think they spend their time. Where do we spend our time?
    * Programming is a social activity - the lone wolf is gone. Importance of a nice attitude?
    * How much do legacy affect us.
    * Management and manpower.
    * Agile constructions.
    * The codebase as one of the primary values of a company.
    * Causes of cruft to the codebase.
    * Defining the death march.
    * The 7th sprint without new features. Is it possible?
    
seminar 6:
    
* [Rich Hickey: Clojure made simple](https://youtu.be/VSdnJDO-xdg)
    * Facebooks ImmutableJS library. Why did they produce it?
    * What you can do in a language is not as important as what it makes practical and idiomatic.
    * Parts of your program manipulates information, the rest is plumbing or machinery.
    * DSLs are straightforward.
    * Death by specificity and glorified maps.
    * HttpServletRequest - we do not see the data. It is encapsulated and hidden.
    * How to avoid problems that the type checker and test can't catch.
    * Restriction to the growth of Clojure - Libraries instead.

seminar 7:

* [Joshua Bloch: How To Design A Good API and Why it Matters](https://youtu.be/aAb7hSCtvGw)
    * One chance to get it right.
    * Characteristics of good API.
    * Can we gather the requirements with a healthy degree of skepticism?
    * API first
    * Many clients to your API will improve it and make it more robust.
    * Single responsibility pattern.
    * When in doubt leave it out.
    * Implementaion details should not leak into the API.
    * Naming is a hard task, take a moment to think it over.
    * Classes should be immutable unless there's a good reason to do otherwise.
    
seminar 8:

* [Misko Hevery: Don't look for things!](https://youtu.be/RlfLCWKxHJ0)
* [Misko Hevery: Global state and singletons](https://youtu.be/-FRm3VPhseI)

seminar 9:

* [Alf Kristian Stoeyle: Clojure, Scala, and Java 8](https://youtu.be/1z_XhbIpm4Q)
* [Henrik Kniberg: Agile Product Ownership in a Nutshell](https://youtu.be/502ILHjX9EE)

For the swedish speaking programmers:
* [Adam Killander: Fundamentalistisk Förvaltning](https://youtu.be/mY-CNwT7fuI) 28:40 - 39:12

seminar 10:

* [Robert Smallshire: The Unreasonable Effectiveness of Dynamic Typing for Practical Programs](https://vimeo.com/74354480)

seminar 11:

* [Rich Hickey: Effective Programs](https://youtu.be/2V1FtfBDsLU)

seminar 12:

* [Rich Hickey: Reducers](https://vimeo.com/45561411)

seminar 13:

* [Rich Hickey: Simplicity Matters](https://youtu.be/rI8tNMsozo0)

seminar 14:

* [Derek Slager: ClojureScript for Skeptics](https://youtu.be/gsffg5xxFQI)



# firestone in Clojure

## Getting started with Cursive and Intellij

We recommend using Cursive and IntelliJ when developing Clojure. Both are freely available for students and non-commercial projects.

1. Install [IntelliJ](https://www.jetbrains.com/idea/download/)
1. Install [Cursive](https://cursive-ide.com/)
1. Make sure you have the code template downloaded on your computer.
1. In IntelliJ, Import project (from existing sources). Select your project root directory. Choose "Import project from external model" and select Leiningen. When prompted to choose a Project SDK, select Java 1.8 (or above can work).
1. Make sure you can start a REPL. To run a REPL, right click on the project (in the Project space usually to the left) and select the option "Run 'REPL for firestone'". You can also configure a Run task: Run -> Edit Configurations... -> + -> Clojure REPL -> Local.
1. Configure keyboard shortcuts. Under preferences -> Keymap, on the right side, Main menu -> Tools -> REPL: Create shortcut for "Run test in current NS" in REPL and "Send form before caret to REPL".

We recommend using the built-in format functionality in the editor. See https://www.jetbrains.com/help/idea/reformatting-source-code.html .

You might also want to disable "structured editing" and/or learn additional commands such as slurp and barf.

Sometimes it is nice to be able to reset the REPL (especially if you are working with multimethods). You can use the following commands in the REPL:

```clojure
(require 'clojure.tools.namespace.repl)
(clojure.tools.namespace.repl/refresh-all)
```

## Tips when designing applications in clojure

* Keep the state of the application in ONE map.
* The state should be information not decisions or the rules. Do not put functions in the state.
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

Observe that hero powers, spells and weapons are not included and that
Spell power doesn't mean anything at the moment.

#### Cards for the first sprint:
* Dalaran Mage (http://hearthstone.gamepedia.com/Dalaran_Mage)
* Defender (http://hearthstone.gamepedia.com/Defender)
* Imp (http://hearthstone.gamepedia.com/Imp)
* Ogre Magi (http://hearthstone.gamepedia.com/Ogre_Magi)
* War Golem (http://hearthstone.gamepedia.com/War_Golem)
