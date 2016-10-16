package scoverage

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** @author Stephen Samuel */
@RunWith(classOf[JUnit4])
class PluginCoverageTest {

  @Test
  def scoverageShouldInstrumentDefaultArgumentsWithMethods() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ object DefaultArgumentsObject {
        |  val defaultName = "world"
        |  def makeGreeting(name: String = defaultName): String = {
        |    s"Hello, $name"
        |  }
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    // we expect:
    // instrumenting the default-param which becomes a method call invocation
    // the method makeGreeting is entered.
    compiler.assertNMeasuredStatements(2)
  }

  @Test
  def scoverageShouldSkipMacros() = {
    val compiler = ScoverageCompiler.default
    val code = if (ScoverageCompiler.ShortScalaVersion == "2.10")
      """
              import scala.language.experimental.macros
              import scala.reflect.macros.Context
              object Impl {
                def poly[T: c.WeakTypeTag](c: Context) = c.literal(c.weakTypeOf[T].toString)
              }

              object Macros {
                def poly[T] = macro Impl.poly[T]
              }"""
    else
      """
              import scala.language.experimental.macros
              import scala.reflect.macros.Context
              class Impl(val c: Context) {
                import c.universe._
                def poly[T: c.WeakTypeTag] = c.literal(c.weakTypeOf[T].toString)
              }
              object Macros {
                def poly[T] = macro Impl.poly[T]
              }"""
    compiler.compileCodeSnippet(code)
    assertTrue(!compiler.reporter.hasErrors)
    compiler.assertNMeasuredStatements(0)
  }

  @Test
  def scoverageShouldInstrumentFinalVals() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ object FinalVals {
        |  final val name = {
        |     val name = "sammy"
        |     if (System.currentTimeMillis() > 0) {
        |      println(name)
        |     }
        |  }
        |  println(name)
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    // we should have 3 statements - initialising the val, executing println, and executing the parameter
    compiler.assertNMeasuredStatements(8)
  }

  @Test
  def scoverageShouldNotInstrumentTheMatchAsAStatement() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ object A {
        |    System.currentTimeMillis() match {
        |      case x => println(x)
        |    }
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)

