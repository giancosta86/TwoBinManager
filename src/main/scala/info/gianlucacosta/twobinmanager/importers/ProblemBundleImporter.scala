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

import info.gianlucacosta.twobinmanager.sdk.importers.ProblemImporter
import info.gianlucacosta.twobinmanager.sdk.io.OutputWriter
import info.gianlucacosta.twobinpack.core.Problem
import info.gianlucacosta.twobinpack.io.FileExtensions
import info.gianlucacosta.twobinpack.io.bundle.ProblemBundleReader

/**
  * Imports problem bundles
  */
class ProblemBundleImporter extends ProblemImporter {
  override protected def readProblems(file: File, outputWriter: OutputWriter): Set[Problem] = {
    val problemBundleReader =
      new ProblemBundleReader(new BufferedReader(new FileReader(file)))

    try {
      val problemBundle =
        problemBundleReader.readProblemBundle()

      problemBundle.problems.toSet
    } finally {
      problemBundleReader.close()
    }

  }

  override def canImport(file: File): Boolean =
    file.getName.toLowerCase.endsWith(FileExtensions.ProblemBundle)
}
