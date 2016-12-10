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

package info.gianlucacosta.twobinmanager.db

import java.util.UUID
import javax.persistence.RollbackException

import info.gianlucacosta.helios.jpa.Includes._
import info.gianlucacosta.twobinmanager.db.DbConversions._
import info.gianlucacosta.twobinpack.test.SimpleTestData.{ProblemA, ProblemB}


class TestDbProblemRepository extends DbTestBase {
  "Retrieving a missing problem by id" should "return None" in {
    val retrievedProblem =
      problemRepository.findById(UUID.randomUUID())

    retrievedProblem should be(None)
  }


  "Retrieving a saved problem by id" should "return it correctly" in {
    entityManagerFactory.runTransaction(entityManager => {
      val problemEntity: ProblemEntity =
        ProblemA

      entityManager.persist(problemEntity)
    })


    val retrievedProblem =
      problemRepository.findById(ProblemA.id)

    retrievedProblem should be(Some(ProblemA))
  }


  "Retrieving a missing problem by name" should "return None" in {
    val retrievedProblem =
      problemRepository.findByName("MISSING")

    retrievedProblem should be(None)
  }


  "Retrieving a problem by name" should "return it correctly" in {
    entityManagerFactory.runTransaction(entityManager => {
      val problemEntity: ProblemEntity =
        ProblemA

      entityManager.persist(problemEntity)
    })


    val retrievedProblem =
      problemRepository.findByName(ProblemA.name)

    retrievedProblem should be(Some(ProblemA))
  }


  "Retrieving all sorted names in an empty db" should "return an empty list" in {
    val retrievedNames =
      problemRepository.findAllNamesSorted()

    retrievedNames should be(List())
  }


  "Retrieving all sorted names in a db with saved problems" should "work correctly" in {
    entityManagerFactory.runTransaction(entityManager => {
      val problemEntityA: ProblemEntity =
        ProblemA

      val problemEntityB: ProblemEntity =
        ProblemB

      entityManager.persist(problemEntityA)
      entityManager.persist(problemEntityB)
    })


    val retrievedNames =
      problemRepository.findAllNamesSorted()

    require(ProblemA.name < ProblemB.name)

    retrievedNames should be(List(
      ProblemA.name,
      ProblemB.name
    ))
  }


  "Adding a new problem" should "work" in {
    problemRepository.add(ProblemA)

    val problemEntity: ProblemEntity =
      ProblemA

    val retrievedEntity =
      entityManager.find(classOf[ProblemEntity], ProblemA.id)

    retrievedEntity should be(problemEntity)
  }


  "Adding the same problem twice" should "fail" in {
    problemRepository.add(ProblemA)

    intercept[RollbackException] {
      problemRepository.add(ProblemA)
    }
  }


  "Adding a problem having no time limit" should "work" in {
    require(ProblemB.timeLimitOption.isEmpty)

    problemRepository.add(ProblemB)
  }


  "Retrieving a problem having no time limit" should "work" in {
    require(ProblemB.timeLimitOption.isEmpty)

    problemRepository.add(ProblemB)

    val retrievedProblem =
      problemRepository.findByName(ProblemB.name)

    retrievedProblem should be(Some(ProblemB))
  }



  "Updating a new problem" should "fail" in {
    problemRepository.update(ProblemA)
  }


  "Updating an existing problem" should "work" in {
    problemRepository.add(ProblemA)

    val updatedProblem =
      ProblemA.copy(name = "Problem with dedicated new name")

    val updatedProblemEntity: ProblemEntity =
      updatedProblem


    problemRepository.update(updatedProblem)

    val retrievedProblemEntity =
      entityManager.find(classOf[ProblemEntity], ProblemA.id)

    retrievedProblemEntity should be(updatedProblemEntity)
  }


  "Removing an existing problem" should "work correctly" in {
    entityManagerFactory.runTransaction(entityManager => {
      val problemEntity: ProblemEntity =
        ProblemA

      entityManager.persist(problemEntity)
    })


    problemRepository.findById(ProblemA.id) should not be None

    problemRepository.removeByName(ProblemA.name)

    problemRepository.findById(ProblemA.id) should be(None)
  }


  "Removing a missing problem by name" should "do nothing" in {
    problemRepository.removeByName("MISSING")
  }


  "Removing all problems from an empty db" should "do nothing" in {
    problemRepository.removeAll()
  }


  "Removing all problems" should "work" in {
    problemRepository.add(ProblemA)
    problemRepository.add(ProblemB)

    problemRepository.count() should be(2)

    problemRepository.removeAll()

    problemRepository.count() should be(0)
  }


  "Counting problems in an empty table" should "return 0" in {
    problemRepository.count() should be(0)
  }


  "Counting problems" should "work" in {
    problemRepository.add(ProblemA)
    problemRepository.add(ProblemB)

    problemRepository.count() should be(2)
  }
}
