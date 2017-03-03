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

import java.io.{BufferedWriter, StringWriter}

import akka.actor.Actor
import info.gianlucacosta.twobinmanager.sdk.server.messages.problems.{ProblemBundleRequest, ProblemBundleResult}
import info.gianlucacosta.twobinpack.core.ProblemBundle
import info.gianlucacosta.twobinpack.io.bundle.ProblemBundleWriter

/**
  * Actor publishing a ProblemBundle in XML format
  */
class ProblemBundleActor(problemBundle: ProblemBundle) extends Actor {
  private val problemBundleXml = {
    val targetWriter =
      new StringWriter()

    val problemBundleWriter =
      new ProblemBundleWriter(
        new BufferedWriter(
          targetWriter
        )
      )

    try {
      problemBundleWriter.writeProblemBundle(problemBundle)
    } finally {
      problemBundleWriter.close()
    }

    targetWriter.toString
  }


  override def receive: Receive = {
    case ProblemBundleRequest(requestId) =>
      val response =
        ProblemBundleResult(
          requestId,
          problemBundleXml
        )

      sender() ! response

    case _ =>
      ()
  }
}
