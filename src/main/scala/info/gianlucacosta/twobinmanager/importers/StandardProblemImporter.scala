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

package info.gianlucacosta.twobinmanager.importers

import java.io.{BufferedReader, File, FileReader}
import java.time.Duration
import java.util.UUID
import java.util.concurrent.Semaphore

import info.gianlucacosta.helios.fx.dialogs.InputDialogs
import info.gianlucacosta.twobinmanager.AppDb
import info.gianlucacosta.twobinmanager.sdk.importers.ProblemImporter
import info.gianlucacosta.twobinmanager.sdk.io.OutputWriter
import info.gianlucacosta.twobinpack.core.{FrameMode, FrameTemplate, Problem}
import info.gianlucacosta.twobinpack.io.standard.StandardProblemReader
import info.gianlucacosta.twobinpack.io.{FileExtensions, ProblemNameDialogs}

import scalafx.application.Platform

/**
  * Imports a standard problem
  */
class StandardProblemImporter extends ProblemImporter {
  override protected def readProblems(file: File, outputWriter: OutputWriter): Set[Problem] = {
    val standardProblemReader =
      new StandardProblemReader(new BufferedReader(new FileReader(file)))


    try {
      val standardProblem =
        standardProblemReader.readStandardProblem()

      val problemDialogHeader =
        s"Import '${file.getName}'..."


      val id =
        UUID.randomUUID()

      val defaultProblemName =
        file.getName.splitAt(file.getName.lastIndexOf("."))._1


      val guiInputReadySemaphore =
        new Semaphore(0)

      var problemOption: Option[Problem] =
        None


      Platform.runLater {
        val problemNameDialogs =
          new ProblemNameDialogs(AppDb.problemRepository)

        val nameOption =
          problemNameDialogs.askForNewProblemName(
            problemDialogHeader,
            defaultProblemName
          )


        problemOption =
          nameOption.flatMap(name => {
            val frameModeOption =
              InputDialogs.askForItem(
                "Frame mode:",
                FrameMode.All,
                Some(FrameMode.Strip),
                header = problemDialogHeader
              )

            frameModeOption.flatMap(frameMode => {
              val canRotateBlocksOption: Option[Boolean] =
                InputDialogs.askYesNoCancel(
                  "Can the user rotate blocks?",
                  header = problemDialogHeader
                )

              canRotateBlocksOption.flatMap(canRotateBlocks => {
                val resolutionOption: Option[Long] =
                  InputDialogs.askForLong(
                    "Resolution (pixels per  ):",
                    Problem.SuggestedResolution,
                    Problem.MinResolution,
                    Problem.MaxResolution,
                    header = problemDialogHeader
                  )

                resolutionOption.flatMap(resolution => {
                  val timeLimitInMinutesOption =
                    InputDialogs.askForLong(
                      "Time limit in minutes (0 = unlimited):",
                      5,
                      0,
                      Problem.MaxTimeLimit.toMinutes,
                      header = problemDialogHeader
                    )


                  timeLimitInMinutesOption.map(timeLimitInMinutes => {
                    val actualTimeLimitInMinutesOption =
                      if (timeLimitInMinutes > 0)
                        Some(timeLimitInMinutes.toInt)
                      else
                        None

                    standardProblem.toProblem(
                      id,
                      name,
                      frameMode,
                      FrameTemplate.SuggestedBlockColorsPool,
                      canRotateBlocks,
                      resolution.toInt,
                      actualTimeLimitInMinutesOption.map(actualTimeLimitInMinutes =>
                        Duration.ofMinutes(actualTimeLimitInMinutes)
                      )
                    )
                  })
                })
              })
            })
          })


        guiInputReadySemaphore.release()
      }

      guiInputReadySemaphore.acquire()

      problemOption.map(problem =>
        Set(problem)
      ).getOrElse(
        Set()
      )

    } finally {
      standardProblemReader.close()
    }
  }


  override def canImport(file: File): Boolean =
    file.getName.toLowerCase.endsWith(FileExtensions.StandardProblem)
}
