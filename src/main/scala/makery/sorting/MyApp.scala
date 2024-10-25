package makery.sorting

import javafx.{scene => jfxs}
import makery.sorting.model.Player
import makery.sorting.view.{GameController, NewPlayerController, PlayerTableController, WelcomeController, WinDialogController}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.stage.{Modality, Stage}
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import makery.sorting.util.Database
import scalafx.collections.ObservableBuffer
import scalafx.scene.media.{Media, MediaPlayer}


object MyApp extends JFXApp {
  Database.setupDB()
  val playersFromDB = Player.getAllPlayers
  val playerData = ObservableBuffer(playersFromDB: _*)

  var selectedPlayer: Option[Player] = None
  var levelIndex: Int = 0

  val rootResource = getClass.getResource("view/Welcome.fxml")
  val loader = new FXMLLoader(rootResource, NoDependencyResolver)
  loader.load();
  val roots: jfxs.layout.AnchorPane = loader.getRoot[jfxs.layout.AnchorPane]
  loader.getController[WelcomeController#Controller]
  initializeBGM()
  playBGM()

  private var bgm: MediaPlayer = _

  stage = new PrimaryStage() {
    title = "Goodies Sort"
    scene = new Scene {
      stylesheets += getClass.getResource("view/Style.css").toString
      root = roots
    }
  }

  def loadMain() ={
    val rootResource = getClass.getResource("view/Welcome.fxml")
    val loader = new FXMLLoader(rootResource, NoDependencyResolver)
    loader.load();
    val roots: jfxs.layout.AnchorPane = loader.getRoot[jfxs.layout.AnchorPane]
    loader.getController[WelcomeController#Controller]
    val scene = new Scene(roots)
    stage.setScene(scene)
    stage.show()
    initializeBGM()
    playBGM()
  }

  def initializeBGM() = {
    val path = getClass.getResource("overworld-melody.mp3").toExternalForm
    val media = new Media(path)
    bgm = new MediaPlayer(media){
      cycleCount = MediaPlayer.Indefinite
    }
  }

  def playBGM(): Unit = {
    bgm.play()
  }

  def stopBGM(): Unit = {
    bgm.stop()
  }

  def showNewPlayer() = {
    val resource = getClass.getResource("view/NewPlayer.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val newPlayerRoot = loader.getRoot[jfxs.Parent]
    val control = loader.getController[NewPlayerController#Controller]

    val dialog = new Stage() {
      initModality(Modality.ApplicationModal)
      initOwner(stage)
      scene = new Scene {
        root = newPlayerRoot
      }
    }
    control.dialogStage = dialog
    dialog.showAndWait()
  }

  def showPlayerTable(): Option[Player] = {
    val resource = getClass.getResource("view/PlayerTable.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val showPlayerRoot = loader.getRoot[jfxs.Parent]
    val control = loader.getController[PlayerTableController#Controller]

    val dialog = new Stage() {
      initModality(Modality.ApplicationModal)
      initOwner(stage)
      scene = new Scene {
        root = showPlayerRoot
      }
    }
    control.dialogStage = dialog
    dialog.showAndWait()
    MyApp.selectedPlayer = control.selectedPlayer
    MyApp.selectedPlayer.foreach(player => MyApp.levelIndex = player.currentLevel.asInstanceOf[Int])
    MyApp.selectedPlayer
  }


  def showGame(selectedPlayer: Player): Boolean = {
    try {
      val resource = getClass.getResource("view/Game.fxml")
      val loader = new FXMLLoader(resource, NoDependencyResolver)
      val root = loader.load().asInstanceOf[jfxs.layout.AnchorPane]
      val scene = new Scene(root)
      val control = loader.getController[GameController#Controller]

      // Initialize GameController with the selected player and level index
      control.currentPlayer = MyApp.selectedPlayer
      levelIndex = selectedPlayer.currentLevel.value

      println(s"Starting game for ${selectedPlayer.name.value} at level $levelIndex")
      control.handleStartGame()
      MyApp.stage.setScene(scene)
      control.gameStarted
    } catch {
      case e: Exception =>
        println(s"Failed to load the game scene: ${e.getMessage}")
        false
    }
  }


  def handleWin(currentPlayer: Option[Player]): Unit = {
    val resource = getClass.getResource("view/WinDialog.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    val winRoot = loader.load().asInstanceOf[jfxs.Parent]
    val control = loader.getController[WinDialogController#Controller]

    // Create and configure the dialog
    val dialog = new Stage() {
      initModality(Modality.ApplicationModal)
      initOwner(MyApp.stage)
      scene = new Scene {
        stylesheets += getClass.getResource("view/Game.css").toString
        root = winRoot
      }
    }
    control.dialogStage = dialog
    dialog.showAndWait()
    if (control.okClicked) {
      loadNextLevel(currentPlayer)
    }
  }

  def loadNextLevel(selectedPlayer: Option[Player]): Unit = {
    levelIndex += 1
    selectedPlayer match {
      case Some(player) =>
        // Update player level safely
        try {
          Player.updatePlayerLevel(player.name.value, levelIndex)
          println(s"Player ${player.name.value} level updated to $levelIndex.")
          MyApp.selectedPlayer = Some(player)  // Update the selected player in MyApp
        } catch {
          case e: Exception =>
            println(s"Failed to update player level: ${e.getMessage}")
        }

        // Load the next level's scene
        val resource = getClass.getResource("view/Game.fxml")
        val loader = new FXMLLoader(resource, NoDependencyResolver)
        val root = loader.load().asInstanceOf[jfxs.layout.AnchorPane]
        val scene = new Scene(root)
        val control = loader.getController[GameController#Controller]
        control.handleStartGame()
        MyApp.stage.setScene(scene)

      case None =>
        println("No player selected, cannot proceed")
    }
  }


}