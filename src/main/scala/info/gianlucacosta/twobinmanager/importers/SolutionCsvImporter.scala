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

import java.io.{BufferedReader, File}

import info.gianlucacosta.twobinpack.io.FileExtensions
import info.gianlucacosta.twobinpack.io.csv.SolutionCsvReader
import info.gianlucacosta.twobinpack.io.repositories.ProblemRepository

/**
  * Imports one or more solutions from a dedicated CSV file - according to version 1 of the document format
  */
class SolutionCsvImporter extends SolutionCsvImporterBase[SolutionCsvReader] {
  override protected def createSolutionsReader(sourceReader: BufferedReader, problemRepository: ProblemRepository): SolutionCsvReader =
    new SolutionCsvReader(
      sourceReader,
      problemRepository
    )


  override protected def skipToNextSolution(solutionsReader: SolutionCsvReader): Unit = {
    //Skip solver line
    solutionsReader.readLine()

    skipAnchoredBlockLines(solutionsReader)
  }


  override def canImport(file: File): Boolean =
    file.getName.toLowerCase.endsWith(FileExtensions.CsvSolutionFile)
}
