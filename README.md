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
