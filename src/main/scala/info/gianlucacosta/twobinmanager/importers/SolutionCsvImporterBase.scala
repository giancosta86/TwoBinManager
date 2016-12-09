/*^
  ===========================================================================
  TwoBinManager
  ===========================================================================
  Copyright (C) 2016 Gianluca Costa
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

import info.gianlucacosta.twobinmanager.sdk.importers.SolutionImporter
import info.gianlucacosta.twobinmanager.sdk.io.OutputWriter
import info.gianlucacosta.twobinpack.core.Solution
import info.gianlucacosta.twobinpack.io.MissingProblemException
import info.gianlucacosta.twobinpack.io.csv.SolutionCsvReaderBase
import info.gianlucacosta.twobinpack.io.repositories.ProblemRepository

import scala.annotation.tailrec

/**
  * Base class for importing solutions from CSV files
  */
abstract class SolutionCsvImporterBase[TSolutionsReader <: SolutionCsvReaderBase] extends SolutionImporter {
  protected def createSolutionsReader(sourceReader: BufferedReader, problemRepository: ProblemRepository): TSolutionsReader

  protected override def readSolutions(
                                        file: File,
                                        outputWriter: OutputWriter,
                                        problemRepository: ProblemRepository
                                      ): Set[Solution] = {
    val sourceReader =
      new BufferedReader(
        new FileReader(file)
      )


    val solutionsReader =
      createSolutionsReader(
        sourceReader,
        problemRepository
      )

    readSolutions(
      Set[Solution](),
      solutionsReader,
      outputWriter
    )
  }


  private def readSolutions(
                             cumulatedSolutions: Set[Solution],
                             solutionsReader: TSolutionsReader,
                             outputWriter: OutputWriter
                           ): Set[Solution] = {


    try {
      val nextSolutionOption: Option[Solution] =
        solutionsReader.readSolution()

      nextSolutionOption match {
        case Some(nextSolution) =>
          readSolutions(
            cumulatedSolutions + nextSolution,
            solutionsReader,
            outputWriter
          )

        case None =>
          cumulatedSolutions
      }
    } catch {
      case ex: MissingProblemException =>
        outputWriter.printOutcome(ex.getMessage)

        skipToNextSolution(solutionsReader)

        readSolutions(
          cumulatedSolutions,
          solutionsReader,
          outputWriter
        )


      case ex: Exception =>
        outputWriter.printException(ex)

        cumulatedSolutions
    }
  }


  protected def skipToNextSolution(solutionsReader: TSolutionsReader): Unit


  @tailrec
  protected final def skipAnchoredBlockLines(solutionsReader: TSolutionsReader): Unit = {
    val blockLine =
      solutionsReader.readLine()

    if (blockLine != null && blockLine.trim.nonEmpty)
      skipAnchoredBlockLines(solutionsReader)
  }
}
