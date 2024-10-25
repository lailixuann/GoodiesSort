package makery.sorting.view

import makery.sorting.MyApp
import makery.sorting.model._
import scalafx.Includes._
import scalafx.animation.FadeTransition
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ButtonType}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{ClipboardContent, DragEvent, MouseEvent, TransferMode}
import scalafx.scene.layout.{AnchorPane, GridPane, HBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.util.Duration
import scalafxml.core.macros.sfxml

import scala.util.Random

@sfxml
class GameController(
                      private val rootPane: AnchorPane,
                      private val gridPane: GridPane,
                      private val restartButton: Button,
                      private val quitButton:Button
                    ) {
  var currentPlayer: Option[Player] = MyApp.selectedPlayer
  var currentLevelIndex: Int = MyApp.levelIndex
  var gameStarted:Boolean = true
  private val imageViewToItem = scala.collection.mutable.Map[ImageView, Item]()

  def handleStartGame(): Unit = {
    currentLevelIndex = MyApp.levelIndex
    println("Starting game... initializing shelves.")
    gridPane.children.clear()
    initializeShelves(LevelData.levels(currentLevelIndex))
  }

  def initializeShelves(levelConfig: LevelConfig): Unit = {
    gameStarted = true
    val totalPositions = levelConfig.shelfCount * levelConfig.itemsPerShelf

    // Ensure the number of items is a multiple of the items per shelf
    val adjustedItemCount = (totalPositions / levelConfig.itemsPerShelf) * levelConfig.itemsPerShelf

    // Generate the required number of items, ensuring that each item appears exactly 3 times
    val adjustedItems = ItemData.items.flatMap { item =>
      List.fill(levelConfig.itemsPerShelf)(item)
    }.take(adjustedItemCount)

    var shuffledItems = Random.shuffle(adjustedItems)
    gridPane.children.clear()

    val shelves = for (i <- 0 until levelConfig.shelfCount) yield {
      val hbox = new HBox {
        styleClass += "shelf"
      }
      val imageViews = for (j <- 0 until levelConfig.itemsPerShelf) yield {
        new ImageView {
          fitHeight = 100.0
          fitWidth = 50.0
          preserveRatio = true
          styleClass += "item-image"
        }
      }
      hbox.children.setAll(imageViews.map(_.delegate): _*)
      gridPane.add(hbox, i % 3, i / 3)
      hbox
    }

    shelves.zipWithIndex.foreach { case (shelf, shelfIndex) =>
      shelf.children.zipWithIndex.foreach { case (itemView, positionIndex) =>
        val itemIndex = shelfIndex * levelConfig.itemsPerShelf + positionIndex
        if (itemIndex < shuffledItems.length) {
          val item = shuffledItems(itemIndex)
          println(s"Adding item: ${item.name} to shelf $shelfIndex position $positionIndex")
          try {
            val image = new Image(item.imagePath)
            val imageView = itemView.asInstanceOf[javafx.scene.image.ImageView]
            imageView.setImage(image)
            imageViewToItem(imageView) = item

            // Drag-and-drop logic
            imageView.onDragDetected = (event: MouseEvent) => {
              val db = imageView.startDragAndDrop(Seq(TransferMode.Move.delegate): _*)
              val content = new ClipboardContent
              content.putImage(imageView.getImage)
              db.setContent(content)
              db.setDragView(imageView.getImage)
              event.consume()
            }

            imageView.onDragOver = (event: DragEvent) => {
              if (event.getGestureSource != imageView && event.getDragboard.hasImage) {
                event.acceptTransferModes(Seq(TransferMode.Move.delegate): _*)
              }
              event.consume()
            }

            imageView.onDragEntered = (event: DragEvent) => {
              if (event.getGestureSource != imageView && event.getDragboard.hasImage) {
                imageView.setOpacity(0.3)
              }
            }

            imageView.onDragExited = (event: DragEvent) => {
              if (event.getGestureSource != imageView && event.getDragboard.hasImage) {
                imageView.setOpacity(1.0)
              }
            }

            imageView.onDragDropped = (event: DragEvent) => {
              val db = event.getDragboard
              var success = false
              if (db.hasImage) {
                val sourceImageView = event.getGestureSource.asInstanceOf[javafx.scene.image.ImageView]
                val sourceImage = sourceImageView.getImage
                val targetImage = imageView.getImage

                // Swap the images
                imageView.setImage(sourceImage)
                sourceImageView.setImage(targetImage)
                val sourceItem = imageViewToItem.getOrElse(sourceImageView, null)
                val targetItem = imageViewToItem.getOrElse(imageView, null)
                if (sourceItem != null && targetItem != null) {
                  imageViewToItem(sourceImageView) = targetItem
                  imageViewToItem(imageView) = sourceItem
                }
                success = true
              }
              event.setDropCompleted(success)
              event.consume()
              checkAllShelves()
            }
            imageView.onDragDone = (event: DragEvent) => {
              event.consume()
            }
          } catch {
            case e: Exception => println(s"Failed to load image for item: ${item.name}, error: ${e.getMessage}")
          }
        } else {
          println(s"No item assigned to shelf $shelfIndex position $positionIndex")
        }
      }
    }
  }

  def checkAllShelves(): Unit = {
    val size: Int = gridPane.children.size
    gridPane.children.foreach { shelf =>
      val hbox = shelf.asInstanceOf[javafx.scene.layout.HBox]
      // Call your checkShelf function to check whether each shelf has 3 identical items
      checkShelf(hbox)
    }
  }

  var noOfShelves: Int = LevelData.levels(currentLevelIndex).shelfCount
  def checkShelf(shelf: HBox): Unit = {
    val imageViews = shelf.children.collect {
      case iv: javafx.scene.image.ImageView => iv
    }.toList

    if (imageViews.size == 3) {
      val items = imageViews.map(iv => imageViewToItem.getOrElse(iv, null))
      if (!items.contains(null) && items.forall(_ == items.head)) {
        println(s"Shelf completed with 3 ${items.head.name}")
        closeShelf(shelf)
        noOfShelves -= 1
      }
      if (noOfShelves == 0) {
        println("All shelves are sorted. You win the game!")
        MyApp.handleWin(currentPlayer)
      }
    }
  }


  def closeShelf(shelf: HBox) = {
    val rect = new Rectangle {
      width = 170
      height = 100
      fill = Color.rgb(129, 84, 56)
      opacity = 90
    }
    shelf.children.clear()
    shelf.children.add(rect)
    val fadeTransition = new FadeTransition {
      duration = Duration(1000)
      node = rect
      fromValue = 0
      toValue = 1.0
    }

    fadeTransition.setOnFinished(_ => {
      shelf.setDisable(true)
    })

    fadeTransition.play()
  }


  def handleQuit(): Unit = {
    val confirmation = new Alert(AlertType.Confirmation) {
      initOwner(MyApp.stage)
      title = "Confirm Quit"
      headerText = "You are about to quit the current level."
      contentText = "If you quit now, your progress for this level will be lost. " +
        "Are you sure you want to quit?"
    }

    val result = confirmation.showAndWait()

    println(s"Current player: ${currentPlayer.get.name.value}")
    result match {
      case Some(ButtonType.OK) =>
        currentPlayer match {
          case Some(player) =>
            Player.updatePlayerLevel(player.name.value, currentLevelIndex)
            MyApp.loadMain()
          case None =>
            println("No player is selected. Cannot update player level.")
        }
      case _ =>
      // If the user cancels or closes the dialog, do nothing
    }
  }
}
