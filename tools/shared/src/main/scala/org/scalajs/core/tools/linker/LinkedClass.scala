/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js tools             **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013-2014, LAMP/EPFL   **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */


package org.scalajs.core.tools.linker

import scala.collection.mutable

import org.scalajs.core.ir
import ir.Trees._
import ir.Position
import ir.Infos
import ir.ClassKind
import ir.Definitions

/** A ClassDef after linking.
 *
 *  Note that the [[version]] in the LinkedClass does not cover
 *  [[staticMethods]], [[memberMethods]], [[abstractMethods]] and
 *  [[exportedMembers]] as they have their individual versions. (The collections
 *  themselves are not versioned).
 *
 *  Moreover, the [[version]] is relative to the identity of a LinkedClass.
 *  The definition of identity varies as linked classes progress through the
 *  linking pipeline, but it only gets stronger, i.e., if two linked classes
 *  are id-different at phase P, then they must also be id-different at phase
 *  P+1. The converse is not true. This guarantees that versions can be used
 *  reliably to determine at phase P+1 whether a linked class coming from phase
 *  P must be reprocessed.
 */
final class LinkedClass(
    // Stuff from Tree
    val name: Ident,
    val kind: ClassKind,
    val superClass: Option[Ident],
    val interfaces: List[Ident],
    val jsNativeLoadSpec: Option[JSNativeLoadSpec],
    val fields: List[FieldDef],
    val staticMethods: List[LinkedMember[MethodDef]],
    val memberMethods: List[LinkedMember[MethodDef]],
    val abstractMethods: List[LinkedMember[MethodDef]],
    val exportedMembers: List[LinkedMember[MemberDef]],
    val topLevelExports: List[TopLevelExportDef],
    val topLevelExportsInfo: Option[Infos.MethodInfo],
    val optimizerHints: OptimizerHints,
    val pos: Position,

    // Actual Linking info
    val ancestors: List[String],
    val hasInstances: Boolean,
    val hasInstanceTests: Boolean,
    val hasRuntimeTypeInfo: Boolean,
    val version: Option[String]) {

  // Helpers to give Info-Like access
  def encodedName: String = name.name
  def isExported: Boolean = topLevelExports.nonEmpty

  /** Names of all top-level exports in this class. */
  def topLevelExportNames: List[String] = topLevelExports.map {
    case TopLevelConstructorExportDef(name, _, _) => name
    case TopLevelModuleExportDef(name)            => name
    case TopLevelJSClassExportDef(name)           => name

    case TopLevelMethodExportDef(MethodDef(_, propName, _, _, _)) =>
      val StringLiteral(name) = propName
      name

    case TopLevelFieldExportDef(name, _) => name
  }

  def fullName: String = Definitions.decodeClassName(encodedName)

  def toInfo: Infos.ClassInfo = {
    val methodInfos = (
        staticMethods.map(_.info) ++
        memberMethods.map(_.info) ++
        abstractMethods.map(_.info) ++
        exportedMembers.map(_.info) ++
        topLevelExportsInfo
    )

    Infos.ClassInfo(encodedName, isExported, kind, superClass.map(_.name),
      interfaces.map(_.name), methodInfos)
  }

  def copy(
      name: Ident = this.name,
      kind: ClassKind = this.kind,
      superClass: Option[Ident] = this.superClass,
      interfaces: List[Ident] = this.interfaces,
      jsNativeLoadSpec: Option[JSNativeLoadSpec] = this.jsNativeLoadSpec,
      fields: List[FieldDef] = this.fields,
      staticMethods: List[LinkedMember[MethodDef]] = this.staticMethods,
      memberMethods: List[LinkedMember[MethodDef]] = this.memberMethods,
      abstractMethods: List[LinkedMember[MethodDef]] = this.abstractMethods,
      exportedMembers: List[LinkedMember[MemberDef]] = this.exportedMembers,
      topLevelExports: List[TopLevelExportDef] = this.topLevelExports,
      topLevelExportsInfo: Option[Infos.MethodInfo] = this.topLevelExportsInfo,
      optimizerHints: OptimizerHints = this.optimizerHints,
      pos: Position = this.pos,
      ancestors: List[String] = this.ancestors,
      hasInstances: Boolean = this.hasInstances,
      hasInstanceTests: Boolean = this.hasInstanceTests,
      hasRuntimeTypeInfo: Boolean = this.hasRuntimeTypeInfo,
      version: Option[String] = this.version): LinkedClass = {
    new LinkedClass(
        name,
        kind,
        superClass,
        interfaces,
        jsNativeLoadSpec,
        fields,
        staticMethods,
        memberMethods,
        abstractMethods,
        exportedMembers,
        topLevelExports,
        topLevelExportsInfo,
        optimizerHints,
        pos,
        ancestors,
        hasInstances,
        hasInstanceTests,
        hasRuntimeTypeInfo,
        version)
  }
}
