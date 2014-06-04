package com.timgroup.scalaquery_nested

import java.sql.Connection
import scala.util.control.Exception.ignoring

import scala.slick.session.{Database, Session}
import org.specs2.mock.Mockito
import org.specs2.mutable
import org.specs2.specification.Scope

class NestedScalaQuerySessionsAndTransactionsUnitSpec extends mutable.Specification with Mockito {

  trait MockConnectionScope extends Scope with NestedScalaQuerySessionsAndTransactions {
    val conn = mock[Connection]
    val db = new Database { override def createConnection(): Connection = conn }
    def doNothing() { }
    def throwSomething() { throw new RuntimeException("ha!") }
  }

  "NestedScalaQuerySessionsAndTransactions#withNestedSession" should {

    "create a new thread-local session when none exists and f does NOT take session" in new MockConnectionScope {
      db withNestedSession { maybeThreadLocalSession must beSome }
      maybeThreadLocalSession must beNone
    }

    "create and pass a new thread-local session when none exists and f DOES take session" in new MockConnectionScope {
      db withNestedSession { s1: Session =>
        maybeThreadLocalSession must beSome
        s1 must beTheSameAs(maybeThreadLocalSession.get)
      }
      maybeThreadLocalSession must beNone
    }

    "reuse existing thread-local session when it exists in both types of functions" in new MockConnectionScope {
      // starting with f that DOES take a Session
      db withNestedSession { s1: Session =>
        db withNestedSession {
          db withNestedSession { s2: Session =>
            db withNestedSession {
              maybeThreadLocalSession must beSome(s1)
              s2 must beTheSameAs(s1)
            }
          }
        }
      }
      maybeThreadLocalSession must beNone

      // starting with f that does NOT take a Session
      db withNestedSession {
        val s0 = maybeThreadLocalSession.getOrElse(null)
        db withNestedSession { s1: Session =>
          db withNestedSession {
            db withNestedSession { s2: Session =>
              db withNestedSession {
                maybeThreadLocalSession must beSome(s0)
                s2 must beTheSameAs(s1)
                s1 must beTheSameAs(s0)
              }
            }
          }
        }
      }
      maybeThreadLocalSession must beNone
    }

    "reuse existing thread-local session across subsequent calls" in new MockConnectionScope {
      db withNestedSession { 
        val s0 = maybeThreadLocalSession.getOrElse(null)
        val s1 = db withNestedSession {
          s0 must beTheSameAs (maybeThreadLocalSession.get)
          maybeThreadLocalSession.get
        }
        val s2 = db withNestedSession {
          s0 must beTheSameAs (maybeThreadLocalSession.get)
          maybeThreadLocalSession.get
        }
        s1 must beTheSameAs(s2)
        s0 must not be (null)
      }
      maybeThreadLocalSession must beNone

    }

    "reuse existing thread-local session across subsequent calls with exception" in new MockConnectionScope {
      db withNestedSession { 
        val s0 = maybeThreadLocalSession.getOrElse(null)
        try {
          db withNestedSession {
            throwSomething()
          }
        } catch {
          case _ => // do nothing
        }

        val s2 = db withNestedSession {
          s0 must beTheSameAs (maybeThreadLocalSession.get)
          maybeThreadLocalSession.get
        }
        s0 must beTheSameAs (s2)
        s0 must not be (null)
      }
      maybeThreadLocalSession must beNone
    }
  }

  "NestedScalaQuerySessionsAndTransactions#withNestedTransaction" should {

    "NOT start a transaction when not called" in new MockConnectionScope {
      db withNestedSession { doNothing() }
      there were no(conn).setAutoCommit(false)
    }

    "start a transaction when called" in new MockConnectionScope {
      db withNestedTransaction { doNothing() }
      there was one(conn).setAutoCommit(false)
      there was one(conn).setAutoCommit(true)
    }

    "rollback a transaction when exception thrown" in new MockConnectionScope {
      ignoring(classOf[RuntimeException]) { db withNestedTransaction { throwSomething() } }
      there was one(conn).setAutoCommit(false)
      there was one(conn).rollback()
      there was one(conn).setAutoCommit(true)
    }

    "rollback a nested transaction when exception thrown" in new MockConnectionScope {
      ignoring(classOf[RuntimeException]) {
        db withNestedTransaction { s1: Session =>
          db withNestedTransaction {
            db withNestedTransaction { s2: Session =>
              db withNestedTransaction {
                throwSomething()
              }
            }
          }
        }
      }
      there was one(conn).setAutoCommit(false)
      there was one(conn).rollback()
      there was one(conn).setAutoCommit(true)
    }

    "reuse existing transaction when it exists" in new MockConnectionScope {
      db withNestedTransaction { s1: Session =>
        db withNestedTransaction {
          db withNestedTransaction { s2: Session =>
            db withNestedTransaction {
              doNothing()
            }
          }
        }
      }
      there was one(conn).setAutoCommit(false)
      there was one(conn).setAutoCommit(true)
    }

    "create a new thread-local session when none exists and f does NOT take session" in new MockConnectionScope {
      db withNestedTransaction { maybeThreadLocalSession must beSome }
      maybeThreadLocalSession must beNone
    }

    "create and pass a new thread-local session when none exists and use in withNestedSession" in new MockConnectionScope {
      db withNestedTransaction { 
        val s0 = maybeThreadLocalSession.getOrElse(null)
        db withNestedSession {
          val s1 = maybeThreadLocalSession.get
          s1 must beTheSameAs(s0)
        }
      }
      maybeThreadLocalSession must beNone
    }

    "create and pass a new thread-local session when none exists and use in withNestedSession and subsequent after exception thrown" in new MockConnectionScope {
      db withNestedTransaction { 
        val s0 = maybeThreadLocalSession.getOrElse(null)
        try {
          db withNestedSession {
             throwSomething()
          }
        } catch {
          case _ => // Do nothing
        }
        db withNestedSession {
          val s1 = maybeThreadLocalSession.get
          s1 must beTheSameAs(s0)
        }
      }
      maybeThreadLocalSession must beNone
    }

    "create and pass a new thread-local session when none exists and f DOES take session" in new MockConnectionScope {
      db withNestedTransaction { s1: Session =>
        maybeThreadLocalSession must beSome
        s1 must beTheSameAs(maybeThreadLocalSession.get)
      }
      maybeThreadLocalSession must beNone
    }

    "reuse existing thread-local session when it exists in both types of functions" in new MockConnectionScope {
      // starting with f that DOES take a Session
      db withNestedTransaction { s1: Session =>
        db withNestedTransaction {
          db withNestedTransaction { s2: Session =>
            db withNestedTransaction {
              maybeThreadLocalSession must beSome(s1)
              s2 must beTheSameAs(s1)
            }
          }
        }
      }
      maybeThreadLocalSession must beNone

      // starting with f that does NOT take a Session
      db withNestedTransaction {
        val s0 = maybeThreadLocalSession.getOrElse(null)
        db withNestedTransaction { s1: Session =>
          db withNestedTransaction {
            db withNestedTransaction { s2: Session =>
              db withNestedTransaction {
                maybeThreadLocalSession must beSome(s0)
                s2 must beTheSameAs(s1)
                s1 must beTheSameAs(s0)
              }
            }
          }
        }
      }
      maybeThreadLocalSession must beNone
    }

  }

}
