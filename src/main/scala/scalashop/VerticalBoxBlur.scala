package scalashop

import org.scalameter._

object VerticalBoxBlurRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns := 5,
    Key.exec.maxWarmupRuns := 10,
    Key.exec.benchRuns := 10,
    Key.verbose := true
  ) withWarmer new Warmer.Default

  def main(args: Array[String]): Unit = {
    val radius = 16
    val width = 1920
    val height = 1080
    val numTasks = 8
    val src = new Img(width, height)
    val dst = new Img(width, height)
    val seqTime = standardConfig measure {
      VerticalBoxBlur.blur(src, dst, 0, width, radius)
    }
    println(s"sequential blur time: $seqTime")

    val parTime = standardConfig measure {
      VerticalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $parTime")
    println(s"speedup: ${seqTime.value / parTime.value}")
  }

}

/** A simple, trivially parallelizable computation. */
object VerticalBoxBlur extends VerticalBoxBlurInterface {

  /** Blurs the columns of the source image `src` into the destination image
    * `dst`, starting with `from` and ending with `end` (non-inclusive).
    *
    * Within each column, `blur` traverses the pixels by going from top to
    * bottom.
    */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit = {
    for (x <- from until end) {
      var y = 0
      while (y < src.height) {
        dst.update(x, y, boxBlurKernel(src, x, y, radius))
        y += 1
      }
    }
  }

  /** Blurs the columns of the source image in parallel using `numTasks` tasks.
    *
    * Parallelization is done by stripping the source image `src` into
    * `numTasks` separate strips, where each strip is composed of some number of
    * columns.
    */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
    val partWidth = if (src.width / numTasks != 0)
                        src.width / numTasks
                    else 1

    def parBlurRecurs(src: Img, dst: Img, numTasks: Int, radius: Int, i: Int): Unit =
      if (i < src.width) parallel(
        blur(src,
             dst,                                  // Calculating blur
             clamp(i, 0, src.width - 1),           // From this
             clamp(i + partWidth, 0, src.width),   // To this
             radius),
        parBlurRecurs(src, dst, numTasks, radius, i + partWidth)
      )

    parBlurRecurs(src, dst, numTasks, radius, 0)
  }
}
