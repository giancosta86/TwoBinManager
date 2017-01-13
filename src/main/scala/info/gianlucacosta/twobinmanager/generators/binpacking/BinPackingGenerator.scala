/*^
  ===========================================================================
  TwoBinManager
  ===========================================================================
  Copyright (C) 2016-2017 Gianluca Costa
  ===========================================================================
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/gpl-3.0.html>.
  ===========================================================================
*/

package info.gianlucacosta.twobinmanager.generators.binpacking

import javafx.stage.Stage

import info.gianlucacosta.twobinmanager.sdk.generators.ProblemGenerator
import info.gianlucacosta.twobinpack.io.repositories.ProblemRepository


object BinPackingGenerator {
  val Name: String =
    "Bin packing random generator"
}


class BinPackingGenerator extends ProblemGenerator {
  override def generate(previousStage: Stage, problemRepository: ProblemRepository): Unit = {
    val binPackingStage =
      new BinPackingStage(previousStage, problemRepository)

    binPackingStage.show()
  }

  override def name: String =
    BinPackingGenerator.Name
}
