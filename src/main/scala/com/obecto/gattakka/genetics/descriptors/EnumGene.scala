package com.obecto.gattakka.genetics.descriptors

import scala.math.BigInt
import scala.collection.immutable.Vector

object EnumGeneDescriptor {
  def apply[T](value1: T, values: T*): EnumGeneDescriptor[T] = {
    EnumGeneDescriptor((value1 +: values).toVector)
  }

  def apply[T](values: Seq[T]): EnumGeneDescriptor[T] = {
    EnumGeneDescriptor(values.toVector)
  }
}

case class EnumGeneDescriptor[T](val values: Vector[T]) extends GeneDescriptor {
  val length: Int = (Math.log((values.size + 1).abs.toDouble) / Math.log(2)).ceil.toInt

  def apply(rnd: scala.util.Random): EnumGene[T] = {
    new EnumGene(rnd.nextInt(values.size), this)
  }

  def apply(byteArray: Array[Byte]): EnumGene[T] = {
    new EnumGene(BigInt(Array[Byte](0) ++ byteArray.take(byteLength)).toInt % values.size, this)
  }

  def apply(value: Int): EnumGene[T] = {
    val normalizedValue = value % values.size
    new EnumGene(normalizedValue, this)
  }

  def apply(value: T): EnumGene[T] = {
    apply(values.find(value == _).get)
  }
}

case class EnumGene[T](valueIndex: Int, descriptor: EnumGeneDescriptor[T]) extends Gene {
  val value = descriptor.values(valueIndex)

  def toByteArray: Array[Byte] = {
    var unpadded = BigInt(valueIndex).toByteArray
    if (unpadded.head == 0) {
      unpadded = unpadded.slice(1, unpadded.length)
    }
    val padding = Array[Byte]().padTo(descriptor.byteLength - unpadded.length, 0.toByte)
    padding ++ unpadded
  }

}
