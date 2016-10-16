package scoverage

object RuntimeInfo {
 def runtimePath: String = "./scalac-scoverage-runtime-java"
 def name: String = "java"
}

class ThisCoverageTest extends CoverageTest
class ThisInvokerConcurrencyTest extends InvokerConcurrencyTest
class ThisInvokerMultiModuleTest extends InvokerMultiModuleTest
class ThisIOUtilsTest extends IOUtilsTest
class ThisLocationTest extends LocationTest
class ThisPluginASTSupportTest extends PluginASTSupportTest
class ThisPluginCoverageTest extends PluginCoverageTest
class ThisRegexCoverageFilterTest extends RegexCoverageFilterTest
class ThisSerializerTest extends SerializerTest
