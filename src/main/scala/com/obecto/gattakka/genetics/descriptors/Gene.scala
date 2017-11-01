package com.obecto.gattakka.genetics.descriptors

trait Gene {
  /** The value of the gene */
  def value: Any

  /** Returns the gene converted to a byte array */
  def toByteArray: Array[Byte]

  /** Returns the descriptor used to describe this gene */
  def descriptor: GeneDescriptor
}
