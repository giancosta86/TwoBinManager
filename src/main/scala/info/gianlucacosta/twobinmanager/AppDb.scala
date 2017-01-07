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

package info.gianlucacosta.twobinmanager

import java.io.File

import info.gianlucacosta.twobinmanager.db.{DbConnection, DbProblemRepository, DbSolutionRepository}
import info.gianlucacosta.twobinpack.twobinmanager.ArtifactInfo


/**
  * Simplifies access to the application's database
  */
object AppDb {
  val DbFile: File =
    new File(
      App.Directory,
      "db"
    )

  val DbConnection: DbConnection = {
    val ddlSetupMode: String =
      if (dbExists)
        "none"
      else
        "create"

    new DbConnection(
      "javax.persistence.jdbc.url" -> s"jdbc:hsqldb:file:${DbFile}",
      "hibernate.show_sql" -> (!ArtifactInfo.release).toString,
      "format_sql" -> (!ArtifactInfo.release).toString,
      "hibernate.hbm2ddl.auto" -> ddlSetupMode
    )
  }

  lazy val problemRepository =
    new DbProblemRepository(DbConnection.entityManagerFactory)

  lazy val solutionRepository =
    new DbSolutionRepository(DbConnection.entityManagerFactory)


  private def dbExists: Boolean = {
    val dbTempDirectory =
      new File(
        DbFile.getAbsolutePath + ".tmp"
      )

    val dbLogFile =
      new File(
        DbFile.getAbsolutePath + ".log"
      )

    val dbScriptFile =
      new File(
        DbFile.getAbsolutePath + ".script"
      )

    val dbPropertiesFile =
      new File(
        DbFile.getAbsolutePath + ".properties"
      )

    dbTempDirectory.isDirectory || dbLogFile.isFile || dbScriptFile.isFile || dbPropertiesFile.isFile
  }
}
