package com.obecto.gattakka.genetics

import scala.collection.mutable

/**
  * Created by gbarn_000 on 7/25/2017.
  */
class Chromosome(from: Array[Byte] = Array()) extends GeneGroup{

  var genes: List[Gene] = List(IntegerGene(32), IntegerGene(32))
  val length: Int = getLength

  def value: Any = null

  if (from.nonEmpty && from.length >= length / 8) {
    fromByteArray(from)
  }

  def getLength: Int = {
    var length = 0
    genes foreach {
      length += _.length
    }
    length
  }

  private def fromByteArray(from: Array[Byte]): List[Gene] = {
    var sumOfGeneLengths = 0
    genes = genes map {
      gene =>
        val cutByteArrayForOneGene = from.slice(sumOfGeneLengths, sumOfGeneLengths + gene.length / 8)
        sumOfGeneLengths += cutByteArrayForOneGene.length

        IntegerGene(gene.length, cutByteArrayForOneGene)
    }
    genes
  }

  def toByteArray: Array[Byte] = {
    val byteBuffer = mutable.ArrayBuffer[Byte]()
    genes foreach {
      gene =>
        byteBuffer ++= gene.toByteArray
    }
    byteBuffer.toArray
  }


  def MD5HashStructure: String = {
    var structureMD5Hash = ""
    genes foreach {
      gene =>
        structureMD5Hash += gene.MD5HashStructure
    }
    structureMD5Hash
  }
}
