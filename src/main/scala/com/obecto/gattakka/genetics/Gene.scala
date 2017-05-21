package com.obecto.genetics
import scala.math.BigInt

trait Gene[T] extends Serializable {
  var value: T
  def size: Int
  def asBinary: Array[Byte]
  def fromBinary(from: Array[Byte]): Gene[T]
}

object BinaryGene {
  def apply(size: Int, rnd: scala.util.Random = scala.util.Random): BinaryGene = {
    new BinaryGene(size, BigInt(numbits = size, rnd = scala.util.Random))
  }
}

class BinaryGene(val size: Int, var value: BigInt) extends Gene[BigInt] {

  assert(size % 8 == 0, s"Size must be divisible by 8 (given: $size)")

  def asBigInt: BigInt = value
  def asInt: Int = value.intValue
  def asFloat: Float = value.floatValue / ((1 << size) - 1)
  def asDouble: Double = value.doubleValue / ((1 << size) - 1)

  def asBinary: Array[Byte] = {
    val unpadded = value.toByteArray
    val padding = Array[Byte]().padTo(size / 8 - unpadded.length, 0.toByte)
    padding ++ unpadded
  }
  def fromBinary(from: Array[Byte]): BinaryGene = new BinaryGene(from.length * 8, BigInt(from))
}
