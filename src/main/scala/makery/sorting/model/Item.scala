package makery.sorting.model

case class Item(name: String, imagePath: String)

object ItemData {
  val items = List(
    Item("Apple Juice", "makery/sorting/image/AppleJuice.png"),
    Item("Orange Juice", "makery/sorting/image/OrangeJuice.png"),
    Item("Bunny", "makery/sorting/image/Bunny.png"),
    Item("Beer", "makery/sorting/image/Beer.png"),
    Item("Tiger", "makery/sorting/image/Tiger.png"),
    Item("Chips", "makery/sorting/image/Chips.png"),
    Item("Ketchup", "makery/sorting/image/Ketchup.png"),
    Item("PiggyBank", "makery/sorting/image/PiggyBank.png"),
    Item("Giraffe", "makery/sorting/image/Giraffe.png"),
    Item("Corn", "makery/sorting/image/Corn.png"),
    Item("Milk", "makery/sorting/image/Milk.png"),
    Item("Tuna", "makery/sorting/image/Tuna.png")
  )
}