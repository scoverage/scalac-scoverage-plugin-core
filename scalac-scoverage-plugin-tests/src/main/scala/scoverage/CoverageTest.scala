package scoverage

import AssertUtil._
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** @author Stephen Samuel */
@RunWith(classOf[JUnit4])
class CoverageTest {

  @Test
  def coverageForNoStatementsIs1() = {
    val coverage = Coverage()
    assertTrue(1.0 === coverage.statementCoverage)
  }

  @Test
  def coverageForNoInvokedStatementsIs0() = {
    val coverage = Coverage()
    coverage.add(Statement("", Location("", "", "", ClassType.Object, "", ""), 1, 2, 3, 4, "", "", "", false, 0))
    assertTrue(0.0 === coverage.statementCoverage)
  }

  @Test
  def coverageForInvokedStatements() = {
    val coverage = Coverage()
    coverage.add(Statement("", Location("", "", "", ClassType.Object, "", ""), 1, 2, 3, 4, "", "", "", false, 3))
    coverage.add(Statement("", Location("", "", "", ClassType.Object, "", ""), 2, 2, 3, 4, "", "", "", false, 0))
    coverage.add(Statement("", Location("", "", "", ClassType.Object, "", ""), 3, 2, 3, 4, "", "", "", false, 0))
    coverage.add(Statement("", Location("", "", "", ClassType.Object, "", ""), 4, 2, 3, 4, "", "", "", false, 0))
    assertTrue(0.25 === coverage.statementCoverage)
  }
}
