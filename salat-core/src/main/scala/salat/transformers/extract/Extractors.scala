/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         Extractors.scala
 * Last modified: 2016-07-10 23:49:08 EDT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package salat.transformers

import com.mongodb.casbah.Imports._
import salat._
import salat.annotations.util._
import salat.util.Logging

import scala.tools.scalap.scalax.rules.scalasig._

package object out {
  def select(t: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    t match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if ctx.caseObjectHierarchy.contains(symbol.path) => {
          new Transformer(t.symbol.path, t)(ctx) with OptionExtractor with CaseObjectExtractor
        }
        case TypeRefType(_, symbol, _) if ctx.customTransformers.contains(symbol.path) => {
          new CustomOptionDeserializer(ctx.customTransformers(symbol.path), symbol.path, t, ctx)
        }
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with BigDecimalExtractor

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with BigIntExtractor

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with CharToString

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with FloatToDouble

        case TypeRefType(_, symbol, _) if isDouble(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with FloatToDouble

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with DateTimeZoneExtractor

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with OptionExtractor with EnumStringifier
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with InContextToDBObject {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionExtractor
      }
      case IsBinary(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) => {
          new Transformer(symbol.path, t)(ctx) with BinaryExtractor
        }
      }
      case IsTraversable(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if ctx.caseObjectHierarchy.contains(symbol.path) => {
          new Transformer(t.symbol.path, t)(ctx) with CaseObjectExtractor with TraversableExtractor
        }
        case TypeRefType(_, symbol, _) if ctx.customTransformers.contains(symbol.path) =>
          new CustomTraversableDeserializer(ctx.customTransformers(symbol.path), symbol.path, t, ctx)

        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigDecimalExtractor with TraversableExtractor

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntExtractor with TraversableExtractor

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with FloatToDouble with TraversableExtractor

        case TypeRefType(_, symbol, _) if isDouble(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with FloatToDouble with TraversableExtractor

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with CharToString with TraversableExtractor

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateTimeZoneExtractor with TraversableExtractor

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumStringifier with TraversableExtractor
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with TraversableExtractor {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) =>
          new Transformer(symbol.path, t)(ctx) with TraversableExtractor
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if ctx.caseObjectHierarchy.contains(symbol.path) => {
          new Transformer(t.symbol.path, t)(ctx) with CaseObjectExtractor with MapExtractor
        }

        case TypeRefType(_, symbol, _) if ctx.customTransformers.contains(symbol.path) =>
          new CustomMapDeserializer(ctx.customTransformers(symbol.path), symbol.path, t, ctx)

        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigDecimalExtractor with MapExtractor

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntExtractor with MapExtractor

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with CharToString with MapExtractor

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateTimeZoneExtractor with MapExtractor

        case TypeRefType(_, symbol, _) if isDouble(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with FloatToDouble with MapExtractor

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with FloatToDouble with MapExtractor

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" =>
          new Transformer(prefix.symbol.path, t)(ctx) with EnumStringifier with MapExtractor

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with MapExtractor {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapExtractor
      }
      case pt if ctx.caseObjectHierarchy.contains(pt.symbol.path) => {
        new Transformer(pt.symbol.path, pt)(ctx) with CaseObjectExtractor
      }
      case TypeRefType(_, symbol, _) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigDecimalExtractor

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntExtractor

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with CharToString

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateTimeZoneExtractor

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with FloatToDouble

        case TypeRefType(_, symbol, _) if isDouble(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with FloatToDouble

        case TypeRefType(_, symbol, _) if Types.isBitSet(symbol) =>
          new Transformer(symbol.path, t)(ctx) with BitSetExtractor

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumStringifier
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject {
            val grater = ctx.lookup_?(symbol.path)
          }
        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) {}
      }
    }
  }
}

package out {

  trait CaseObjectExtractor extends Transformer with Logging {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = {
      val name = value.asInstanceOf[AnyRef].getClass.getName
      ctx.caseObjectOverrides.getOrElse(name, {
        MongoDBObject(ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(name))
      })
    }
  }

  object UtilsExtractor {
    def toDBObject(value: Any)(implicit ctx: Context): Any = value match {
      case x: BasicDBObject                   => map2DBObject(x)
      case x: BasicDBList                     => list2DBList(x)
      case x: scala.collection.Map[_, _]      => map2DBObject(x)
      case x: scala.collection.Traversable[_] => list2DBList(x)
      case Some(x)                            => Some(toDBObject(x))
      case y: AnyRef if ctx.lookup_?(Some(y.getClass.getCanonicalName).getOrElse(y.getClass.getName)).isDefined =>
        grater[y.type].asDBObject(y)
      case _ => value
    }

    private def map2DBObject(x: scala.collection.Map[_, _])(implicit ctx: Context) = {
      val builder = MongoDBObject.newBuilder
      x.foreach {
        case (k, el) =>
          builder += k.toString -> toDBObject(el)
      }

      builder.result()
    }

    private def list2DBList(x: scala.collection.Traversable[_])(implicit cxt: Context) = {
      MongoDBList(x.map(toDBObject(_)).toList: _*)
    }
  }

  trait BigDecimalExtractor extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = ctx.bigDecimalStrategy.out(value)
  }

  trait BigIntExtractor extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case bi: BigInt               => ctx.bigIntStrategy.out(bi)
      case bi: java.math.BigInteger => ctx.bigIntStrategy.out(bi)
      case l: Long                  => ctx.bigIntStrategy.out(l)
      case i: Int                   => ctx.bigIntStrategy.out(i)
    }
  }

  trait FloatToDouble extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context) = value match {
      case d: Double            => d
      case i: Int               => i.toDouble
      case i: java.lang.Integer => i.toDouble
      case i: java.lang.Double  => i.toDouble
      case l: Long              => l.toDouble
      case s: Short             => s.toDouble
      case f: Float             => f.toDouble
      case f: java.lang.Float   => f.doubleValue()
    }
  }

  trait CharToString extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context) = value match {
      case c: Char                => c.toString
      case c: java.lang.Character => c.toString
    }
  }

  trait InContextToDBObject extends Transformer with InContextTransformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case cc: CaseClass => ctx.lookup(path, cc).asInstanceOf[Grater[CaseClass]].asDBObject(cc)
      case _             => MongoDBObject("failed-to-convert" -> value.toString)
    }
  }

  trait OptionExtractor extends Transformer {
    self: Transformer =>

    // ok, Some(null) should never happen.  except sometimes it does.
    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case Some(null)  => None
      case Some(value) => Some(super.transform(value))
      case _           => None
    }

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case _ if ctx.lookup_?(path).isDefined => Some(value)
      case _                                 => Some(UtilsExtractor.toDBObject(value))
    }
  }

  trait TraversableExtractor extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    protected def transformElem(el: Any) = super.transform(el)

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case traversable: Traversable[_] =>
        Some(MongoDBList(traversable.map(transformElem).map(UtilsExtractor.toDBObject).toList: _*))
      case _ => None
    }
  }

  trait BinaryExtractor extends Transformer with Logging {
    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case bs: Seq[Byte] => Some(bs.toArray)
      case _             => None
    }
  }

  trait BitSetExtractor extends Transformer with Logging {

    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case bs: scala.collection.BitSet => Some(bs.toArray.map(_.toByte))
      case _                           => None
    }
  }

  trait DateTimeZoneExtractor extends Transformer {
    override def transform(value: Any)(implicit ctx: Context) = value match {
      case tz: org.joda.time.DateTimeZone => Some(tz.getID)
      case tz: java.util.TimeZone         => Some(tz.getID)
      case _                              => None
    }
  }

  trait MapExtractor extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    protected def transformElem(el: Any) = super.transform(el)

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case map: scala.collection.Map[_, _] => {
        val builder = MongoDBObject.newBuilder
        map.foreach {
          case (k, el) =>
            builder += (k match {
              case s: String => s
              case x         => x.toString
            }) -> UtilsExtractor.toDBObject(transformElem(el))
        }
        Some(builder.result())
      }
      case _ => None
    }
  }

  trait EnumStringifier extends Transformer {
    self: Transformer =>

    val strategy = {
      val s = getClassNamed_!(path).annotation[salat.annotations.raw.EnumAs].
        map(_.strategy())
      if (s.isDefined) s.get else ctx.defaultEnumStrategy
    }

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case ev: Enumeration#Value if strategy == EnumStrategy.BY_VALUE => ev.toString
      case ev: Enumeration#Value if strategy == EnumStrategy.BY_ID    => ev.id
    }
  }

}

