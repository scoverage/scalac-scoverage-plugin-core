package scoverage

import java.io.StringWriter

import AssertUtil._
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.xml.Utility

@RunWith(classOf[JUnit4])
class SerializerTest {

  @Test
  def coverageShouldBeSerializableIntoXml() = {
    val coverage = Coverage()
    coverage.add(
      Statement(
        "mysource",
        Location("org.scoverage", "test", "org.scoverage.test", ClassType.Trait, "mymethod", "mypath"),
        14, 100, 200, 4, "def test : String", "test", "DefDef", true, 32
      )
    )
    val expected = <statements>
      <statement>
        <source>mysource</source> <package>org.scoverage</package> <class>test</class> <classType>Trait</classType> <fullClassName>org.scoverage.test</fullClassName> <method>mymethod</method> <path>mypath</path> <id>14</id> <start>100</start> <end>200</end> <line>4</line> <description>def test : String</description> <symbolName>test</symbolName> <treeName>DefDef</treeName> <branch>true</branch> <count>32</count> <ignored>false</ignored>
      </statement>
    </statements>
    val writer = new StringWriter()
    val actual = Serializer.serialize(coverage, writer)
    assertTrue(Utility.trim(expected) === Utility.trim(xml.XML.loadString(writer.toString)))
  }
}
