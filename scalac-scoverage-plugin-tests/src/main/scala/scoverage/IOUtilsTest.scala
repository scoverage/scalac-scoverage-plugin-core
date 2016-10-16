package scoverage

import java.io.{File, FileWriter}
import java.util.UUID

import AssertUtil._
import org.junit.Assert._
import org.junit.{After, Before, Test}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** @author Stephen Samuel */
@RunWith(classOf[JUnit4])
class IOUtilsTest {

  val base = new File(IOUtils.getTempDirectory, UUID.randomUUID.toString)

  @Before def setup(): Unit = {
    base.mkdir()
  }

  @Test
  def shouldParseMeasurementFiles() = {
    val file = newTempFile("scoveragemeasurementtest.txt")
    val writer = new FileWriter(file)
    writer.write("1\n5\n9\n\n10\n")
    writer.close()
    val invokedSet = IOUtils.invoked(Seq(file)).toSet

    assertTrue(invokedSet === Set(1, 5, 9, 10))
  }

  @Test
  def shouldParseMultipleMeasurementFiles() = {
    val file1 = newTempFile("scoverage.measurements.11.txt")
    val writer1 = new FileWriter(file1)
    writer1.write("1\n5\n9\n\n10\n")
    writer1.close()
    val file2 = newTempFile("scoverage.measurements.22.txt")
    val writer2 = new FileWriter(file2)
    writer2.write("1\n7\n14\n\n2\n")
    writer2.close()

    val files = IOUtils.findMeasurementFiles(file1.getParent)
    val invokedSet = IOUtils.invoked(files).toSet

    assertTrue(invokedSet === Set(1, 2, 5, 7, 9, 10, 14))
  }

  @Test
  def shouldDeepSearchForReportFiles() = {

    val file1 = newTempFile(Constants.XMLReportFilename)
    val writer1 = new FileWriter(file1)
    writer1.write("1\n3\n5\n\n\n7\n")
    writer1.close()

    val file2 = newTempFile(UUID.randomUUID + "/" + Constants.XMLReportFilename)
    file2.getParentFile.mkdir()
    val writer2 = new FileWriter(file2)
    writer2.write("2\n4\n6\n\n8\n")
    writer2.close()

    val file3 = new File(file2.getParent + "/" + UUID.randomUUID + "/" + Constants.XMLReportFilename)
    file3.getParentFile.mkdir()
    val writer3 = new FileWriter(file3)
    writer3.write("11\n20\n30\n\n44\n")
    writer3.close()

    val files = IOUtils.reportFileSearch(base, IOUtils.isReportFile)
    val invokedSet = IOUtils.invoked(files).toSet

    assertTrue(invokedSet === Set(1, 2, 3, 4, 5, 6, 7, 8, 11, 20, 30, 44))
  }

  @After def cleanup(): Unit = {
    base.delete()
  }

  private def newTempFile(file: String): File = {
    new File(s"$base/$file")
  }
}
