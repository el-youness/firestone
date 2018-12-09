# Firestone

## How to submit code

* Code must be tested (every function must have at least one test)
* Every function must have a doc string

### How to write a commit message

Use Fix #10 if the commit fixes issue 10. This will close the issue and add a reference to it.

All commit messages must follow the following four rules from [the seven rules of a great Git commit message](https://chris.beams.io/posts/git-commit/):

* Limit the subject line to 50 characters
* Capitalize the subject line
* Do not end the subject line with a period
* Use the imperative mood in the subject line

For example: "Remove deprecated methods Fix #15"

## Instructions

### Important before running a REPL
remove `:jvm-opts ["--add-modules" "java.xml.bind"]` in **project.clj** if you don't have Java 10.

### Structure of the code
This is the basic structure explained in the lectures:

* **construct.clj** has all the functions that will directly modify the attributes in the state without any verification.
* **core.clj** has all the functions that verify and take care of processes of the game behind the view.
* **api.clj** has all the functions that the player will concretely use.

In the **definition** package, there are all the files related to the heroes and minions definitions as well as the tests related to each one. 