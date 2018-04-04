package com.obecto.gattakka.genetics.operators

import com.obecto.gattakka.IndividualDescriptor

class TournamentSelectionStrategy(tournamentSize: Int, rnd: scala.util.Random = scala.util.Random) extends SelectionStrategy {

  def selectBest(from: Seq[IndividualDescriptor]): IndividualDescriptor = {
    val amount = from.size

    List.fill(tournamentSize) {
      from(rnd.nextInt(amount))
    }.maxBy(_.fitness)
  }

  def selectWorst(from: Seq[IndividualDescriptor]): IndividualDescriptor = {
    val amount = from.size

    List.fill(tournamentSize) {
      from(rnd.nextInt(amount))
    }.minBy(_.fitness)
  }

}
