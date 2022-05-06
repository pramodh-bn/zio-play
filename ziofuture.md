https://www.youtube.com/watch?v=u3pgyEiu9eU

#What ZIO
##CORE
Build Scalable, concurrent, non-blocking, resilient applications that never leak resources

## Streams
Build concurrent, streaming applications with back-pressuring and resource safety

## STM
Build non-blocking concurrent structures with transactional guarantees

## Testkit
Write fast, deterministic tests that do not require interacting with external systems

`Zio gives you superpowers, making you more productive and happier than ever before building complex modern applications.`

* Scalable
* Async
* Parallel
* Concurrent
* Leak-free
* efficient
* streaming
* stable 
* resilient

OS Threads -> JVM Threads
A Thread has
* Explicit Shutdown
* Preallocated Stack
* GC Root
* Context Switching 
* Maximum threads 10000

Imagine a Java Thread can be split into say further down into small subthreads called fibers. 
They are known as ZIO fibers. (before Loom's arrival)
Advantages of a ZIO fiber construct or abstraction is that
* Garbage collected
* Dynamic Stack
* No GC Root
* Less Context Switching
* Maximum 1,000,000 Fibers

Another advantage you never have to use callback again
```scala
// Legacy callback
s3Get(key,
  error => log(error),
  value => s3Put(key, enrichProfile(value)),
  error => log(error),
  _ => ())

// Zio Style, Asynchronous but with semantic blocking
val enrich =
  for {
    value <- s3Get(key)
    _     <- s3Put(key, enrichProfile(value))
  } yield ()
  
// Trivially parallelize with precise control:
ZIO.forEachParN(20)(urls) {
  url => for {
    data <- load(url)
    json <- parseToJson(data)
    transformed <- transform(json)
  } yield transformed
}

// Leverage non-blocking, blazing fast concurrent
// structures like Queue, Ref & more:
def startConsumers(n: Int, queue: Queue[Work]) = {
  val worker = queue.take.flatMap(dowork(_)).forever
  val workers = List.fill(n)(worker)
  
  ZIO.forkAll(workers)
}
#Concurrent
```
```scala
// Commit conditional transactions without locks
// or condition variables, free of race conditions
// and deadlocks.
def acquireConnection =
  STM.atomically {
    for {
      connection <- available.get.collect {
                        case head::Nil
                      }
      _          <- available.update(_.drop(1))
      _          <- used.update(connection :: _)
    } yield connection
  }
```

# Leak-free
```scala
// Package up acquire & release into a Managed
// resource, with no possibility of leaks:
val managedFile = Managed.make(open(file))(close(_))
managedFile.use {
  resource => (for{
    data <- read(resource)
    _    <- aggregateData(data)
  } yield ()).forever
}
```
# Efficient
```scala
// Race two effects, cancelling the loser:
val geoLookup = geoIpService.lookup(ipAddress)
val dbLookup = userRepo.getProfile(userId).map(_.location.toLatLong)

val fastest = geoLookup.race(dbLookup)

// Timeout slow effects, interrupting their execution:
slowDbQuery.timeout(60.seconds)
```



