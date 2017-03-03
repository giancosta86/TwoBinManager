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

package info.gianlucacosta.twobinmanager.main

import java.io.{File, FileWriter}
import java.time.Duration
import javafx.fxml.FXML
import javafx.stage.Stage

import info.gianlucacosta.helios.Includes._
import info.gianlucacosta.helios.apps.AppInfo
import info.gianlucacosta.helios.desktop.DesktopUtils
import info.gianlucacosta.helios.fx.Includes._
import info.gianlucacosta.helios.fx.dialogs.about.AboutBox
import info.gianlucacosta.helios.fx.dialogs.{Alerts, BusyDialog, InputDialogs}
import info.gianlucacosta.twobinmanager.analytics.AnalyticsStage
import info.gianlucacosta.twobinmanager.importers.ImportService
import info.gianlucacosta.twobinmanager.sdk.analytics.ChartRetriever
import info.gianlucacosta.twobinmanager.server.ServerStage
import info.gianlucacosta.twobinmanager.{App, ProblemViewStage, SolutionsViewStage}
import info.gianlucacosta.twobinpack.core.{Problem, ProblemBundle, Solution, StandardProblem}
import info.gianlucacosta.twobinpack.io.bundle.ProblemBundleWriter
import info.gianlucacosta.twobinpack.io.csv.SolutionCsvWriter
import info.gianlucacosta.twobinpack.io.repositories.{ProblemRepository, SolutionRepository}
import info.gianlucacosta.twobinpack.io.standard.StandardProblemWriter
import info.gianlucacosta.twobinpack.io.{FileExtensions, ProblemNameDialogs}

import scala.annotation.tailrec
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.input.{DragEvent, TransferMode}
import scalafx.stage.FileChooser

/**
  * Controller for the main scene, which resides in the primary stage
  */
class MainController {
  var appInfo: AppInfo = _
  var stage: Stage = _
  var problemRepository: ProblemRepository = _
  var solutionRepository: SolutionRepository = _

  private lazy val problemNameDialogs: ProblemNameDialogs =
    new ProblemNameDialogs(problemRepository)


  private lazy val aboutBox: AboutBox =
    new AboutBox(appInfo)


  private val csvSolutionsFileChooser = new FileChooser {
    extensionFilters.setAll(
      new FileChooser.ExtensionFilter("Solution file", s"*${FileExtensions.CsvSolutionFile}")
    )

    title =
      "Export solutions as CSV..."
  }


  private val standardProblemFileChooser = new FileChooser {
    extensionFilters.setAll(
      new FileChooser.ExtensionFilter("Standard problem", s"*${FileExtensions.StandardProblem}")
    )

    title =
      "Export as standard problem..."
  }


  private val problemBundleFileChooser = new FileChooser {
    extensionFilters.setAll(
      new FileChooser.ExtensionFilter("Problem bundle", s"*${FileExtensions.ProblemBundle_v2}")
    )

    title =
      "Create problem bundle..."
  }

  @FXML
  def initialize(): Unit = {
    titleLabel.text =
      appInfo.name


    importLabel.onDragOver =
      (event: DragEvent) => {
        if (event.dragboard.hasFiles) {
          event.acceptTransferModes(TransferMode.Any: _*)
        }

        event.consume()
      }


    importLabel.onDragDropped =
      (event: DragEvent) => {
        if (event.dragboard.files != null) {
          val filesToImport: Iterable[File] =
            event.dragboard.files

          new BusyDialog(stage, "Importing files...") {
            run {
              val problemsBeforeImport =
                problemRepository.count()

              val solutionsBeforeImport =
                solutionRepository.count()


              val importLogString =
                ImportService.importFiles(filesToImport)


              val problemsAfterImport =
                problemRepository.count()

              val solutionsAfterImport =
                solutionRepository.count()


              val importedProblems =
                problemsAfterImport - problemsBeforeImport

              val importedSolutions =
                solutionsAfterImport - solutionsBeforeImport


              Platform.runLater {
                new ImportOutcomeAlert(
                  stage,
                  importedProblems,
                  importedSolutions,
                  importLogString
                ).showAndWait()
              }
            }
          }
        }
      }
  }


  @FXML
  def newProblem(): Unit = {
    val problemGeneratorOption =
      InputDialogs.askForItem(
        "Problem generator:",
        App.PluginsRegistry.problemGenerators,
        header = "New problem..."
      )

    problemGeneratorOption.foreach(problemGenerator => {
      problemGenerator.generate(stage, problemRepository)
    })
  }


