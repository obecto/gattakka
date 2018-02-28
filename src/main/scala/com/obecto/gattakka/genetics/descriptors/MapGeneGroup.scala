package com.obecto.gattakka.genetics.descriptors

object MapGeneGroupDescriptor {
  type GroupField = Tuple2[Any, GeneDescriptor]

  def apply(tag: String, geneDescriptors: GroupField*): MapGeneGroupDescriptor = {
    MapGeneGroupDescriptor(geneDescriptors.toVector, tag)
  }

  def apply(geneDescriptors: GroupField*): MapGeneGroupDescriptor = {
    MapGeneGroupDescriptor(geneDescriptors.toVector)
  }
}

case class MapGeneGroupDescriptor(val geneDescriptors: Seq[Tuple2[Any, GeneDescriptor]], val tag: String = "") extends GeneDescriptor {

  val length = geneDescriptors.foldLeft(0)(_ + _._2.byteLength * 8)

  def apply(rnd: scala.util.Random): MapGeneGroup = {
    new MapGeneGroup(geneDescriptors.map(_._2.apply(rnd).asInstanceOf[Gene]), this)
  }

  def apply(byteArray: Array[Byte]): MapGeneGroup = {
    var sliceStart = 0
    val genes = geneDescriptors.map({field =>
      val descriptor = field._2
      val slice = byteArray.slice(sliceStart / 8, (sliceStart + descriptor.length) / 8)
      sliceStart += descriptor.length

      descriptor.apply(slice)
    })

    new MapGeneGroup(genes, this)
  }
}

case class MapGeneGroup(genes: Seq[Gene], descriptor: MapGeneGroupDescriptor) extends Gene {
  val value: Map[Any, Any] = (genes zip descriptor.geneDescriptors).map(x => x._2._1 -> x._1.value).toMap

  def toByteArray: Array[Byte] = {
    genes.view.flatMap(gene => gene.toByteArray).toArray
  }
}