    /** should have the following statements instrumented:
      * the selector, clause 1
      */
    compiler.assertNMeasuredStatements(2)
  }

  @Test
  def scoverageShouldInstrumentMatchGuards() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ object A {
        |    System.currentTimeMillis() match {
        |      case l if l < 1000 => println("a")
        |      case l if l > 1000 => println("b")
        |      case _ => println("c")
        |    }
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)

    /** should have the following statements instrumented:
      * the selector, guard 1, clause 1, guard 2, clause 2, clause 3
      */
    compiler.assertNMeasuredStatements(6)
  }

  @Test
  def scoverageShouldInstrumentNonBasicSelector() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ trait A {
        |  def someValue = "sammy"
        |  def foo(a:String) = someValue match {
        |    case any => "yes"
        |  }
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    // should instrument:
    // the someValue method entry
    // the selector call
    // case block "yes" literal
    compiler.assertNMeasuredStatements(3)
  }

  @Test
  def scoverageShouldInstrumentConditionalSelectorsInAMatch() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ trait A {
        |  def foo(a:String) = (if (a == "hello") 1 else 2) match {
        |    case any => "yes"
        |  }
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    // should instrument:
    // the if clause,
    // thenp block,
    // thenp literal "1",
    // elsep block,
    // elsep literal "2",
    // case block "yes" literal
    compiler.assertNMeasuredStatements(6)
  }

  // https://github.com/scoverage/sbt-scoverage/issues/16
  @Test
  def scoverageShouldInstrumentForLoopsButNotTheGeneratedScaffolding() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ trait A {
        |  def print1(list: List[String]) = for (string: String <- list) println(string)
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    // should instrument:
    // the def method entry
    // foreach body
    // specifically we want to avoid the withFilter partial function added by the compiler
    compiler.assertNMeasuredStatements(2)
  }

  @Test
  def scoverageShouldInstrumentForLoopGuards() = {
    val compiler = ScoverageCompiler.default

    compiler.compileCodeSnippet(
      """object A {
        |  def foo(list: List[String]) = for (string: String <- list if string.length > 5)
        |    println(string)
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    // should instrument:
    // foreach body
    // the guard
    // but we want to avoid the withFilter partial function added by the compiler
    compiler.assertNMeasuredStatements(3)
  }

  @Test
  def scoverageShouldCorrectlyHandleNewWithArgsApplyWithListOfArgs() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ object A {
        |  new String(new String(new String))
        | } """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    // should have 3 statements, one for each of the nested strings
    compiler.assertNMeasuredStatements(3)
  }

  @Test
  def scoverageShouldCorrectlyHandleNoArgsNewApplyEmptyListOfArgs() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ object A {
        |  new String
        | } """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    // should have 1. the apply that wraps the select.
    compiler.assertNMeasuredStatements(1)
  }

  @Test
  def scoverageShouldCorrectlyHandleNewThatInvokesNestedStatements() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """
        | object A {
        |  val value = new java.util.concurrent.CountDownLatch(if (System.currentTimeMillis > 1) 5 else 10)
        | } """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    // should have 6 statements - the apply/new statement, two literals, the if cond, if elsep, if thenp
    compiler.assertNMeasuredStatements(6)
  }

  @Test
  def scoverageShouldInstrumentValRHS() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """object A {
        |  val name = BigDecimal(50.0)
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    compiler.assertNMeasuredStatements(1)
  }

  @Test
  def scoverageShouldNotInstrumentFunctionTupleWrapping() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """
        |    sealed trait Foo
        |    case class Bar(s: String) extends Foo
        |    case object Baz extends Foo
        |
        |    object Foo {
        |      implicit val fooOrdering: Ordering[Foo] = Ordering.fromLessThan {
        |        case (Bar(_), Baz) => true
        |        case (Bar(a), Bar(b)) => a < b
        |        case (_, _) => false
        |      }
        |    }
      """.stripMargin)

    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    // should have 4 profiled statements: the outer apply, the true, the a < b, the false
    // we are testing that we don't instrument the tuple2 call used here
    compiler.assertNMeasuredStatements(4)
  }

  @Test
  def scoverageShouldInstrumentAllCaseStatementsInAnExplicitMatch() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """ trait A {
        |  def foo(name: Any) = name match {
        |    case i : Int => 1
        |    case b : Boolean => println("boo")
        |    case _ => 3
        |  }
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
    // should have one statement for each case body
    // selector is a constant so would be ignored.
    compiler.assertNMeasuredStatements(3)
  }

  @Test
  def pluginShouldSupportYields() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """
        |  object Yielder {
        |    val holidays = for ( name <- Seq("sammy", "clint", "lee");
        |                         place <- Seq("london", "philly", "iowa") ) yield {
        |      name + " has been to " + place
        |    }
        |  }""".stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    // 2 statements for the two applies in Seq, one for each literal which is 6, one for the flat map,
    // one for the map, one for the yield op.
    compiler.assertNMeasuredStatements(11)
  }

  @Test
  def pluginShouldNotInstrumentLocalMacroImplementation() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """
        | object MyMacro {
        | import scala.language.experimental.macros
        | import scala.reflect.macros.Context
        |  def test = macro testImpl
        |  def testImpl(c: Context): c.Expr[Unit] = {
        |    import c.universe._
        |    reify {
        |      println("macro test")
        |    }
        |  }
        |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    compiler.assertNoCoverage()
  }

  /* Make sure this is covered in another repo, then delete

  test("plugin should not instrument expanded macro code github.com/skinny-framework/skinny-framework/issues/97") {
    val compiler = ScoverageCompiler.default
    scalaLoggingDeps.foreach(compiler.addToClassPath(_))
    compiler.compileCodeSnippet( s"""import ${scalaLoggingPackageName}.StrictLogging
                                   |class MacroTest extends StrictLogging {
                                   |  logger.info("will break")
                                   |} """.stripMargin)
    assert(!compiler.reporter.hasErrors)
    assert(!compiler.reporter.hasWarnings)
    compiler.assertNoCoverage()
  }

  ignore("plugin should handle return inside catch github.com/scoverage/scalac-scoverage-plugin/issues/93") {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet(
      """
        |    object bob {
        |      def fail(): Boolean = {
        |        try {
        |          true
        |        } catch {
        |          case _: Throwable =>
        |            Option(true) match {
        |              case Some(bool) => return recover(bool) // comment this return and instrumentation succeeds
        |              case _ =>
        |            }
        |            false
        |        }
        |      }
        |      def recover(it: Boolean): Boolean = it
        |    }
      """.stripMargin)
    assert(!compiler.reporter.hasErrors)
    assert(!compiler.reporter.hasWarnings)
    compiler.assertNMeasuredStatements(11)
  }
  */
}