  @FXML
  def viewProblem(): Unit = {
    val problemNameOption =
      problemNameDialogs.askForExistingProblemName("View problem...")

    problemNameOption.foreach(problemName => {
      val problem =
        problemRepository.findByName(problemName).get

      val problemViewStage =
        new ProblemViewStage(
          appInfo,
          stage,
          problem
        )

      problemViewStage.show()
    })
  }


  @FXML
  def editProblem(): Unit = {
    val problemNameOption =
      problemNameDialogs.askForExistingProblemName("Edit problem...")

    problemNameOption.foreach(problemName => {
      val problem =
        problemRepository.findByName(problemName).get

      val newProblemNameOption =
        problemNameDialogs.askForNewProblemName(problem, "Edit problem...")

      newProblemNameOption.foreach(newProblemName => {
        val newTimeLimitInMinutesOption =
          askForNewTimeLimitInMinutes(problem)

        newTimeLimitInMinutesOption.foreach(newTimeLimitInMinutes => {
          val resolutionOption =
            InputDialogs.askForLong(
              "Resolution (pixels per unit):",
              problem.frameTemplate.resolution,
              Problem.MinResolution,
              Problem.MaxResolution
            )

          resolutionOption.foreach(resolution => {
            val newProblem =
              problem.copy(
                name =
                  newProblemName,

                frameTemplate =
                  problem.frameTemplate.copy(
                    resolution =
                      resolution.toInt
                  ),

                timeLimitOption =
                  if (newTimeLimitInMinutes > 0)
                    Some(
                      Duration.ofMinutes(newTimeLimitInMinutes)
                    )
                  else
                    None
              )

            problemRepository.update(newProblem)

            Alerts.showInfo("The problem has been successfully edited.")
          })
        })
      })
    })
  }

  @tailrec
  private def askForNewTimeLimitInMinutes(problem: Problem): Option[Int] = {
    if (problem.timeLimitOption.isEmpty)
      Some(0)
    else {
      val requestedMinutesOption =
        InputDialogs.askForLong(
          "Time limit in minutes (0 = no limit):",
          problem.timeLimitOption
            .map(_.toMinutes)
            .getOrElse(0),
          0,
          Problem.MaxTimeLimit.toMinutes
        ).map(_.toInt)


      requestedMinutesOption match {
        case Some(requestedMinutes) =>
          if (requestedMinutes == 0)
            Some(0)
          else {
            val requestedTimeLimit =
              Duration.ofMinutes(requestedMinutes)

            if (requestedTimeLimit < problem.timeLimitOption.get) {
              Alerts.showWarning("The time limit can only be increased or removed!")
              askForNewTimeLimitInMinutes(problem)
            } else
              requestedMinutesOption
          }

        case None =>
          None
      }
    }
  }


  @FXML
  def exportProblemBundle(): Unit = {
    val problemBundleOption =
      askForProblemBundle(problemBundleFileChooser.title())

    problemBundleOption.foreach(problemBundle => {
      problemBundleFileChooser.initialFileName =
        "Problems"

      val problemBundleFile =
        problemBundleFileChooser.smartSave(stage)

      if (problemBundleFile != null) {
        val problemBundleWriter =
          new ProblemBundleWriter(new FileWriter(problemBundleFile))


        try {
          problemBundleWriter.writeProblemBundle(problemBundle)

          Alerts.showInfo("Problem bundle created successfully.")
        } catch {
          case ex: Exception =>
            Alerts.showException(ex, alertType = AlertType.Warning)
        } finally {
          problemBundleWriter.close()
        }
      }
    })
  }


  private def askForProblemBundle(header: String): Option[ProblemBundle] =
    problemNameDialogs
      .askForExistingProblemNames(header)
      .map(problemNames => {
        val problems =
          problemNames.map(problemName =>
            problemRepository.findByName(problemName).get
          )

        ProblemBundle(problems)
      })


  @FXML
  def exportStandardProblem(): Unit = {
    val problemNameToExportOption: Option[String] =
      problemNameDialogs.askForExistingProblemName(standardProblemFileChooser.title())

    problemNameToExportOption.foreach(problemName => {
      val problem =
        problemRepository.findByName(problemName).get

      standardProblemFileChooser.initialFileName =
        problemName

      val targetFile =
        standardProblemFileChooser.smartSave(stage)

      if (targetFile != null) {
        val standardProblem =
          new StandardProblem(problem)

        val standardProblemWriter =
          new StandardProblemWriter(new FileWriter(targetFile))

        try {
          standardProblemWriter.writeStandardProblem(standardProblem)

          Alerts.showInfo("Standard problem exported successfully.")
        } catch {
          case ex: Exception =>
            Alerts.showException(ex, alertType = AlertType.Warning)
        } finally {
          standardProblemWriter.close()
        }
      }
    })
  }


