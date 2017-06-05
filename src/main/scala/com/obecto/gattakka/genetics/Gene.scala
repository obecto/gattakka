package com.obecto.gattakka.genetics
import scala.math.BigInt

trait Gene[T] extends Serializable {
  var value: T
  def size: Int
  def toByteArray: Array[Byte]
  // fromSize is the "parent" gene's size. It should be expected to remain constant
  def fromByteArray(from: Array[Byte], fromSize: Int = size): Gene[T]
}

object BinaryGene {
  def apply(size: Int, rnd: scala.util.Random = scala.util.Random): BinaryGene = {
    new BinaryGene(size, BigInt(numbits = size, rnd = scala.util.Random))
  }
}

class BinaryGene(val size: Int, var value: BigInt) extends Gene[BigInt] {

  val maxValue = (BigInt(1) << size) - 1
  val maxValueDouble = maxValue.doubleValue

  value = value & maxValue // Zero highest bits, just in case


  def toBigInt: BigInt = value
  def asInt: Int = value.intValue
  def toFloat: Float = toDouble.toFloat
  def toDouble: Double = (value.doubleValue / maxValueDouble)

  def toByteArray: Array[Byte] = {
    val unpadded = value.toByteArray
    val padding = Array[Byte]().padTo(size / 8 - unpadded.length, 0.toByte)
    padding ++ unpadded
  }
  def fromByteArray(from: Array[Byte], fromSize: Int = size): BinaryGene = new BinaryGene(fromSize, BigInt(from))
}
