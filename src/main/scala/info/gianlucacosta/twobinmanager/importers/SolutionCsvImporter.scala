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
import info.gianlucacosta.twobinpack.io.csv.SolutionCsvReader
import info.gianlucacosta.twobinpack.io.repositories.ProblemRepository
import info.gianlucacosta.twobinpack.io.{FileExtensions, MissingProblemException}

import scala.annotation.tailrec

/**
  * Imports one or more solutions from a dedicated CSV file
  */
class SolutionCsvImporter extends SolutionImporter {
  protected override def readSolutions(
                                        file: File,
                                        outputWriter: OutputWriter,
                                        problemRepository: ProblemRepository
                                      ): Set[Solution] = {
    val solutionsReader =
      new SolutionCsvReader(
        new BufferedReader(
          new FileReader(file)
        ),

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
                             solutionsReader: SolutionCsvReader,
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

        moveToNextSolution(solutionsReader)

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


  private def moveToNextSolution(solutionsReader: SolutionCsvReader): Unit = {
    //Skip solver line
    solutionsReader.readLine()

    skipBlockLines(solutionsReader)
  }


  @tailrec
  private def skipBlockLines(solutionsReader: SolutionCsvReader): Unit = {
    val blockLine =
      solutionsReader.readLine()

    if (blockLine != null && blockLine.trim.nonEmpty)
      skipBlockLines(solutionsReader)
  }


  override def canImport(file: File): Boolean =
    file.getName.toLowerCase.endsWith(FileExtensions.CsvSolutionFile)
}