  @FXML
  def removeProblem(): Unit = {
    val problemNameToRemoveOption =
      problemNameDialogs.askForExistingProblemName("Remove problem")

    problemNameToRemoveOption.foreach(problemName => {
      problemRepository.removeByName(problemName)
      Alerts.showInfo("Problem removed successfully.")
    })
  }


  @FXML
  def removeAllProblems(): Unit = {
    if (problemRepository.count() > 0) {
      val canRemoveAllProblems =
        InputDialogs.askYesNoCancel(
          "Are you sure you want to remove all the problems in the database?\n\nRemoving problems will also delete all the related items, such as solutions.",
          "Remove all problems"
        ).contains(true)

      if (canRemoveAllProblems) {
        problemRepository.removeAll()

        Alerts.showInfo("Problems removed successfully.")
      }
    } else {
      problemNameDialogs.showNoProblemsAvailableWarning()
    }
  }


  @FXML
  def viewSolutions(): Unit = {
    val problemNameOption =
      problemNameDialogs.askForExistingProblemName("View problem solutions...")

    problemNameOption.foreach(problemName => {
      val solutions =
        solutionRepository.findAllByProblemName(problemName)

      if (solutions.nonEmpty) {
        val solutionsViewStage =
          new SolutionsViewStage(
            appInfo,
            stage,
            solutions
          )

        solutionsViewStage.show()
      } else {
        Alerts.showWarning("There are no solutions in the database for the selected problem.")
      }
    })
  }


  @FXML
  def exportCsvSolutions(): Unit = {
    val problemNameOption =
      problemNameDialogs.askForExistingProblemName(csvSolutionsFileChooser.title())

    problemNameOption.foreach(problemName => {
      val solutions =
        solutionRepository.findAllByProblemName(problemName)

      if (solutions.nonEmpty) {
        csvSolutionsFileChooser.initialFileName =
          s"Solutions for ${problemName}"

        val solutionFile =
          csvSolutionsFileChooser.smartSave(stage)

        if (solutionFile != null) {
          val solutionWriter =
            new SolutionCsvWriter(new FileWriter(solutionFile))

          try {
            solutions.foreach(solution =>
              solutionWriter.writeSolution(solution)
            )

            Alerts.showInfo("Solutions successfully exported to the CSV file.")
          } catch {
            case ex: Exception =>
              Alerts.showException(ex, alertType = AlertType.Warning)
          } finally {
            solutionWriter.close()
          }
        }
      } else {
        Alerts.showWarning("There are no solutions in the database for the selected problem.")
      }
    })
  }


  @FXML
  def removeProblemSolutions(): Unit = {
    val problemNameOption =
      problemNameDialogs.askForExistingProblemName("Remove solutions for problem")

    problemNameOption.foreach(problemName => {
      solutionRepository.removeByProblemName(problemName)

      Alerts.showInfo(s"Any solution related to problem '${problemName}' was successfully deleted.")
    })
  }


  @FXML
  def showBasicStats(): Unit = {
    Alerts.showInfo(s"Total problems: ${problemRepository.count()}\nTotal solutions: ${solutionRepository.count()}")
  }


  @FXML
  def showAnalytics(): Unit = {
    new BusyDialog(stage, "Retrieving data...", 150) {
      run {
        val allSolutions: Iterable[Solution] =
          solutionRepository.findAll()

        val analyticsDimensions: Map[String, ChartRetriever] =
          App.PluginsRegistry.analyticsDimensions

        Platform.runLater {
          val analyticsStage =
            new AnalyticsStage(
              appInfo,
              stage,
              allSolutions,
              analyticsDimensions
            )

          analyticsStage.show()
        }
      }
    }
  }


  @FXML
  def openPluginsFolder(): Unit = {
    App.PluginsDirectory.mkdirs()

    DesktopUtils.openFile(App.PluginsDirectory)
  }


  @FXML
  def showServerWindow(): Unit = {
    val problemBundleOption =
      askForProblemBundle("Define a problem bundle for the server")

    problemBundleOption.foreach(problemBundle => {
      val serverStage =
        new ServerStage(
          appInfo,
          stage,
          problemRepository,
          solutionRepository,
          problemBundle
        )

      serverStage.show()
    })
  }


  @FXML
  def showAboutBox(): Unit = {
    aboutBox.showAndWait()
  }


  @FXML
  var titleLabel: javafx.scene.control.Label = _


  @FXML
  var importLabel: javafx.scene.control.Label = _
}
