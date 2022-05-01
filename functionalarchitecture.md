From this [video](https://www.youtube.com/watch?v=9hAcW2oMl6M]
(nouns) -> (verb)
data -> Service
objects bundle data + service

Subtyping is the basis of OOP
classes are the basis of subtyping

to, OOP --> nouns are classes, verbs are classes
interfaces are the basis of abstraction

#OOP
* Every type is a class
* interfaces for abstraction
* Always bundles data and service

#FP
Functions are the basis of fp 
```f(x)```
Lambdas and immutable data are the basis of functions. 
* To FP, nouns are data, verbs are functions (they don't exist together they exist in different categories)
* In FP data is constructed using so called Algebraic Data Types ```sealed traits``` and ```case classes``` in Scala 3 ```enums``` and ```scale classes```
* functions are other things, we use functions to describe how to do these other things
* FP doesn't have any tools for abstraction. (Haskell is a subtype of FP)
---
#In FP 
* some types are data
* Some types are functions
* Never bundles data & service
# FP Challenges
```
data notification = Notification String (Maybe RichMessage)
notifyUser:: User -> UserNotification -> IO()
```
Subtyping is the foundation of Modularity
In Haskell you have functions and data

Architecture is primarily concerned with achieving a large-scale **Code Organization** that minimizes cost of maintenance.
Haskell has weak tools for code organization
***Weak Code Organization***

# Leveraging OOP
What Scala Gives
Functional aspects
* Data
* Purity
* Composition
OOP gives
* Methods
* Constructors
* Modules

#Use the right tool for the job
##Data Modeling
**Use the right tool for the job**

