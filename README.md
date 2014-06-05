ScalaQuery_Nested
================
[![Build Status](https://travis-ci.org/youdevise/scalaquery_nested.png)](https://travis-ci.org/youdevise/scalaquery_nested)

Summary
-------
Safely manage nested sessions & transactions in [Slick](https://github.com/slick/slick).

Details
-------

After the first few weeks of using Slick (formerly ScalaQuery), our team
at [TIM Group](http://www.timgroup.com) was surprised to diagnose bugs caused
by nested calls to the Slick methods `Database#withSession` and `Database#withTransaction`.

It turns out that, when you nest these calls, a different database session will be created in
each scope. This can lead to race conditions. For example:

```scala
database.withSession {
  // insert object A in database table here
  database.withSession {
    // query returning object A here
    // --> race condition! may not see same A in different db connection
  }
}
```

This mini-library enriches `Database` with methods `#withNestedSession` and `#withNestedTransaction`,
which behave in the way that we'd originally assumed it would work:

```scala
import com.timgroup.scalaquery_nested.NestedScalaQuerySessionsAndTransactions._

database.withNestedSession {
  // insert object A in database table here
  database.withNestedSession {
    // query returning object A here
    // --> no problem, same db connection used
  }
}
```

Caveat! Just like the Slick methods it is replacing, this implementation is ultimately based
on storing the database session in a Java ThreadLocal. This means the following code may return
unexpected results:

```scala
database.withNestedSession {
  // insert object A in database table here
  val future = database.withNestedSession {
    Future {
      // query returning object A here 
      // --> in a future which may be run in a different thread,
      //     different database connection might be used, leading to unexpected results
    }
  }
  Await.result(future, timeout)
}
```

Usage
-----

To pull in the implicit conversion to add these two methods, you can extend the trait `NestedScalaQuerySessionsAndTransactions`:

```scala
import com.timgroup.scalaquery_nested.NestedScalaQuerySessionsAndTransactions

class Cars extends Table[Car]("cars") with NestedScalaQuerySessionsAndTransactions {
  database withNestedTransaction { /* can nest sessions and transactions... */ }    
}
```

Alternatively, you can also import directly from the object into any scope:

```scala
// inside some scope in which a ScalaQuery database is defined...
import com.timgroup.scalaquery_nested.NestedScalaQuerySessionsAndTransactions._

database withNestedTransaction { /* can nest sessions and transactions... */ }

```

How to Add as a Dependency
--------------------------
You can get scalaquery_nested with the following line in your `build.sbt`:

```scala
libraryDependencies += "com.timgroup" %% "scalaquery_nested" % "1.1.0-M1"
```

Compatibility
-------------
As of version [v1.0.0](https://github.com/youdevise/scalaquery_nested/blob/v1.0.0/.travis.yml),
we intransitively compile against ScalaQuery 0.10.0-M1 (the last pre-slick release), and our tests
pass on all Scala versions since 2.9.0-1 through 2.10.0.

Contributing
------------
Please feel free to send pull requests. Any change to code must come with tests, of course.
