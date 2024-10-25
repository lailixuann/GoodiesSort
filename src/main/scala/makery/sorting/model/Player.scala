package makery.sorting.model

import makery.sorting.util.Database
import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalikejdbc._

import scala.util.Try

class Player(val nameS: String, val levelI: Int) extends Database {
  val name = StringProperty(nameS)
  val currentLevel = IntegerProperty(levelI)

  def this() = this(null, 0)

  def save(): Try[Int] = {
    if (Player.isExist(name.value)) {
      Try(DB autoCommit { implicit session =>
        sql"""
        UPDATE players SET current_level = ${currentLevel.value}
        WHERE name = ${name.value}
      """.update.apply()
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
        INSERT INTO players (name, current_level) VALUES
        (${name.value}, ${currentLevel.value})
      """.update.apply()
      })
    }
  }

  def delete(): Try[Int] = {
    if (Player.isExist(name.value)) {
      Try(DB autoCommit { implicit session =>
        sql"""
          DELETE FROM players WHERE name = ${name.value}
        """.update.apply()
      }).recover {
        case e: Exception =>
          e.printStackTrace()
          0
      }
    } else {
      throw new Exception("Player does not exist in database")
    }
  }
}

object Player extends Database {
  def apply(nameS: String, levelI: Int): Player = new Player(nameS, levelI)

  // Create player table if not exists
  def initialize(): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
        CREATE TABLE players (
          id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
          name VARCHAR(64) NOT NULL,
          current_level INT NOT NULL
        )
      """.execute.apply()
    }
  }

  def isExist(name: String): Boolean = {
    DB readOnly { implicit session =>
      sql"""
           SELECT COUNT(*) FROM players WHERE name = $name
         """.map(_.int(1)).single.apply().getOrElse(0) > 0
    }
  }

  def insertPlayer(player: Player, level: Int): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
        INSERT INTO players (name, current_level)
        VALUES (${player.name.value}, ${level})
      """.update.apply()
    }
  }

  def updatePlayerLevel(name: String, level: Int): Unit = {
    try{
      DB.autoCommit { implicit session =>
        val updateCount = sql"""
        UPDATE players
        SET current_level = $level
        WHERE name = $name
      """.update.apply()

        if (updateCount > 0) {
          println(s"Successfully updated level for player $name to $level.")
        } else {
          println(s"No player found with name $name. Level update failed.")
        }
      }
    } catch {
      case e: Exception =>
        println(s"Failed to update player level: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  def getPlayerLevel(name: String): Option[Int] = {
    DB.readOnly { implicit session =>
      sql"""
        SELECT current_level FROM players WHERE name = $name
      """.map(_.int("current_level")).single.apply()
    }
  }

  def getPlayer(name: String): Option[Player] = {
    DB.readOnly { implicit session =>
      sql"""
        SELECT * FROM players WHERE name = $name
      """.map(rs => new Player(
          rs.string("name"),
          rs.int("current_level")))
        .single
        .apply()
    }
  }

  def getAllPlayers: List[Player] = {
    DB.readOnly { implicit session =>
      sql"""
        SELECT * FROM players
      """.map(rs => new Player(
        rs.string("name"),
        rs.int("current_level")))
        .list
        .apply()
    }
  }
}
