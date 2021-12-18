package individual

import scala.collection.parallel.immutable.ParRange
import scalashop.parallel

 /**
  * Variant 11
  */
object IndividualFunction extends App {
  def calculateFunction: PartialFunction[(Double, Double, Double), Double] = {
    case (x, c, n) if x < -200 || x > 200 =>
      if (x > 200) c + x
      else Math.pow(x, n)
  }

  def toList(range: ParRange, c: Double, n: Double, numTasks: Int): List[Double] = {
    val partLength = math.floor(range.size.toDouble / numTasks.toDouble).toInt
    var arr = List[(Int, List[Double])]()

    def toListRecurs(i: Int): Unit =
      if (i < range.size)
        parallel({
            val delta = (i,
              range.slice(i, i + partLength)
                   .map(x => (x.toDouble, c, n))
                   .collect(calculateFunction)
                   .toList)
            arr = delta +: arr
          }, toListRecurs(i + partLength))

    toListRecurs(0)
    arr.sortWith((a,b) => a._1 < b._1).flatMap {
      case(_, list) => list
    }
  }

  def toList(range: Seq[Int], c: Double, n: Double): List[Double] =
    range.map(x => (x.toDouble, c, n)).collect(calculateFunction).toList

  val list: List[Double] = toList(new ParRange(-250 to 250), 2, 3, 8)
  println("Parallel:")
  println(list.zipWithIndex)
  println("Synchronous:")
  print(toList(-250 to 250, 2, 3).zipWithIndex)
}
