package makery.sorting.model

case class LevelConfig(shelfCount: Int, itemsPerShelf: Int)

object LevelData {
  val levels: List[LevelConfig] = List(
    LevelConfig(2, 3), // Level 0: 2 shelves, 3 items each
    LevelConfig(3, 3), // Level 1: 3 shelves, 3 items each
    LevelConfig(4, 3), // Level 2: 4 shelves, 3 items each
    LevelConfig(5, 3), // Level 3: 5 shelves, 3 items each
    LevelConfig(6, 3), // Level 4: 6 shelves, 3 items each
    LevelConfig(7, 3), // Level 5: 7 shelves, 3 items each
    LevelConfig(8, 3), // Level 6: 8 shelves, 3 items each
    LevelConfig(9, 3), // Level 7: 9 shelves, 3 items each
    LevelConfig(10, 3),// Level 8: 10 shelves, 3 items each
    LevelConfig(11, 3),// Level 9: 11 shelves, 3 items each
    LevelConfig(12, 3) // Level 10: 12 shelves, 3 items each
  )
}