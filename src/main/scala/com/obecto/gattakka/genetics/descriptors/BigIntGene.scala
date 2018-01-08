package com.obecto.gattakka.genetics.descriptors

import scala.math.BigInt

object BigIntGeneDescriptor {
  def apply(length: Int): BigIntGeneDescriptor = {
    new BigIntGeneDescriptor(length)
  }
}

case class BigIntGeneDescriptor(val length: Int) extends GeneDescriptor {
  val byteLength: Int = (length.toDouble / 8).ceil.toInt
  val maxValue: BigInt = (BigInt(1) << length) - 1
  val maxValueDouble: Double = maxValue.doubleValue

  def apply(rnd: scala.util.Random): BigIntGene = {
    new BigIntGene(BigInt(numbits = length, rnd = rnd), this)
  }

  def apply(byteArray: Array[Byte]): BigIntGene = {
    new BigIntGene(BigInt(byteArray.take(byteLength)), this)
  }

  def apply(value: BigInt): BigIntGene = {
    val normalizedValue = value & maxValue // Zero highest bits, just in case
    new BigIntGene(normalizedValue, this)
  }
}

case class BigIntGene(value: BigInt, descriptor: BigIntGeneDescriptor) extends Gene {
  def toDouble: Double = value.doubleValue / descriptor.maxValueDouble

  def toByteArray: Array[Byte] = {
    var unpadded = value.toByteArray
    // remove bigint's additional zero byte
    if (unpadded.head == 0) {
      unpadded = unpadded.slice(1, unpadded.length)
    }
    // println(s"unpadded length is: ${unpadded.length} with value $value")
    val padding = Array[Byte]().padTo(descriptor.byteLength - unpadded.length, 0.toByte)
    padding ++ unpadded
  }

}
