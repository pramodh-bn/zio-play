Going through Scala With Zio: Introduction to Fibers
https://www.youtube.com/watch?v=0OTYAVWUnU4

Functional Programming 101 with Scala and ZIO
https://www.youtube.com/watch?v=gZMwjYTkK9k

Functional Programming basic concepts
ZIO Basic concepts

What is Functional programming?
A paradigm, where programs are composition of pure functions

What is a pure function?
A pure function must be total, deterministic and depends on inputs only, no side effects

Benefits of Functional Programming
* Local Reasoning
* Referential Transparency --> Fearless Refactoring
* Conciseness --> Fewer bugs
* Easier to test
* Applications behave more predictably
* Allows us to write correct parallel programs

But Side effects are going to be always there
The trick Don't write functions that interact with the outside world. 
Write functions that describe interactions instead
* Descriptions of interactions with the outside world
* Immutable values that can serve as inputs and outputs of pure functions
* They are executed only at the end of the world
* 