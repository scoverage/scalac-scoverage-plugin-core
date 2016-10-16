package scoverage

import org.mockito.Mockito
import org.mockito.Mockito._

import AssertUtil._
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.reflect.internal.util.{BatchSourceFile, SourceFile, NoFile}
import scala.reflect.io.AbstractFile

@RunWith(classOf[JUnit4])
class RegexCoverageFilterTest {

  @Test
  def isClassIncludedShouldReturnTrueForEmptyExcludes() = {
    assertTrue(new RegexCoverageFilter(Nil, Nil, Nil).isClassIncluded("x"))
  }

  @Test
  def isClassIncludedShouldNotCrashForEmptyInput() = {
    assertTrue(new RegexCoverageFilter(Nil, Nil, Nil).isClassIncluded(""))
  }

  @Test
  def isClassIncludedShouldExcludeScoverageArrowScoverage() = {
    assertTrue(!new RegexCoverageFilter(Seq("scoverage"), Nil, Nil).isClassIncluded("scoverage"))
  }

  @Test
  def isClassIncludedShouldIncludeScoverageArrowScoverageeee() = {
    assertTrue(new RegexCoverageFilter(Seq("scoverage"), Nil, Nil).isClassIncluded("scoverageeee"))
  }

  @Test
  def isClassIncludedShouldExcludeScoverageStarArrowScoverageeee() = {
    assertTrue(!new RegexCoverageFilter(Seq("scoverage*"), Nil, Nil).isClassIncluded("scoverageeee"))
  }

  @Test
  def isClassIncludedShouldIncludeEeeArrowScoverageeee() = {
    assertTrue(new RegexCoverageFilter(Seq("eee"), Nil, Nil).isClassIncluded("scoverageeee"))
  }

  @Test
  def isClassIncludedShouldExcludeDotStarEeeArrowScoverageeee() = {
    assertTrue(!new RegexCoverageFilter(Seq(".*eee"), Nil, Nil).isClassIncluded("scoverageeee"))
  }

  val abstractFile = mock(classOf[AbstractFile])
  Mockito.when(abstractFile.path).thenReturn("sammy.scala")

  @Test
  def isFileIncludedShouldReturnTrueForEmptyExcludes() = {
    val file = new BatchSourceFile(abstractFile, Array.emptyCharArray)
    assertTrue(new RegexCoverageFilter(Nil, Nil, Nil).isFileIncluded(file))
  }

  @Test
  def isFileIncludedShouldExcludeByFilename() = {
    val file = new BatchSourceFile(abstractFile, Array.emptyCharArray)
    assertFalse(new RegexCoverageFilter(Nil, Seq("sammy"), Nil).isFileIncluded(file))
  }

  @Test
  def isFileIncludedShouldExcludeByRegexWildcard() = {
    val file = new BatchSourceFile(abstractFile, Array.emptyCharArray)
    assertFalse(new RegexCoverageFilter(Nil, Seq("sam.*"), Nil).isFileIncluded(file))
  }

  @Test
  def isFileIncludedShouldNotExcludeNonMatchingRegex() = {
    val file = new BatchSourceFile(abstractFile, Array.emptyCharArray)
    assertTrue(new RegexCoverageFilter(Nil, Seq("qweqeqwe"), Nil).isFileIncluded(file))
  }

  val options = new ScoverageOptions()

  @Test
  def isSymbolIncludedShouldReturnTrueForEmptyExcludes() = {
    assertTrue(new RegexCoverageFilter(Nil, Nil, Nil).isSymbolIncluded("x"))
  }

  @Test
  def isSymbolIncludedShouldNotCrashForEmptyInput() = {
    assertTrue(new RegexCoverageFilter(Nil, Nil, Nil).isSymbolIncluded(""))
  }

  @Test
  def isSymbolIncludedShouldExcludeScoverageArrowScoverage() = {
    assertTrue(!new RegexCoverageFilter(Nil, Nil, Seq("scoverage")).isSymbolIncluded("scoverage"))
  }

  @Test
  def isSymbolIncludedShouldIncludeScoverageArrowScoverageeee() = {
    assertTrue(new RegexCoverageFilter(Nil, Nil, Seq("scoverage")).isSymbolIncluded("scoverageeee"))
  }

