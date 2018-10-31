# Course Information 

## The git repository for your team

Create a repository for each team and invite tomase as a collaborator to the repository. You are not allowed to use repositories outside
of KTH-github. 

## How to demo

When done with a sprint, tag the code with a version x.0.0, where x is the number of the sprint, and send
an email to tomase@kth.se notifying me that you are done with the sprint.

## Videos

See those talks preferably together in your team or with other programmers so that you can reflect upon the material together. 
Be prepared for each seminar to share your reflections. 
Note that all these videos are subjective.
Reflect on your own and together with your friends.

**seminar 2:**
[concurrency, distributed systems, locks, reactive systems]
* [Jonas Bonér: Building Scalable, Highly Concurrent and Fault-Tolerant Systems: Lessons Learned](https://youtu.be/DihXpZ9xR8E)
    * go concurrent
    * how to avoid mutable state (the root of all evil)
    * the problems with locks (is this what we have to deal with?)
    * go async (we have it naturally in Javascript - you can't block the thread)
    * reactive programming
    * how to recover from failure (do we have a way to find out?)

**seminar 3:**
[immutability, locks, information, facts]
* [Rich Hickey: The Value of Values](https://youtu.be/-6BsiVyC1kM)
    * information and facts (are mutable objects a good representations of these)
    * place oriented programming
    * do we still suffer from the history of hardware limitations?
    * The value propositions (independent of programming language)
    * How we communicate between systems
    * How to think about the past and make more powerful decisions. 

**seminar 4:**
[focus on programs vs programmers, easiness (and complexity) over simple solutions, we can choose]
* [Rich Hickey: Simple made Easy](https://www.infoq.com/presentations/Simple-Made-Easy)
    * Are we doing it easy for us as developers or do we make the programs simple.
    * Humans have a complexity limit. What can we do in order to handle more difficult requirements?
    * In order to change a program we must be able to reason about it.
    * Is it easy to change a program with lot of tests? (think about it)
    * TDD - is it "guard rail programming"
    * State is never simple - why?
    * Encapsulation of information - is it bad?
    * Simplicity as a choice - do you know when you choose complexity over simplicity?

**seminar 5:**
[software crises, about the whole process of making software, quality, the death march]
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
    
**seminar 6:**
[why he thinks that clojure is a simpler alternative, explaining why he thinks that OOP is hard and complex]
* [Rich Hickey: Clojure made simple](https://youtu.be/VSdnJDO-xdg)
    * Facebooks ImmutableJS library. Why did they produce it?
    * What you can do in a language is not as important as what it makes practical and idiomatic.
    * Parts of your program manipulates information, the rest is plumbing or machinery.
    * DSLs are straightforward.
    * Death by specificity and glorified maps.
    * HttpServletRequest - we do not see the data. It is encapsulated and hidden.
    * How to avoid problems that the type checker and test can't catch.
    * Restriction to the growth of Clojure - Libraries instead.

**seminar 7:**
[this is mostly about OOP, focuses on APIs, still very relevant for functional programming]
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
    
**seminar 8:**
[how to test in OOP, splitting your application in factories and business logic]
* [Misko Hevery: Don't look for things!](https://youtu.be/RlfLCWKxHJ0)
    * Hard to instantiate objects.
    * Doing logic in the constructor.
    * Mixing construction and logic.
    * Dependency injection.
    * Only ask for objects that you directly need.
    * Almost no new operators in the business logic.

[how to test in OOP, problems with global state and static methods and mutability]    
* [Misko Hevery: Global state and singletons](https://youtu.be/-FRm3VPhseI)
    * insanity: repeating the same thing and expecting a different result.
    * non-explicit connection to global state.
    * Good vs bad singletons - just create it once.
    * Dependency injection.
    * Listen carefully to the questions at the end.

**seminar 9:**
[explaining his opinion on Java 8]
* [Alf Kristian Stoeyle: Clojure, Scala, and Java 8](https://youtu.be/1z_XhbIpm4Q)
    * Experienced Scala developers will not use the mutable parts - everywhere?
    * Streams in Java lazy in a language based on side-effects - problems?
    * To easy to do mutable stuff in Scala
    * Legacy is more a problem for Java

[very elegant presentation of agile]    
* [Henrik Kniberg: Agile Product Ownership in a Nutshell](https://youtu.be/502ILHjX9EE)
    * Product owner, stakeholders and a team
    * Features to build
    * Backlog
    * Iterations
    * WIP-limit
    * History will determine the speed
    * The product owner will have to priorities - everything is important is not an option...

[very good presentation of a better view on maintenance]
For the swedish speaking programmers:
* [Adam Killander: Fundamentalistisk Förvaltning](https://youtu.be/mY-CNwT7fuI) 28:40 - 39:12
    * How to think about projects and maintenance - is there a difference?

**seminar 10:**
[about type systems]
* [Robert Smallshire: The Unreasonable Effectiveness of Dynamic Typing for Practical Programs](https://vimeo.com/74354480)
    * Type systems and their qualities
    * The effectiveness of types
    * Types and tests
    * The economics of types
    * What problems do dynamically typed programs have?

**seminar 11:**
[about programs/systems that deals with ordinary information]
* [Rich Hickey: Effective Programs](https://youtu.be/2V1FtfBDsLU)
    * Entangled systems
    * Irregularities of data and/or behaviour
    * The problems of programming
    * Place oriented programming
    * Classes and information

**seminar 12:**
[what is an event, code examples of actors]
* [Jonas Bonér: How Events Are Reshaping Modern Systems](https://youtu.be/3V3pHm2Cpks)
    * Why we should care about event driven design
    * multicore, distributed systems, data-centric applications
    * events, acts, immutability
    * event loops
    * streaming, uture and promises
    * eventual consistency
    * commands vs events

**seminar 13:**
[clojurescript as an alternative to javascript for the browser, frontend development]dadasdfdffffffffffffffff
* [Derek Slager: ClojureScript for Skeptics](https://youtu.be/gsffg5xxFQI)
    * clojurescript in browsers
    * javascript production code size
    * immutability to the rescue
    * google closure compiler
    
**seminar 14:**
[Another version of simple made easy, but with other words and examples]
* [Rich Hickey: Simplicity Matters](https://youtu.be/rI8tNMsozo0)
    * Simplicity is prerequisite for reliability
    * Changing a system
    * Architectural agility
    * Design is about taking things apart
    * Programmers knows the tradeoffs of nothing
    * Who wants to be in Foo Fighters
    * Elephant in the corner will dominate what you can do
    * Example: List and order

[the database as a value, another way of thinking about history, code examples of using the database]
* [Rich Hickey: The Functional Database](https://www.infoq.com/presentations/datomic-functional-database)
    * datoms and datalog
    * an aggregating entity database
    * live REPL coding

## Deadlines

### Sprint 1 
11 November

### Sprint 2
25 November

### Sprint 3
9 December

### Sprint 4
21 December


## firestone in Clojure

### Getting started with Cursive and Intellij

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

### Tips when designing applications in clojure

* Keep the state of the application in ONE map.
* The state should be information not decisions or the rules. Do not put functions in the state.
* Test basic functionality in the meta data of all functions. This is a good way of documenting the function.
* The functions should be free of side effects.
* Use the operators ->, ->> and as-> in order to make the code more readable.

## firestone-sprint-1

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
* Demo the sprint by showing that you have relevant tests of the above functionality that passes.

### Goals

* Be able to create the state of the game.
* Be able to play minion cards.
* A minion should be able to attack another minion.
* A minion should be able to attack the other hero.
* A player should be able to end the turn.
* Hand/Deck mechanics such as drawing cards and fatigue. 
* All the functionality required for the minions listed below.

Observe that hero powers, spells and weapons are not included and that
At the moment "Spell power" does not mean anything.

### Cards to implement:
* Dalaran Mage (http://hearthstone.gamepedia.com/Dalaran_Mage)
* Defender (http://hearthstone.gamepedia.com/Defender)
* Imp (http://hearthstone.gamepedia.com/Imp)
* Ogre Magi (http://hearthstone.gamepedia.com/Ogre_Magi)
* War Golem (http://hearthstone.gamepedia.com/War_Golem)
