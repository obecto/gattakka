package com.obecto.gattakka.genetics

import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor, Gene}

case class Chromosome(byteArray: Array[Byte], descriptor: GeneDescriptor) {
  def value: Any = descriptor.apply(byteArray).value
  def toGene: Gene = descriptor.apply(byteArray)
}
