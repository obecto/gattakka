package com.obecto.gattakka.genetics.descriptors

//import scala.collection.mutable

object GeneGroupDescriptor {
  def apply(tag: String, geneDescriptors: GeneDescriptor*): GeneGroupDescriptor = {
    GeneGroupDescriptor(geneDescriptors.toList, tag)
  }
  def apply(geneDescriptors: GeneDescriptor*): GeneGroupDescriptor = {
    GeneGroupDescriptor(geneDescriptors.toList)
  }
}

case class GeneGroupDescriptor(geneDescriptors: List[GeneDescriptor], tag: String = "") extends GeneDescriptor {

  val length = geneDescriptors.foldLeft(0)(_ + _.byteLength * 8)

  def apply(rnd: scala.util.Random): GeneGroup = {
    new GeneGroup(geneDescriptors.map(_.apply(rnd).asInstanceOf[Gene]), this)
  }

  def apply(byteArray: Array[Byte]): GeneGroup = {
    var sliceStart = 0
    val genes = geneDescriptors.map({descriptor =>
      val slice = byteArray.slice(sliceStart, sliceStart + descriptor.byteLength)
      sliceStart += descriptor.byteLength

      descriptor.apply(slice).asInstanceOf[Gene]
    })

    new GeneGroup(genes, this)
  }
}

case class GeneGroup(genes: List[Gene], descriptor: GeneGroupDescriptor) extends Gene {
  val value: List[Any] = genes.map(_.value)

  def toByteArray: Array[Byte] = {
    genes.view.flatMap(gene => gene.toByteArray).toArray
  }
}
