package info.gianlucacosta.twobinmanager.server

import info.gianlucacosta.helios.apps.AppInfo
import info.gianlucacosta.helios.fx.Includes._
import info.gianlucacosta.helios.fx.stage.StackedStage
import info.gianlucacosta.twobinpack.core.ProblemBundle
import info.gianlucacosta.twobinpack.io.repositories.{ProblemRepository, SolutionRepository}

import scalafx.stage.Stage

class ServerStage(
                   appInfo: AppInfo,
                   val previousStage: javafx.stage.Stage,
                   problemRepository: ProblemRepository,
                   solutionRepository: SolutionRepository,
                   problemBundle: ProblemBundle
                 ) extends Stage with StackedStage {
  this.setMainIcon(appInfo)

  scene =
    new ServerScene(
      problemRepository,
      solutionRepository,
      problemBundle
    ) {
      override protected def preInitialize(): Unit = {
        super.preInitialize()

        controller.stage =
          ServerStage.this
      }
    }
}
