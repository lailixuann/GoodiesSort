package makery.sorting.util

import makery.sorting.model.Player
import scalikejdbc._
trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"

  val dbURL = "jdbc:derby:myDB;create=true;";
  Class.forName(derbyDriverClassname)

  ConnectionPool.singleton(dbURL, "me", "mine")

  implicit val session = AutoSession
}

object Database extends Database {
  def setupDB() = {
    if (!hasDBInitialize)
      Player.initialize()
  }

  def hasDBInitialize: Boolean = {

    DB getTable "Players" match {
      case Some(x) => true
      case None => false
    }
  }
}