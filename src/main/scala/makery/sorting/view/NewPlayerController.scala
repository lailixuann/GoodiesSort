package makery.sorting.view

import makery.sorting.MyApp
import scalafx.scene.control.{Alert, Button, TextField}
import scalafx.stage.Stage
import makery.sorting.model.Player
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType
import scalafxml.core.macros.sfxml


@sfxml
class NewPlayerController(
                           private val playerName: TextField,
                           private val cancelButton: Button,
                           private val confirmButton: Button
                         ) {
  var dialogStage: Stage = _
  private var _player: Player = _
  var confirmClicked = false

  def handleCancel(event: ActionEvent): Unit = {
    println("Cancel clicked")
    dialogStage.close();
  }

  def handleConfirm(): Player = {
    println("Confirm clicked")

    if (isInputValid) {
      val playerNameInput = playerName.getText
      val player = new Player(playerNameInput, 0)
      MyApp.selectedPlayer = Some(player)
      try {
        Player.insertPlayer(player, 0)
        confirmClicked = true
        dialogStage.close()
        if (MyApp.showGame(player)) {
          player
        } else {
          new Alert(AlertType.Warning) {
            title = "Failed to Start Game"
            headerText = "Game Start Error"
            contentText = "There was an issue starting the game. Please try again."
          }.showAndWait()
          null.asInstanceOf[Player]
        }
      } catch {
        case e: Exception =>
          new Alert(AlertType.Error) {
            title = "Database Error"
            headerText = "Error Inserting Player"
            contentText = s"An error occurred while inserting the player: ${e.getMessage}"
          }.showAndWait()
          null.asInstanceOf[Player]
      }
    } else {
      null.asInstanceOf[Player]
    }
  }


  private def isInputValid: Boolean = {
    val name = playerName.getText
    if (name.isEmpty) {
      showError("Name cannot be empty")
      false
    } else if (Player.isExist(name)) {
      showError("A player with this name already exists")
      false
    }
    else {
      true
    }
  }

  private def showError(message: String): Unit = {
    new Alert(AlertType.Error) {
      initOwner(dialogStage)
      title = "Error"
      headerText = "Error"
      contentText = message
    }.showAndWait()
  }
}