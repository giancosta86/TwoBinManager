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

/*^
  ===========================================================================
  TwoBinKernel
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

package info.gianlucacosta.twobinmanager.db

import java.util.UUID
import javax.persistence.{EntityManager, EntityManagerFactory}

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

/**
  * Provides shared features for tests based on an in-memory db having all the tables
  * expressing the architecture's core model
  */
trait DbTestBase extends FlatSpec with Matchers with BeforeAndAfterEach {
  private var _dbConnection: DbConnection = _

  protected def entityManagerFactory: EntityManagerFactory =
    _dbConnection.entityManagerFactory


  private var _entityManager: EntityManager = _

  protected def entityManager: EntityManager =
    _entityManager


  private var _problemRepository: DbProblemRepository = _

  protected def problemRepository: DbProblemRepository =
    _problemRepository


  private var _solutionRepository: DbSolutionRepository = _

  protected def solutionRepository: DbSolutionRepository =
    _solutionRepository


  override protected def beforeEach(): Unit = {
    _dbConnection = new DbConnection(
      "javax.persistence.jdbc.url" -> s"jdbc:hsqldb:mem:testDb_${UUID.randomUUID()}",
      "hibernate.show_sql" -> "true",
      "format_sql" -> "true",
      "hibernate.hbm2ddl.auto" -> "create"
    ) {
      open()
    }

    _entityManager =
      _dbConnection.entityManagerFactory.createEntityManager()

    _problemRepository =
      new DbProblemRepository(_dbConnection.entityManagerFactory)

    _solutionRepository =
      new DbSolutionRepository(_dbConnection.entityManagerFactory)
  }


  override protected def afterEach(): Unit = {
    _entityManager.close()

    _dbConnection.close()
  }


  protected def runTransaction(unitOfWork: => Unit): Unit = {
    entityManager.getTransaction.begin()

    unitOfWork

    entityManager.getTransaction.commit()
  }
}
