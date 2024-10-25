package makery.sorting.view

import makery.sorting.MyApp
import scalafx.event.ActionEvent
import scalafx.scene.control.Button
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

@sfxml
class WinDialogController(
                           private val quitButton: Button,
                          private val nextLevelButton: Button
                         ){
  var dialogStage : Stage  = _
  var okClicked: Boolean = false

  def handleQuit(event: ActionEvent): Unit = {
    println("Quit button clicked")
    dialogStage.close()
    MyApp.loadMain()
  }

  def handleNextLevel(): Unit = {
    println("Next Level button clicked")
    okClicked = true
    dialogStage.close()
  }
}