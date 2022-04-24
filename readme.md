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
```val sayHello: ZIO[Any, Nothing, Unit] = 
    ZIO.effectTotal(println("Hello World!"))
Runtime.unsafe(sayHello)
```
- ZIO is aggressively lazy
- sayHello is just a description of a computation
- Nothing is printed until we call unsafeRun

# ZIO is composable
```
val zioTweets:ZIO[Any, Throwable, List[String]] = ???
zioTweets.retry(Schedule.exponential(1.second))
```
-- Easy to combine effects to solve problems
-- Effects are descriptions so we can run them again
-- Can't do with Future

# ZIO preserves information
```
sealed trait ConfigError
final case class IOError(message: String)  extends ConfigError
final case class ParseError(message: String) extends ConfigError

def readFile(path: String): ZIO[Any, IOError, String] = ???
def parseConfig(s: String): ZIO[Any, ParseError, String] = ???

def readConfig(path: String): ZIO[Any, ConfigError, Config] = 
    readFile.flatMap(parseConfig)
val config: ZIO[Any, Nothing, Config] = 
    readConfig("config.json").orElseSucceed(defaultConfig)
```
-- We can express exactly how our effects can fail
-- Composed error types automatically inferred
-- We know whether our effects can fail at all

# ZIO is concurrent
```scala
trait ZIO[-R, +E, +A] {
  def fork: ZIO[R, Nothing, Fiber[E, A]]
}
trait Fiber[+E, +A] {
  def join: ZIO[Any, E, A]
}
```
-- Fiber based concurrency model
-- Have hundreds of thousands of fibers at a time
-- Semantically block but never block underlying threads

```scala
def getUserById(id: Int): ZIO[Any, Throwable, User] = ???

val getUsers: ZIO[Any, Throwable, List[User]] = 
  ZIO.foreachPar(ids)(getUserById)
```
-- Perform effects in parallel and collect the results
-- Automatically interrupt others if one fails
-- Control parallelism with foreachParN

```
val getDataFromEastCoast: ZIO[Any, Throwable, List[Result]] = ???
val getDataFromWestCoast: ZIO[Any, Throwable, List[Result]] = ???

val result: ZIO[Any, Throwable, List[Result]] = 
    getDataFromEastCoast.race(getDataFromWestCoast)
```
-- Return first effect to succeed
-- Automatically interrupt the loser
-- Race many effects with raceAll

# ZIO is Resource Safe
```scala
trait Fiber[+E, +A] {
  def interrupt: ZIO[Any, Nothing, Exit[E, A]]
}
```
-- Necessary for safe resource usage
-- Stop doing work if we don't need result anymore
-- Can't interrupt a Future without ZIO
```scala
trait ZIO[-R, +E, +A] {
  def ensuring[R1 <: R](finalizer: ZIO[R1, Nothing, A]): ZIO[R1, E, A]
}
```
-- The other necessary condition for safe resource usage
-- Finalizer will run no matter how effect terminates
-- Other combinators such as bracket built on top of this

```scala
val connection: ZManaged[Any, Throwable, Connection]
def postgres(connection: Connection): ZManaged[Any, Throwable, UserRepo] = ???

val userRepo: ZManaged[Any, Throwable, UserRepo] = connection.flatMap(postgres)
```
-- Resources guaranteed to be acquired in order
-- Resources guaranteed to be released in order
-- Combinators for parallelism and many other scenarios

# ZIO is Testable
```scala
trait ZIO[-R, +E, +A] {
  def provider(r: R): IO[E, A]
}

object ZIO {
  def environment[R]: ZIO[R, Nothing, R]
}
```
-- Express the services our application depends on
-- Add a dependency with ZIO#Environment
-- Eliminate a dependency with ZIO#provide







