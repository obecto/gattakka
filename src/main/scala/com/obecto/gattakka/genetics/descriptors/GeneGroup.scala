package com.obecto.gattakka.genetics.descriptors

//import scala.collection.mutable

object GeneGroupDescriptor {
  def apply(geneDescriptors: List[GeneDescriptor]): GeneGroupDescriptor = {
    new GeneGroupDescriptor(geneDescriptors)
  }
  def apply(geneDescriptors: GeneDescriptor*): GeneGroupDescriptor = {
    new GeneGroupDescriptor(geneDescriptors.toList)
  }
}

case class GeneGroupDescriptor(geneDescriptors: List[GeneDescriptor]) extends GeneDescriptor {

  val length = geneDescriptors.foldLeft(0)(_ + _.length)

  def apply(rnd: scala.util.Random): GeneGroup = {
    new GeneGroup(geneDescriptors.map(_.apply(rnd).asInstanceOf[Gene]), this)
  }

  def apply(byteArray: Array[Byte]): GeneGroup = {
    var sliceStart = 0
    val genes = geneDescriptors.map({descriptor =>
      val slice = byteArray.slice(sliceStart / 8, (sliceStart + descriptor.length) / 8)
      sliceStart += descriptor.length

      descriptor.apply(slice).asInstanceOf[Gene]
    })

    new GeneGroup(genes, this)
  }
}

case class GeneGroup(genes: List[Gene], descriptor: GeneDescriptor) extends Gene {
  val value: List[Any] = genes.map(_.value)

  def toByteArray: Array[Byte] = {
    genes.toVector.flatMap(gene => gene.toByteArray).toArray
  }
}
