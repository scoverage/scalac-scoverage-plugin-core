package scoverage

import scala.xml.Node

object AssertUtil {

  implicit class TypedOps[A](v1: A) {
    def ===(v2: A): Boolean = v1 == v2
  }

  implicit class NodeOps(n1: Node) {
    def ===(n2: Node): Boolean = n1.strict_==(n2)
  }
}
