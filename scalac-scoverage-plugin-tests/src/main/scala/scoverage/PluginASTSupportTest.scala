package scoverage

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** @author Stephen Samuel */
@RunWith(classOf[JUnit4])
class PluginASTSupportTest {

  @Test
  def scoverageComponentShouldIgnoreBasicMacros() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet( """
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
  }

  @Test
  def scoverageComponentShouldIgnoreComplexMacros11() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet( """ object ComplexMacro {
                          |
                          |  import scala.language.experimental.macros
                          |  import scala.reflect.macros.Context
                          |
                          |  def debug(params: Any*) = macro debugImpl
                          |
                          |  def debugImpl(c: Context)(params: c.Expr[Any]*) = {
                          |    import c.universe._
                          |
                          |    val trees = params map {param => (param.tree match {
                          |      case Literal(Constant(_)) => reify { print(param.splice) }
                          |      case _ => reify {
                          |        val variable = c.Expr[String](Literal(Constant(show(param.tree)))).splice
                          |        print(s"$variable = ${param.splice}")
                          |      }
                          |    }).tree
                          |    }
                          |
                          |    val separators = (1 until trees.size).map(_ => (reify { print(", ") }).tree) :+ (reify { println() }).tree
                          |    val treesWithSeparators = trees zip separators flatMap {p => List(p._1, p._2)}
                          |
                          |    c.Expr[Unit](Block(treesWithSeparators.toList, Literal(Constant(()))))
                          |  }
                          |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
  }

  // https://github.com/scoverage/scalac-scoverage-plugin/issues/32
  @Test
  def exhaustiveWarningsShouldNotBeGeneratedForUnchecked() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet( """object PartialMatchObject {
                          |  def partialMatchExample(s: Option[String]): Unit = {
                          |    (s: @unchecked) match {
                          |      case Some(str) => println(str)
                          |    }
                          |  }
                          |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
  }



  // https://github.com/scoverage/scalac-scoverage-plugin/issues/45
  @Test
  def compileFinalValsInAnnotations() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet( """object Foo  {
                          |  final val foo = 1L
                          |}
                          |@SerialVersionUID(Foo.foo)
                          |class Bar
                          |""".stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
  }

  @Test
  def typeParamWithDefaultArgSupported() = {
    val compiler = ScoverageCompiler.default
    compiler.compileCodeSnippet( """class TypeTreeObjects {
                                   |  class Container {
                                   |    def typeParamAndDefaultArg[C](name: String = "sammy"): String = name
                                   |  }
                                   |  new Container().typeParamAndDefaultArg[Any]()
                                   |} """.stripMargin)
    assertTrue(!compiler.reporter.hasErrors)
    assertTrue(!compiler.reporter.hasWarnings)
  }
}

