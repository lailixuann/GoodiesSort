package makery.sorting.view

import makery.sorting.MyApp
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button}
import scalafxml.core.macros.sfxml

@sfxml
class WelcomeController(
                         private val newPlayerButton: Button,
                         private val continueButton: Button,
                         private val instructionsButton: Button
                       ) {
  def handleNewPlayer(event: ActionEvent): Unit = {
    println("New Player button clicked")
    MyApp.showNewPlayer()
  }

  def handleContinue(event: ActionEvent): Unit = {
    println("Continue button clicked")
    MyApp.showPlayerTable()
  }

  def handleInstructions(event: ActionEvent): Unit = {
    println("Instructions button clicked")
    new Alert(AlertType.Information) {
      initOwner(newPlayerButton.getScene.getWindow)
      width = 500
      height = 450
      title = "Instructions"
      headerText = "How to Play"
      contentText = "This game is to SORT items into shelves through swapping them. " +
        "Each shelf needs to be filled with 3 identical items to be completed.\n\n" +
        "TO SWAP: Drag an item by clicking and holding on its image and drop it onto another item.\n\n" +
        "TO QUIT: Click the \"Quit\" button, please confirm your decision to quit; progress for the current level will be lost if you proceed."
    }.showAndWait()
  }
}