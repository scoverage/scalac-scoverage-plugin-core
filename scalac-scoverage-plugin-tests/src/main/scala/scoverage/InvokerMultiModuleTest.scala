package scoverage

import java.io.File
import java.util.UUID

import AssertUtil._
import org.junit.Assert._
import org.junit.{After, Before, Test}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Verify that the runtime can handle a multi-module project
 */
@RunWith(classOf[JUnit4])
class InvokerMultiModuleTest {

  val measurementDir = Array(
    new File(IOUtils.getTempDirectory, UUID.randomUUID.toString),
    new File(IOUtils.getTempDirectory, UUID.randomUUID.toString))

  @Before def setup(): Unit = {
    deleteMeasurementFiles()
    measurementDir.foreach(_.mkdirs())
  }

  @Test
  def callingInvokerInvokedOnWithDifferentDirectoriesPutsMeasurementsInDifferentDirectories() = {

    val testIds: Set[Int] = (1 to 10).toSet

    testIds.map { i: Int => Invoker.invoked(i, measurementDir(i % 2).toString) }

    // Verify measurements went to correct directory
    val measurementFiles0 = IOUtils.findMeasurementFiles(measurementDir(0))
    val idsFromFile0 = IOUtils.invoked(measurementFiles0).toSet

    assertTrue(idsFromFile0 === testIds.filter { i: Int => i % 2 == 0 })

    val measurementFiles1 = IOUtils.findMeasurementFiles(measurementDir(1))
    val idsFromFile1 = IOUtils.invoked(measurementFiles1).toSet

    assertTrue(idsFromFile1 === testIds.filter { i: Int => i % 2 == 1 })
  }

  @After def cleanup(): Unit = {
    deleteMeasurementFiles()
    measurementDir.foreach(_.delete)
  }

  private def deleteMeasurementFiles(): Unit = {
    measurementDir.foreach((md) => {
      if (md.isDirectory)
        md.listFiles().foreach(_.delete())
    })
  }
}
