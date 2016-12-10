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

import java.io.{BufferedReader, File, StringReader}
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.regex.Pattern

import info.gianlucacosta.twobinmanager.sdk.io.OutputWriter
import info.gianlucacosta.twobinpack.core.Problem
import info.gianlucacosta.twobinpack.io.FileExtensions

import scala.annotation.tailrec


object ProblemBundleImporter {

  private object DurationInMinutesPatterns {
    val someTagPattern =
      Pattern.compile(
        "<timeLimitInMinutesOption class=\"some\">\\s*<x class=\"int\">(\\d+)<\\/x>\\s*<\\/timeLimitInMinutesOption>"
      )

    val noneTagPattern =
      Pattern.compile(
        "<timeLimitInMinutesOption class=\"none\".*\\/>"
      )
  }

}

/**
  * Imports problem bundles - according to version 1 of the document format
  */
class ProblemBundleImporter extends ProblemBundleImporterBase {
  override protected def readProblems(file: File, outputWriter: OutputWriter): Set[Problem] = {
    val updatedFileContent =
      replaceTimeLimitInMinutesTag(file)

    val sourceReader =
      new BufferedReader(
        new StringReader(
          updatedFileContent
        )
      )

    readProblemsInBundle(sourceReader)
  }


  private def replaceTimeLimitInMinutesTag(file: File): String = {
    val sourceBytes =
      Files.readAllBytes(file.toPath)

    val fileContent =
      new String(sourceBytes, Charset.forName("utf-8"))

    replaceTimeLimitInMinutesTag(fileContent)
  }


  @tailrec
  private def replaceTimeLimitInMinutesTag(cumulatedFileContent: String): String = {
    val someTagMatcher =
      ProblemBundleImporter.DurationInMinutesPatterns.someTagPattern.matcher(cumulatedFileContent)

    if (someTagMatcher.find()) {
      val timeLimitInMinutes =
        someTagMatcher.group(1).toInt

      val timeLimitInSeconds =
        timeLimitInMinutes * 60

      val replacementTag =
        "<timeLimitOption class=\"some\"><x class=\"java.time.Duration\">" + timeLimitInSeconds + "</x></timeLimitOption>"

      val updatedFileContent =
        someTagMatcher.replaceFirst(replacementTag)

      replaceTimeLimitInMinutesTag(
        updatedFileContent
      )
    } else {
      val noneTagMatcher =
        ProblemBundleImporter.DurationInMinutesPatterns.noneTagPattern.matcher(cumulatedFileContent)

      noneTagMatcher.replaceAll("<timeLimitOption class=\"none\"/>")
    }
  }

  override def canImport(file: File): Boolean =
    file.getName.toLowerCase.endsWith(FileExtensions.ProblemBundle)
}
