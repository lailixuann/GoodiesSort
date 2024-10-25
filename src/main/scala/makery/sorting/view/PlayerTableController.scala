package makery.sorting.view

import makery.sorting.MyApp
import makery.sorting.model.Player
import scalafx.beans.value.ObservableValue
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, TableColumn, TableView}
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

import scala.language.postfixOps
import scala.util.{Failure, Success}

@sfxml
class PlayerTableController(
                            private val playerTable: TableView[Player],
                            private val nameColumn: TableColumn[Player, String],
                            private val levelColumn: TableColumn[Player, Int],
                            private val cancelButton: Button,
                            private val deleteButton: Button,
                            private val selectButton: Button
                          ) {
  var dialogStage: Stage = _
  var selectedPlayer: Option[Player] = None

  refreshPlayerData()
  initialize()

  private def initialize(): Unit = {
    MyApp.playerData.clear()
    MyApp.playerData ++= Player.getAllPlayers
    playerTable.items = MyApp.playerData
    nameColumn.cellValueFactory = {
      _.value.name
    }
    levelColumn.cellValueFactory = { cellData => cellData.value.currentLevel.asInstanceOf[ObservableValue[Int, Int]] }
  }

  def refreshPlayerData(): Unit = {
//    val playersFromDB = Player.getAllPlayers
//    playerTable.items = ObservableBuffer(playersFromDB: _*)

//    nameColumn.cellValueFactory = {
//      _.value.name
//    }
//    levelColumn.cellValueFactory = { cellData => cellData.value.currentLevel.asInstanceOf[ObservableValue[Int, Int]] }
    initialize()
    println("Player data refreshed.")
  }

  def handleDelete(event: ActionEvent): Unit = {
    println("Delete button clicked")
    val selectedIndex = playerTable.selectionModel().getSelectedIndex
    if (selectedIndex >= 0) {
      println(s"Attempting to delete player at index $selectedIndex")
      MyApp.playerData.remove(selectedIndex).delete() match {
        case Success(_) =>
          refreshPlayerData()
          println("Player delete successfully")
        case Failure(e) =>
          val alert = new Alert(AlertType.Information) {
            initOwner(MyApp.stage)
            title = "Failed to Remove Player"
            headerText = "Failure to Remove Player"
            contentText = "An error occurred while trying to remove the player from the database."
          }
          alert.showAndWait()
      }
    } else {
      val alert = new Alert(AlertType.Warning) {
        initOwner(MyApp.stage)
        title = "No Selection"
        headerText = "No Player Selected"
        contentText = "Please select a player in the table."
      }
      alert.showAndWait()
    }
  }


  def handleSelect(event: ActionEvent): Unit = {
    println("Select button clicked")
    val selectedPlayerOpt = Option(playerTable.selectionModel().getSelectedItem)
    dialogStage.close()

    selectedPlayerOpt match {
      case Some(selectedPlayer) =>
        MyApp.selectedPlayer = Some(selectedPlayer)
        MyApp.showGame(selectedPlayer)

        try {
          selectedPlayer.save() match {
            case Success(_) =>
              if (!MyApp.showGame(selectedPlayer)) {
                // If game fails to start, show an alert
                new Alert(AlertType.Warning) {
                  initOwner(MyApp.stage)
                  title = "Failed to Start Game"
                  headerText = "Game Start Error"
                  contentText = "There was an issue starting the game. Please try again."
                }.showAndWait()
              }
            case Failure(e) =>
              // Show an error alert if saving player data fails
              new Alert(AlertType.Error) {
                initOwner(MyApp.stage)
                title = "Save Error"
                headerText = "Error Loading Player Data"
                contentText = s"An error occurred while loading the player data: ${e.getMessage}"
              }.showAndWait()
          }
        } catch {
          case e: Exception =>
            new Alert(AlertType.Error) {
              initOwner(MyApp.stage)
              title = "Error"
              headerText = "Unexpected Error"
              contentText = s"An unexpected error occurred: ${e.getMessage}"
            }.showAndWait()
        }
      case None =>
        // Show an alert if no player is selected
        new Alert(AlertType.Warning) {
          initOwner(MyApp.stage)
          title = "No Selection"
          headerText = "No Player Selected"
          contentText = "Please select a player in the table."
        }.showAndWait()
    }
  }

  def handleCancel(): Unit = {
    println("Cancel button clicked")
    dialogStage.close()
  }
}
