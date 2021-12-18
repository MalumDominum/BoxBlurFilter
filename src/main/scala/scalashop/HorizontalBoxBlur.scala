package scalashop

import org.scalameter._

object HorizontalBoxBlurRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns := 5,
    Key.exec.maxWarmupRuns := 10,
    Key.exec.benchRuns := 10,
    Key.verbose := true
  ) withWarmer new Warmer.Default

  def main(args: Array[String]): Unit = {
    val radius = 3
    val width = 1920
    val height = 1080
    val numTasks = 8
    val src = new Img(width, height)
    val dst = new Img(width, height)
    val seqTime = standardConfig measure {
      HorizontalBoxBlur.blur(src, dst, 0, height, radius)
    }
    val parTime = standardConfig measure {
      HorizontalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $parTime")
    println(s"speedup: ${seqTime.value / parTime.value}")
  }
}

/** A simple, trivially parallelizable computation. */
object HorizontalBoxBlur extends HorizontalBoxBlurInterface {

  /** Blurs the rows of the source image `src` into the destination image `dst`,
   *  starting with `from` and ending with `end` (non-inclusive).
   *
   *  Within each row, `blur` traverses the pixels by going from left to right.
   */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit = {
    for (y <- from until end) {
      var x = 0
      while (x < src.width) {
        dst.update(x, y, boxBlurKernel(src, x, y, radius))
        x += 1
      }
    }
  }

  /** Blurs the rows of the source image in parallel using `numTasks` tasks.
   *
   *  Parallelization is done by stripping the source image `src` into
   *  `numTasks` separate strips, where each strip is composed of some number of
   *  rows.
   */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
    val partHeight = if (src.height / numTasks != 0)
                         src.height / numTasks
                     else 1

    def parBlurRecurs(src: Img, dst: Img, numTasks: Int, radius: Int, i: Int): Unit =
      if (i < src.height) parallel(
        blur(src,
             dst,
             clamp(i, 0, src.height - 1),
             clamp(i + partHeight, 0, src.height),
             radius),
        parBlurRecurs(src, dst, numTasks, radius, i + partHeight)
      )

    parBlurRecurs(src, dst, numTasks, radius, 0)
  }
}
