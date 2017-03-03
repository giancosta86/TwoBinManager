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

package info.gianlucacosta.twobinmanager.server.actors

import java.io.{BufferedReader, StringReader}
import java.time.LocalDateTime
import javafx.collections.ObservableList

import akka.actor.Actor
import info.gianlucacosta.twobinmanager.sdk.server.messages.solutions.{SolutionsUploadRequest, SolutionsUploadResult}
import info.gianlucacosta.twobinpack.core.{Problem, ProblemBundle, Solution}
import info.gianlucacosta.twobinpack.io.csv.v2.SolutionCsvReader2
import info.gianlucacosta.twobinpack.io.repositories.{ProblemRepository, SolutionRepository}

import scala.annotation.tailrec
import scalafx.application.Platform

/**
  * Actor dealing with solutions upload requests.
  *
  * @param problemRepository  The problem repository, used to deserialize solutions
  * @param solutionRepository The solution repository, used to save solutions
  * @param problemBundle      The problem bundle currently served by TwoBinManager
  * @param solutionsUploads   An observable collections containing all the solutions uploads
  * @param autoAccept         True if the actor should automatically save solutions to the solution repository,
  *                           in lieu of updating the solutionsUploads collection
  */
class SolutionsUploadActor(
                            problemRepository: ProblemRepository,
                            solutionRepository: SolutionRepository,
                            problemBundle: ProblemBundle,
                            solutionsUploads: ObservableList[SolutionsUpload],
                            autoAccept: Boolean
                          ) extends Actor {
  private val problemSet: Set[Problem] =
    problemBundle
      .problems
      .toSet


  override def receive: Receive = {
    case SolutionsUploadRequest(requestMessageId, solutionsCsv) =>
      val response =
        try {
          val solutions =
            deserializeSolutions(solutionsCsv)

          require(
            solutions.nonEmpty,
            "No solutions uploaded"
          )

          val solverOptions =
            solutions
              .map(_.solverOption)
              .toSet

          require(
            solverOptions.size == 1,
            "All the solutions must reference the same solver"
          )

          val solverOption =
            solverOptions.head


          val solutionProblems =
            solutions
              .map(_.problem)
              .toSet

          require(
            solutionProblems == problemSet,
            "The solutions must reference all and only the problems in the problem bundle"
          )


          if (autoAccept) {
            solutions.foreach(solutionRepository.add)
          } else {
            val solutionsUpload =
              SolutionsUpload(
                requestMessageId,
                solverOption,
                solutions,
                sender().path.address.hostPort,
                LocalDateTime.now
              )

            Platform.runLater {
              solutionsUploads.add(solutionsUpload)
            }
          }


          SolutionsUploadResult(
            requestMessageId,
            None
          )
        } catch {
          case ex: Exception =>
            SolutionsUploadResult(
              requestMessageId,
              Some(ex.getMessage)
            )
        }

      sender() ! response

    case _ =>
      ()
  }


  private def deserializeSolutions(solutionsCsv: String): Iterable[Solution] = {
    val solutionsReader =
      new SolutionCsvReader2(
        new BufferedReader(
          new StringReader(
            solutionsCsv
          )
        ),

        problemRepository
      )

    try {
      deserializeSolutions(
        List(),
        solutionsReader
      )
    } finally {
      solutionsReader.close()
    }
  }


  @tailrec
  private def deserializeSolutions(cumulatedSolutions: List[Solution], solutionsReader: SolutionCsvReader2): List[Solution] = {
    val nextSolutionOption =
      solutionsReader.readSolution()


    nextSolutionOption match {
      case Some(nextSolution) =>
        deserializeSolutions(
          nextSolution :: cumulatedSolutions,
          solutionsReader
        )

      case None =>
        cumulatedSolutions.reverse
    }
  }
}
