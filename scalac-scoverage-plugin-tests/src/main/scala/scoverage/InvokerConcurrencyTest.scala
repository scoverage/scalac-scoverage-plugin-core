package scoverage

import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

import AssertUtil._
import org.junit.Assert._
import org.junit.{After, Before, Test}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.collection.breakOut
import scala.concurrent._
import scala.concurrent.duration._

/**
 * Verify that the runtime is thread-safe
 */
@RunWith(classOf[JUnit4])
class InvokerConcurrencyTest {

  implicit val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))

  val measurementDir = new File(IOUtils.getTempDirectory, UUID.randomUUID.toString)  //"target/invoker-test.measurement.concurrent")

  @Before def setup(): Unit =  {
    deleteMeasurementFiles()
    measurementDir.mkdirs()
  }

  @Test
  def callingInvokerInvokedOnMultipleThreadsDoesNotCorruptTheMeasurementFile() = {

    val testIds: Set[Int] = (1 to 1000).toSet

    // Create 1k "invoked" calls on the common thread pool, to stress test
    // the method
    val futures: List[Future[Unit]] = testIds.map { i: Int =>
      Future {
        Invoker.invoked(i, measurementDir.toString)
      }
    }(breakOut)

    futures.foreach(Await.result(_, 1.second))

    // Now verify that the measurement file is not corrupted by loading it
    val measurementFiles = IOUtils.findMeasurementFiles(measurementDir)
    val idsFromFile = IOUtils.invoked(measurementFiles).toSet

    assertTrue(idsFromFile === testIds)
  }

  @After def cleanup(): Unit = {
    deleteMeasurementFiles()
    measurementDir.delete()
  }

  private def deleteMeasurementFiles(): Unit = {
    if (measurementDir.isDirectory)
      measurementDir.listFiles().foreach(_.delete())
  }
}
