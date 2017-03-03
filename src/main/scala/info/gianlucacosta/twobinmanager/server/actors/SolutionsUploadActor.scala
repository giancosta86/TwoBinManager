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
