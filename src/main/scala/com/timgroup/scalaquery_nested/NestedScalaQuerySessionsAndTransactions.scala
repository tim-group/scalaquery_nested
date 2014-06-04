package com.timgroup.scalaquery_nested

import java.sql.SQLException
import scala.util.control.Exception.catching

import scala.slick.session.{Database, Session}

/**
 * Extend this trait to enrich Database with #withNestedSession and Database#withNestedTransaction
 *
 * This implementation causes nested calls to share sessions and transactions, which matched
 * our initial (mis)understanding of how ScalaQuery's #withSession and #withTransaction calls 
 * worked.
 *
 * Note, however, that this implementation DOES still rely on thread-local variables for
 * passing around the existing sessions and transactions. Therefore, you MUST NOT structure
 * your database model code asynchronously, as the different bits could run on different
 * threads, and therefore in different sessions:
 *
 *     def incorrectlyAsyncDBAccess(param: String) {
 *       database withNestedSession {
 *         // WRONG -- Future may execute on different thread!
 *         Future { this.* insert Model(param) }
 *       }
 *     }
 */
trait NestedScalaQuerySessionsAndTransactions {

  implicit def enrichDatabaseWithNestedSessionAndTransaction(db: Database): DatabaseEnrichedWithNestedSessionAndTransaction =
    DatabaseEnrichedWithNestedSessionAndTransaction(db)

  case class DatabaseEnrichedWithNestedSessionAndTransaction(db: Database) {
    def withNestedSession[T](f: Session => T): T = withNestedSession { f(maybeThreadLocalSession.get) }

    def withNestedSession[T](f: => T): T = withExistingOrNewSession(db)(f)

    def withNestedTransaction[T](f: Session => T): T = withNestedSession { s => s.withTransaction(f(s)) }

    def withNestedTransaction[T](f: => T): T = withNestedSession { s => s.withTransaction(f) }
  }

  def withExistingOrNewSession[T](db: Database)(f: => T): T = {
    maybeThreadLocalSession match {
      case Some(s) => f
      case None    => db.withSession(f) // creates a new session
    }
  }

  /** Returns None if the ScalaQuery thread-local session is not set */
  def maybeThreadLocalSession: Option[Session] = catching(classOf[SQLException]) opt { Database.threadLocalSession }
}

/**
 * You can also statically import from this object
 */
object NestedScalaQuerySessionsAndTransactions extends NestedScalaQuerySessionsAndTransactions
