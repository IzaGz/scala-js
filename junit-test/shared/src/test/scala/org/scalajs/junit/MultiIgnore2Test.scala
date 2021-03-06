package org.scalajs.junit

import org.junit._
import org.scalajs.junit.utils.JUnitTest

class MultiIgnore2Test {
  @Ignore @Test def multiTest1(): Unit = ()
  @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()
  @Ignore @Test def multiTest4(): Unit = ()
  @Test def multiTest5(): Unit = ()
}

class MultiIgnore2TestAssertions extends JUnitTest {
  protected def expectedOutput(builder: OutputBuilder): OutputBuilder = {
    builder
      .ignored("multiTest1")
      .success("multiTest2")
      .success("multiTest3")
      .ignored("multiTest4")
      .success("multiTest5")
  }
}
