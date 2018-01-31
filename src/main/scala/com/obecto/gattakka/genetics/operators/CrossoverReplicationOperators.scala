package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Chromosome}
import com.obecto.gattakka.{PipelineOperator}
import scala.collection.mutable.ArrayBuilder

trait PointCrossoverReplicationOperator extends PipelineOperator with ChromosomeReplicationBaseOperator {

  def parentCount: Int = 2
  def childCount: Int = 2
  def pointCount: Int

  def apply(parent1: Chromosome, parent2: Chromosome): List[Chromosome] = {
    var builders = (ArrayBuilder.make[Byte], ArrayBuilder.make[Byte])
    val minSize = math.min(parent1.byteArray.size, parent2.byteArray.size)
    val points = (0 to pointCount).map(_ => rnd.nextInt(minSize)).sorted.distinct
    var previous = 0
    points foreach { point =>
      builders._1 ++= parent1.byteArray.slice(previous, point)
      builders._2 ++= parent2.byteArray.slice(previous, point)
      builders = builders.swap
      previous = point
    }
    builders._1 ++= parent1.byteArray.slice(previous, parent1.byteArray.size)
    builders._2 ++= parent2.byteArray.slice(previous, parent2.byteArray.size)
    List(builders._1, builders._2).map(builder => new Chromosome(builder.result, parent1.descriptor))
  }

}

trait UniformCrossoverReplicationOperator extends PipelineOperator with ChromosomeReplicationBaseOperator {

  def parentCount: Int = 2
  def childCount: Int = 2
  def flipChance: Double = 0.5

  def apply(parent1: Chromosome, parent2: Chromosome): List[Chromosome] = {
    var builders = (ArrayBuilder.make[Byte], ArrayBuilder.make[Byte])
    val minSize = math.min(parent1.byteArray.size, parent2.byteArray.size)
    var previous = 0
    (0 to minSize) foreach { index =>
      if (rnd.nextFloat() < flipChance) {
        builders._1 ++= parent1.byteArray.slice(previous, index)
        builders._2 ++= parent2.byteArray.slice(previous, index)
        builders = builders.swap
        previous = index
      }
    }
    builders._1 ++= parent1.byteArray.slice(previous, parent1.byteArray.size)
    builders._2 ++= parent2.byteArray.slice(previous, parent2.byteArray.size)
    List(builders._1, builders._2).map(builder => new Chromosome(builder.result, parent1.descriptor))
  }

}
