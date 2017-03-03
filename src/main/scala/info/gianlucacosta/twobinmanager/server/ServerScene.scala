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

package info.gianlucacosta.twobinmanager.server

import info.gianlucacosta.helios.fx.scene.fxml.FxmlScene
import info.gianlucacosta.twobinpack.core.ProblemBundle
import info.gianlucacosta.twobinpack.io.repositories.{ProblemRepository, SolutionRepository}

import scalafx.Includes._

class ServerScene(
                   problemRepository: ProblemRepository,
                   solutionRepository: SolutionRepository,
                   problemBundle: ProblemBundle
                 ) extends FxmlScene[ServerController, javafx.scene.layout.BorderPane](
  classOf[ServerController]
) {
  override protected def preInitialize(): Unit = {
    controller.problemRepository =
      problemRepository

    controller.solutionRepository =
      solutionRepository

    controller.problemBundleOption() =
      Some(problemBundle)
  }
}


