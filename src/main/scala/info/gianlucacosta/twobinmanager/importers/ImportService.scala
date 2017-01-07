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

import java.io.{File, StringWriter}

import info.gianlucacosta.twobinmanager.sdk.importers.Importer
import info.gianlucacosta.twobinmanager.sdk.io.OutputWriter
import info.gianlucacosta.twobinmanager.{App, AppDb}

/**
  * Imports one or more files, using the available Importer instances
  */
object ImportService {
  /**
    * Imports the given files - after sorting them by name - and returns
    * the output string created by the dedicated logger.
    *
    * @param files The files to import
    * @return The log string
    */
  def importFiles(files: Iterable[File]): String = {
    val stringWriter =
      new StringWriter

    val outputWriter =
      new OutputWriter(stringWriter)


    files
      .toList
      .sortBy(_.getName)
      .foreach(file => {
        outputWriter.printHeader(s"Importing file '${file.getAbsolutePath}'")

        val importerOption: Option[Importer] =
          App.PluginsRegistry.importers.find(_.canImport(file))

        importerOption match {
          case Some(importer) =>
            outputWriter.println(s"Applying importer: '${importer.getClass.getCanonicalName}'")
            outputWriter.println()

            importer.importFile(
              file,
              outputWriter,
              AppDb.problemRepository,
              AppDb.solutionRepository
            )

          case None =>
            outputWriter.printOutcome("No suitable importer found")
        }
      })

    stringWriter.toString
  }
}
