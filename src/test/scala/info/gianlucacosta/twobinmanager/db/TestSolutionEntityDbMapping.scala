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

import info.gianlucacosta.twobinmanager.db.DbConversions._
import info.gianlucacosta.twobinpack.test.SimpleTestData


class TestSolutionEntityDbMapping extends DbTestBase {

  "SolutionEntity" should "be persisted correctly" in {
    val testProblemEntity: ProblemEntity =
      SimpleTestData.ProblemA

    val testSolutionEntity: SolutionEntity =
      SimpleTestData.SolutionA1

    runTransaction {
      entityManager.persist(testProblemEntity)
    }

    runTransaction {
      entityManager.persist(testSolutionEntity)
    }
  }


  "SolutionEntity" should "NOT be persisted correctly without the related problem" in {
    val testSolutionEntity: SolutionEntity =
      SimpleTestData.SolutionA1

    intercept[Exception] {
      runTransaction {
        entityManager.persist(testSolutionEntity)
      }
    }
  }


  "SolutionEntity" should "be retrieved correctly" in {
    val testProblemEntity: ProblemEntity =
      SimpleTestData.ProblemA

    val testSolutionEntity: SolutionEntity =
      SimpleTestData.SolutionA1


    runTransaction {
      entityManager.persist(testProblemEntity)
    }


    runTransaction {
      entityManager.persist(testSolutionEntity)
    }

    val retrievedSolutionEntity =
      entityManager.find(classOf[SolutionEntity], testSolutionEntity.id)

    retrievedSolutionEntity should be(testSolutionEntity)
  }
}
