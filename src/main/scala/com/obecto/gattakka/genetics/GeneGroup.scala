package com.obecto.gattakka.genetics

/**
  * Created by gbarn_000 on 7/26/2017.
  */
trait GeneGroup extends Gene {
  def genes: Seq[Gene]

  def length: Int

  def toByteArray: Array[Byte]
  
  def MD5HashStructure: String
}
