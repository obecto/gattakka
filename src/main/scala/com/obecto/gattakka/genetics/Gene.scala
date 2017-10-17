package com.obecto.gattakka.genetics

import java.nio.ByteBuffer
import java.security.MessageDigest

import scala.math.BigInt


trait Gene {
  def value: Any

  def length: Int
  def toByteArray: Array[Byte]

  def fromByteArray(from: Array[Byte]): Gene

  def setRandomValue(): Unit

  def MD5HashStructure: String

}

object IntegerGene {
  def apply(length: Int, rnd: scala.util.Random = scala.util.Random): IntegerGene = {
    new IntegerGene(length, BigInt(numbits = length, rnd = scala.util.Random))
  }

  def apply(length: Int, value: BigInt): IntegerGene = {
    new IntegerGene(length, value)
  }

  def apply(length: Int, byteArray: Array[Byte]): IntegerGene = {
    new IntegerGene(length, BigInt(byteArray))
  }
}

case class IntegerGene(length: Int, var value: BigInt) extends Gene {

  val rnd = scala.util.Random
  val maxValue: BigInt = (BigInt(1) << length) - 1
  val maxValueDouble: Double = maxValue.doubleValue

  def scale(minValue: Int, maxValue: Int): Unit = {
    //...
  }
  value = value & maxValue // Zero highest bits, just in case

  def toDouble: Double = value.doubleValue / maxValueDouble

  def toByteArray: Array[Byte] = {
    var unpadded = value.toByteArray
    //remove allocated by scala additional zero byte
    if (unpadded.head == 0) {
      unpadded = unpadded.slice(1, unpadded.length)
    }
    // println(s"unpadded length is: ${unpadded.length} with value $value")
    val padding = Array[Byte]().padTo(length / 8 - unpadded.length, 0.toByte)
    padding ++ unpadded
  }

  def fromByteArray(from: Array[Byte]): Gene = {
    value = BigInt(from)
    this
  }

  def setRandomValue(): Unit = {
    value = BigInt(length, rnd)
  }

  def MD5HashStructure: String = {
    val byteArrayOfType: Array[Byte] = value.getClass.toString.getBytes
    val byteArrayOfLength: Array[Byte] = ByteBuffer.allocate(4).putInt(length).array
    val md5ByteArrayHash = MessageDigest.getInstance("MD5").digest(byteArrayOfLength ++ byteArrayOfType)
    new String(md5ByteArrayHash)
  }

}