  @Test
  def isSymbolIncludedShouldExcludeScoverageStarArrowScoverageeee() = {
    assertTrue(!new RegexCoverageFilter(Nil, Nil, Seq("scoverage*")).isSymbolIncluded("scoverageeee"))
  }

  @Test
  def isSymbolIncludedShouldIncludeEeeArrowScoverageeee() = {
    assertTrue(new RegexCoverageFilter(Nil, Nil, Seq("eee")).isSymbolIncluded("scoverageeee"))
  }

  @Test
  def isSymbolIncludedShouldExcludeDotStarEeeArrowScoverageeee() = {
    assertTrue(!new RegexCoverageFilter(Nil, Nil, Seq(".*eee")).isSymbolIncluded("scoverageeee"))
  }

  @Test
  def isSymbolIncludedShouldExcludeScalaReflectApiExprsExpr() = {
    assertTrue(!new RegexCoverageFilter(Nil, Nil, options.excludedSymbols).isSymbolIncluded("scala.reflect.api.Exprs.Expr"))
  }

  @Test
  def isSymbolIncludedShouldExcludeScalaReflectMacrosUniverseTree() = {
    assertTrue(!new RegexCoverageFilter(Nil, Nil, options.excludedSymbols).isSymbolIncluded("scala.reflect.macros.Universe.Tree"))
  }

  @Test
  def isSymbolIncludedShouldExcludeScalaReflectApiTreesTree() = {
    assertTrue(!new RegexCoverageFilter(Nil, Nil, options.excludedSymbols).isSymbolIncluded("scala.reflect.api.Trees.Tree"))
  }

  @Test
  def getExcludedLineNumbersShouldExcludeNoLinesIfNoMagicCommentsAreFound() = {
    val file =
      """1
        |2
        |3
        |4
        |5
        |6
        |7
        |8
      """.stripMargin

    val numbers = new RegexCoverageFilter(Nil, Nil, Nil).getExcludedLineNumbers(mockSourceFile(file))
    assertTrue(numbers === List.empty)
  }

  @Test
  def getExcludedLineNumbersShouldExcludeLinesBetweenMagicComments() = {
    val file =
      """1
        |2
        |3
        |  // $COVERAGE-OFF$
        |5
        |6
        |7
        |8
        |    // $COVERAGE-ON$
        |10
        |11
        |    // $COVERAGE-OFF$
        |13
        |    // $COVERAGE-ON$
        |15
        |16
      """.stripMargin

    val numbers = new RegexCoverageFilter(Nil, Nil, Nil).getExcludedLineNumbers(mockSourceFile(file))
    assertTrue(numbers === List(Range(4, 9), Range(12, 14)))
  }

  @Test
  def getExcludedLineNumbersShouldExcludeAllLinesAfterAnUnpairedMagicComment() = {
    val file =
      """1
        |2
        |3
        |  // $COVERAGE-OFF$
        |5
        |6
        |7
        |8
        |    // $COVERAGE-ON$
        |10
        |11
        |    // $COVERAGE-OFF$
        |13
        |14
        |15
      """.stripMargin

    val numbers = new RegexCoverageFilter(Nil, Nil, Nil).getExcludedLineNumbers(mockSourceFile(file))
    assertTrue(numbers === List(Range(4, 9), Range(12, 16)))
  }

  @Test
  def getExcludedLineNumbersShouldAllowTextCommentsOnTheSameLineAsTheMarkers() = {
    val file =
      """1
        |2
        |3
        |  // $COVERAGE-OFF$ because the next lines are boring
        |5
        |6
        |7
        |8
        |    // $COVERAGE-ON$ resume coverage here
        |10
        |11
        |    // $COVERAGE-OFF$ but ignore this bit
        |13
        |14
        |15
      """.stripMargin

    val numbers = new RegexCoverageFilter(Nil, Nil, Nil).getExcludedLineNumbers(mockSourceFile(file))
    assertTrue(numbers === List(Range(4, 9), Range(12, 16)))
  }

  private def mockSourceFile(contents: String): SourceFile = {
    new BatchSourceFile(NoFile, contents.toCharArray)
  }
}

