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
