package scalashop

import org.junit._
import org.junit.Assert.assertEquals

class BlurSuite {
  val pixels: List[List[RGBA]] =
    List(List((0, 0, 0, 0)
    , (0, 0, 0, 0)
    , (0, 0, 0, 0))
    , List((0, 0, 0, 0)
      , (0, 0, 0, 0)
      , (0, 0, 0, 0))
    , List((0, 0, 0, 0)
      , (0, 0, 0, 0)
      , (255, 127, 63, 14))).map(list => list.map {
    case (r, g, b, a) => rgba(r, g, b, a)
  })
  val blurredPixels: List[List[RGBA]] = List(List((0, 0, 0, 0)
    , (0, 0, 0, 0)
    , (0, 0, 0, 0))
    , List((0, 0, 0, 0)
      , (28, 14, 7, 1)
      , (56, 28, 14, 3))
    , List((0, 0, 0, 0)
      , (56, 28, 14, 3)
      , (113, 56, 28, 6))).map(list => list.map {
    case (r, g, b, a) => rgba(r, g, b, a)
  })

  @Test
  def testCommonUse(): Unit = {
    val src = new Img(pixels.head.length, pixels.length)
    val dst = new Img(pixels.head.length, pixels.length)
    for (y <- pixels.indices; x <- pixels.head.indices) src.update(x, y, pixels(x)(y))

    HorizontalBoxBlur.parBlur(src, dst, 3, 1)
    for (y <- pixels.indices; x <- pixels.head.indices) {
      assertEquals(blurredPixels(x)(y), dst.apply(x, y))
    }
  }

  @Test
  def testMoreTasks(): Unit = {
    val src = new Img(pixels.head.length, pixels.length)
    val dst = new Img(pixels.head.length, pixels.length)
    for (y <- pixels.indices; x <- pixels.head.indices) src.update(x, y, pixels(x)(y))

    HorizontalBoxBlur.parBlur(src, dst, 4, 1)
    for (y <- pixels.indices; x <- pixels.head.indices) {
      assertEquals(blurredPixels(x)(y), dst.apply(x, y))
    }
  }

  @Test
  def testForLastStrip(): Unit = {
    val src = new Img(pixels.head.length, pixels.length)
    val dst = new Img(pixels.head.length, pixels.length)
    for (y <- pixels.indices; x <- pixels.head.indices) src.update(x, y, pixels(x)(y))

    HorizontalBoxBlur.parBlur(src, dst, 2, 1)
    for (y <- pixels.indices; x <- pixels.head.indices) {
      assertEquals(blurredPixels(x)(y), dst.apply(x, y))
    }
  }

  @Test
  def testKernel(): Unit = {
    val src = new Img(pixels.head.length, pixels.length)
    for (y <- pixels.indices; x <- pixels.head.indices) src.update(x, y, pixels(x)(y))
    assertEquals(boxBlurKernel(src, 2, 2, 1), rgba(113, 56, 28, 6))

  }

  @Rule def individualTestTimeout = new org.junit.rules.Timeout(10 * 1000)
}
