package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.genetics.{Genome, Chromosome}
import com.obecto.gattakka.{PipelineOperator, IndividualDescriptor, Population}
import scala.util.Random
import scala.collection.mutable.ListBuffer

trait ReplicationBaseOperator extends PipelineOperator {

  def rnd: Random = Random
  def replicationChance: Double
  def parentSelectionStrategy: SelectionStrategy
  def keepFirstChildOnly: Boolean = false

  def parentCount: Int
  def apply(parents: Seq[Genome]): Traversable[Genome]

  def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    val createdIndividuals = ListBuffer.empty[IndividualDescriptor]
    val withoutDoomed = snapshot filterNot (_.doomedToDie)
    var filteredParents = withoutDoomed

    // while (rnd.nextFloat() < 1 - math.pow(1 - replicationChance, filteredParents.size.toDouble)) {
    for (i <- 0 to (withoutDoomed.size * replicationChance / parentCount + rnd.nextFloat()).round.toInt) {
      val parents = for (i <- 0 to parentCount) yield {
        val parent = parentSelectionStrategy.selectBest(filteredParents)
        filteredParents = filteredParents filterNot (_ equals parent)
        parent
      }

      var childrenGenomes = apply(parents.view.map(_.genome).toSeq).toList

      if (keepFirstChildOnly) {
        childrenGenomes = List(childrenGenomes.head)
      }

      createdIndividuals ++= childrenGenomes.map (IndividualDescriptor(Population.getUniqueBotId, _, None))
    }

    snapshot ++ createdIndividuals
  }

}


trait ChromosomeReplicationBaseOperator extends PipelineOperator with ReplicationBaseOperator {

  def mixParents: Boolean = false

  def apply(parent1: Chromosome, parent2: Chromosome): List[Chromosome]
  def childCount: Int

  def apply(parents: Seq[Genome]): List[Genome] = {
    def helper(chromosome: Chromosome, matchWith: List[Chromosome]): List[Chromosome] = {
      val matches = matchWith.filter (_.descriptor == chromosome.descriptor)
      if (matches.isEmpty) {
        List.fill(childCount)(chromosome)
      } else {
        apply(chromosome, matches(rnd.nextInt(matches.size)))
      }
    }

    if (mixParents) {
      var chromosomeLists = (parents map (_.chromosomes)).toVector
      val maxSize = chromosomeLists.view.map(_.size).max

      val newChromosomes = (0 to maxSize).view.map({ i =>
        val selected = rnd.nextInt(i)
        val result = helper(
          chromosomeLists(selected).head,
          chromosomeLists.view.filter(_ != chromosomeLists(selected)).flatten.toList
        )
        chromosomeLists = chromosomeLists map (_.tail) filterNot (_.isEmpty)
        result
      }).toList

      newChromosomes.transpose.map(new Genome(_))
    } else {
      parents.head.chromosomes
        .map(helper(_, parents.tail.map(_.chromosomes).flatten.toList))
        .transpose
        .map(new Genome(_))
    }

  }

}
