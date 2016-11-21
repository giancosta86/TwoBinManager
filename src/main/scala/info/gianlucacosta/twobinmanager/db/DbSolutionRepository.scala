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
import javax.persistence.EntityManagerFactory

import info.gianlucacosta.helios.jpa.Includes._
import info.gianlucacosta.twobinmanager.db.DbConversions._
import info.gianlucacosta.twobinpack.core.Solution
import info.gianlucacosta.twobinpack.io.repositories.SolutionRepository

import scala.collection.JavaConversions._

class DbSolutionRepository(entityManagerFactory: EntityManagerFactory) extends SolutionRepository {
  override def findById(id: UUID): Option[Solution] = {
    entityManagerFactory.runUnitOfWork(entityManager => {
      val solutionEntity =
        entityManager
          .find(classOf[SolutionEntity], id)

      Option(solutionEntity)
    })
  }


  override def add(solution: Solution): Unit = {
    entityManagerFactory.runTransaction(entityManager => {
      val solutionEntity: SolutionEntity =
        solution

      val problemEntity: ProblemEntity =
        entityManager.find(classOf[ProblemEntity], solution.problem.id)

      solutionEntity.setProblem(problemEntity)

      entityManager.persist(solutionEntity)
    })
  }


  entityManagerFactory.addNamedQueryFor(
    getClass,
    "findAll",

    """
      SELECT solution
      FROM SolutionEntity solution
    """
  )

  override def findAll(): Iterable[Solution] = {
    entityManagerFactory.runUnitOfWork(entityManager => {
      entityManager
        .createNamedQueryFor(
          getClass,
          "findAll",
          classOf[SolutionEntity]
        )
        .getResultList
        .map(solutionEntity => solutionEntity: Solution)
    })
  }


  entityManagerFactory.addNamedQueryFor(
    getClass,
    "findAllByProblemName",

    """
      SELECT solution
      FROM SolutionEntity solution
      WHERE solution.problem.name = :name
    """
  )

  override def findAllByProblemName(problemName: String): Iterable[Solution] = {
    entityManagerFactory.runUnitOfWork(entityManager => {
      entityManager
        .createNamedQueryFor(
          getClass,
          "findAllByProblemName",
          classOf[SolutionEntity]
        )
        .setParameter("name", problemName)
        .getResultList
        .map(solutionEntity => solutionEntity: Solution)
    })
  }


  entityManagerFactory.addNamedQueryFor(
    getClass,
    "removeByProblemName",

    """
      DELETE FROM SolutionEntity solution
      WHERE solution.id IN (
        SELECT solution.id
        FROM SolutionEntity solution
        WHERE solution.problem.name = :problemName
      )
    """
  )

  override def removeByProblemName(problemName: String): Unit = {
    entityManagerFactory.runTransaction(entityManager => {
      entityManager
        .createNamedQueryFor(
          getClass,
          "removeByProblemName"
        )
        .setParameter("problemName", problemName)
        .executeUpdate()
    })
  }


  entityManagerFactory.addNamedQueryFor(
    getClass,
    "count",

    """
      SELECT COUNT(id)
      FROM SolutionEntity
    """
  )

  override def count(): Long =
    entityManagerFactory.runUnitOfWork(entityManager => {
      entityManager
        .createNamedQueryFor(
          getClass,
          "count",
          classOf[java.lang.Long]
        )
        .getSingleResult
    })
}
