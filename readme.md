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

**Benefits of Functional Programming**
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

**ZIO the library**
* Asynchronous and Concurrent -> Fiber based model
* Resilient --> Leverages the power of scala's Type system!
* Efficient --> Apps that never leak resources
* Easy to understand and test --> Thanks to superior composability!

**ZIO the data type**
ZIO[-R, +E, +A]
* Core type of the ZIO library
* Functional effect

A good mental model is the following
R => Either[E, A]
This means that a ZIO effect:
* Needs an environment of type R to run
* It may fail with an error of type E
* Or, it may complete successfully, returning a value of type A

Common Aliases:
* Task[+A]          = ZIO[Any, Throwable, +A]
* UIO[+A]           = ZIO[Any, Nothing, +A]
* RIO[-R, +A]       = ZIO[-R, Throwable, +A]
* IO[+E, +A]        = ZIO[Any, E, A]
* URIO[-R, +A]      = ZIO[R, Nothing, A]

structuring code with Zio and Zlayers
https://www.youtube.com/watch?v=PaogLRrYo64

https://www.youtube.com/watch?v=6A1SA5Be9qw
ZIO is a library
ZIO is a zero dependency library for asynchronous and concurrent programming in Scala

# ZIO is Lazy
```
val sayHello: ZIO[Any, Nothing, Unit] = 
    ZIO.effectTotal(println("Hello World!"))
Runtime.unsafe(sayHello)

- ZIO is aggressively lazy
- sayHello is just a description of a computation
- Nothing is printed until we call unsafeRun
```


