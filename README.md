ScalaQuery_Nested
================
[![Build Status](https://travis-ci.org/youdevise/scalaquery_nested.png)](https://travis-ci.org/youdevise/scalaquery_nested)

Summary
-------
Safely manage nested sessions & transactions in [ScalaQuery 0.10.0-M1](https://github.com/slick/slick/tree/0.10.0-M1).

Details
-------

We recently discovered that we had incorrect code due to ScalaQuery's methods
`Database#withSession` and `Database#withTransaction` not being nestable.

When these calls are nested, it causes a different database session to be
used, which can lead to a race condition when immediately querying back a
changed object. For example:

```scala
database.withSession {
  // add object to database here
  database.withSession {
    // query same object back from database here
    // !! race condition which can have un-expected results since different database connection is used
  }
}
```

This mini-library enriches `Database` with methods `#withNestedSession` and `#withNestedTransaction`,
which behave in the way that we'd originally assumed:

```scala
import com.timgroup.scalaquery_nested.NestedScalaQuerySessionsAndTransactions._

database.withNestedSession {
  // add object to database here
  database.withNestedSession {
    // query same object back from database here
    // !! same connection used, so expected object is returned
  }
}
```

Caveat! Just like the ScalaQuery methods it is replacing, this implementation is ultimately based
on storing the database session in a Java ThreadLocal. This means the following code may return
unexpected results:

```scala
database.withNestedSession {
  // add object to database here
  val future = database.withNestedSession {
    Future {
      // query same object back from database here 
      // !! in a future which may be run in a different thread,
      // so different database connection might be used, leading to unexpected results
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
You can get scala-csv with the following line in your `build.sbt`:

```scala
libraryDependencies += "com.timgroup" %% "scalaquery_nested" % "1.0.0"
```

Compatibility
-------------
As of version [v1.0.0](https://github.com/youdevise/scalaquery_nested/blob/v1.0.0/.travis.yml),
we intransitively compile against ScalaQuery 0.10.0-M1 (the last pre-slick release), and our tests
pass on all Scala versions since 2.9.0-1.

Contributing
------------
Please feel free to send pull requests. Any change to code must come with tests, of course.
