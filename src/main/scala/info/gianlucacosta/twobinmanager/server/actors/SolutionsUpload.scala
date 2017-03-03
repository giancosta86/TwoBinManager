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