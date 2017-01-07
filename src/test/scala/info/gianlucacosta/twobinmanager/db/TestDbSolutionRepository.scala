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

package info.gianlucacosta.twobinmanager.db

import java.time.Duration
import java.util.UUID
import javax.persistence.{PersistenceException, RollbackException}

import info.gianlucacosta.helios.Includes._
import info.gianlucacosta.helios.jpa.Includes._
import info.gianlucacosta.twobinmanager.db.DbConversions._
import info.gianlucacosta.twobinpack.core.Problem
import info.gianlucacosta.twobinpack.test.SimpleTestData._


class TestDbSolutionRepository extends DbTestBase {
  "Adding a missing solution" should "work" in {
    problemRepository.add(ProblemA)

    solutionRepository.add(SolutionA1)
  }


  "Re-adding an existing solution" should "fail" in {
    problemRepository.add(ProblemA)

    solutionRepository.add(SolutionA1)

    intercept[RollbackException] {
      solutionRepository.add(SolutionA1)
    }
  }


  "Adding a solution for a missing problem" should "fail" in {
    intercept[PersistenceException] {
      solutionRepository.add(SolutionA1)
    }
  }


  "Getting all the solutions for a missing problem" should "return an empty list" in {
    solutionRepository.findAllByProblemName("MISSING") should be(empty)
  }


  private def saveDefaultProblemsAndSolutions(): Unit = {
    problemRepository.add(ProblemA)
    problemRepository.add(ProblemB)

    solutionRepository.add(SolutionA1)
    solutionRepository.add(SolutionA2)
    solutionRepository.add(SolutionB1)
  }


  "Getting all the solutions for an existing problem" should "return the list" in {
    saveDefaultProblemsAndSolutions()

    val retrievedSolutionsSet =
      solutionRepository.findAllByProblemName(ProblemA.name).toSet


    retrievedSolutionsSet should be(Set(
      SolutionA1,
      SolutionA2
    ))
  }


  "Counting solutions in an empty table" should "return 0" in {
    solutionRepository.count() should be(0)
  }


  "Counting solutions" should "work" in {
    saveDefaultProblemsAndSolutions()

    solutionRepository.count() should be(3)
  }


  "Removing problems" should "delete the related solutions" in {
    saveDefaultProblemsAndSolutions()


    solutionRepository.count() should be(3)

    problemRepository.removeByName(ProblemA.name)

    solutionRepository.count() should be(1)
  }


  "Deleting solutions for an existing problem" should "work" in {
    saveDefaultProblemsAndSolutions()

    solutionRepository.removeByProblemName(ProblemA.name)

    solutionRepository.findAll().toSet should be(Set(
      SolutionB1
    ))
  }


  "Deleting solutions for a missing problem" should "just do nothing" in {
    saveDefaultProblemsAndSolutions()

    solutionRepository.removeByProblemName("MISSING")

    problemRepository.count() should be(2)
    solutionRepository.count() should be(3)
  }


  "Deleting solutions for a problem" should "not delete the problem" in {
    saveDefaultProblemsAndSolutions()

    solutionRepository.removeByProblemName(ProblemA.name)

    problemRepository.count() should be(2)
  }


  "Retrieving a solution by id" should "work" in {
    val problemEntity: ProblemEntity =
      ProblemA

    val solutionEntity: SolutionEntity =
      SolutionA1


    entityManagerFactory.runTransaction(entityManager => {
      entityManager.persist(problemEntity)
    })

    entityManagerFactory.runTransaction(entityManager => {
      entityManager.persist(solutionEntity)
    })


    val retrievedSolution =
      solutionRepository.findById(solutionEntity.id)

    retrievedSolution should be(Some(SolutionA1))
  }


  "Trying to retrieve a missing id" should "return nothing" in {
    solutionRepository.findById(UUID.randomUUID()) should be(None)
  }


  "Retrieving all solutions from an empty db" should "return an empty set" in {
    solutionRepository.findAll() should be(empty)
  }


  "Retrieving all solutions" should "work" in {
    saveDefaultProblemsAndSolutions()


    solutionRepository.findAll().toSet should be(Set(
      SolutionA1,
      SolutionA2,
      SolutionB1
    )
    )
  }


  "Removing all problems" should "remove solutions as well" in {
    saveDefaultProblemsAndSolutions()

    problemRepository.removeAll()

    solutionRepository.count() should be(0)
  }


  "Saving a solution whose elapsed time is more than 1 hour" should "work" in {
    val problem =
      ProblemA.copy(
        timeLimitOption =
          Some(
            Duration.ofHours(5)
          )
      )

    problemRepository.add(problem)

    val elapsedTime: Duration =
      Duration.ofHours(3) + Duration.ofMinutes(19) + Duration.ofSeconds(42)

    val solution =
      SolutionA1.copy(
        problem =
          problem,

        elapsedTimeOption =
          Some(
            elapsedTime
          )
      )

    solutionRepository.add(solution)
  }


  "Retrieving a solution whose elapsed time is more than 1 hour" should "work" in {
    val problem =
      ProblemA.copy(
        timeLimitOption =
          Some(
            Duration.ofHours(5)
          )
      )

    problemRepository.add(problem)

    val elapsedTime: Duration =
      Duration.ofHours(3) + Duration.ofMinutes(19) + Duration.ofSeconds(42)

    val solution =
      SolutionA1.copy(
        problem =
          problem,

        elapsedTimeOption =
          Some(
            elapsedTime
          )
      )

    solutionRepository.add(solution)

    val retrievedSolution =
      solutionRepository.findAll().head

    retrievedSolution.elapsedTimeOption should be(Some(elapsedTime))
  }


  "Retrieving a solution whose elapsed time is the max time limit supported by a problem" should "work" in {
    val problem =
      ProblemA.copy(timeLimitOption = Some(Problem.MaxTimeLimit))

    problemRepository.add(problem)

    val elapsedTime =
      Problem.MaxTimeLimit

    val solution =
      SolutionA1.copy(
        problem =
          problem,

        elapsedTimeOption =
          Some(
            elapsedTime
          )
      )

    solutionRepository.add(solution)

    val retrievedSolution =
      solutionRepository.findAll().head

    retrievedSolution.elapsedTimeOption should be(Some(elapsedTime))
  }
}
