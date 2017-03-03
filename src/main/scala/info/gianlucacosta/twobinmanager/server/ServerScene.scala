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


