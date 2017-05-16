package com.obecto.genetics
import scala.math.BigInt

trait Gene[T] extends Serializable {
  var value: T
  def size: Int
  def asBinary: Array[Byte]
  def fromBinary(from: Array[Byte]): Unit
}

object BinaryGene {
  def apply(size: Int, rnd: scala.util.Random = scala.util.Random): BinaryGene = {
    new BinaryGene(BigInt(numbits = size, rnd = scala.util.Random))
  }
}

class BinaryGene(var value: BigInt) extends Gene[BigInt] {
  def size: Int = value.bitLength

  def asBigInt: BigInt = value
  def asInt: Int = value.intValue
  def asFloat: Float = value.floatValue / (1 << size)
  def asDouble: Double = value.doubleValue / (1 << size)

  def asBinary = value.toByteArray
  def fromBinary(from: Array[Byte]): Unit = {
    value = BigInt(from)
  }
}
