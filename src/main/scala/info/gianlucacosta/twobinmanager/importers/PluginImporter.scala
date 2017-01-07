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

import java.io.File
import java.nio.file.{FileAlreadyExistsException, Files, Paths}

import info.gianlucacosta.twobinmanager.App
import info.gianlucacosta.twobinmanager.sdk.importers.Importer
import info.gianlucacosta.twobinmanager.sdk.io.OutputWriter
import info.gianlucacosta.twobinpack.io.repositories.{ProblemRepository, SolutionRepository}

/**
  * Imports plugin JAR files
  */
class PluginImporter extends Importer {
  override def canImport(file: File): Boolean =
    file.getName.toLowerCase.endsWith(".jar")

  override def importFile(
                           file: File,
                           outputWriter: OutputWriter,
                           problemRepository: ProblemRepository,
                           solutionRepository: SolutionRepository
                         ): Unit = {
    outputWriter.printSubHeader(s"Importing plugin JAR...")
    outputWriter.println()

    val sourcePath =
      file.toPath

    App.PluginsDirectory.mkdirs()

    val targetPath =
      Paths.get(
        App.PluginsDirectory.toString,
        file.getName
      )


    try {
      Files.copy(sourcePath, targetPath)

      outputWriter.printOutcome("Plugin successfully imported")
    } catch {
      case ex: FileAlreadyExistsException =>
        outputWriter.printOutcome(s"The file '${targetPath}' already exists, so it will not be overwritten!")
      case ex: Exception =>
        outputWriter.printException(ex)
    }
  }
}
