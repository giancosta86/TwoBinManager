package info.gianlucacosta.twobinmanager.server.actors

import java.time.LocalDateTime
import java.util.UUID

import info.gianlucacosta.twobinpack.core.Solution

/**
  * A validated request ready to upload solutions into TwoBinManager's db
  *
  * @param requestMessageId The id of the request message
  * @param solverOption     The solver shared by all the solutions
  * @param solutions        The solutions
  * @param hostPort         The host and port, as in Akka remoting
  * @param dateTime         The datetime when the request was received
  */
case class SolutionsUpload(
                            requestMessageId: UUID,
                            solverOption: Option[String],
                            solutions: Iterable[Solution],
                            hostPort: String,
                            dateTime: LocalDateTime
                          )