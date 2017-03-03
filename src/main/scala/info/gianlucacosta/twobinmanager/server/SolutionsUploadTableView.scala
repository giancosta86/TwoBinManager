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

package info.gianlucacosta.twobinmanager.server

import java.time.format.DateTimeFormatter

import info.gianlucacosta.helios.fx.dialogs.BusyDialog
import info.gianlucacosta.twobinmanager.server.actors.SolutionsUpload
import info.gianlucacosta.twobinpack.io.repositories.SolutionRepository

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.{ReadOnlyObjectWrapper, ReadOnlyStringWrapper}
import scalafx.event.ActionEvent
import scalafx.scene.control.TableColumn._
import scalafx.scene.control._
import scalafx.stage.Stage


private object SolutionsUploadTableView {
  private val dateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}


private class SolutionsUploadTableView(stage: Stage, solutionRepository: SolutionRepository) extends TableView[SolutionsUpload] {
  columns ++= List(
    new TableColumn[SolutionsUpload, String] {
      text =
        "Solver"

      prefWidth =
        280

      sortable =
        true

      cellValueFactory = { row =>
        ReadOnlyStringWrapper(
          row.value.solverOption.getOrElse("(anonymous)")
        )
      }
    },


    new TableColumn[SolutionsUpload, String] {
      text =
        "Source"

      prefWidth =
        260

      sortable =
        true

      cellValueFactory = { row =>
        ReadOnlyStringWrapper(
          row.value.hostPort
        )
      }
    },


    new TableColumn[SolutionsUpload, String] {
      text =
        "Date & Time"

      prefWidth =
        200

      sortable =
        true

      cellValueFactory = { row =>
        ReadOnlyStringWrapper(
          row.value.dateTime.format(
            SolutionsUploadTableView.dateTimeFormatter
          )
        )
      }
    },


    new TableColumn[SolutionsUpload, SolutionsUpload] {
      text =
        ""

      prefWidth =
        100

      sortable =
        false

      cellValueFactory = { row =>
        ReadOnlyObjectWrapper[SolutionsUpload](row.value)
      }

      cellFactory = { _ =>
        new TableCell[SolutionsUpload, SolutionsUpload] {
          item.onChange { (_, _, solutionsUpload) =>
            graphic =
              if (solutionsUpload != null)
                new Button("Accept") {
                  onAction = (event: ActionEvent) => {
                    val solutions =
                      solutionsUpload.solutions

                    new BusyDialog(stage, "Accepting solutions...").run {
                      solutions.foreach(solutionRepository.add)

                      Platform.runLater {
                        items().removeAll(solutionsUpload)
                      }
                    }
                  }
                }
              else
                null
          }
        }
      }
    },


    new TableColumn[SolutionsUpload, SolutionsUpload] {
      text =
        ""

      prefWidth =
        100

      sortable =
        false


      cellValueFactory = { row =>
        ReadOnlyObjectWrapper[SolutionsUpload](row.value)
      }

      cellFactory = { _ =>
        new TableCell[SolutionsUpload, SolutionsUpload] {
          item.onChange { (_, _, solutionsUpload) =>
            graphic =
              if (solutionsUpload != null)
                new Button("Remove") {
                  onAction = (event: ActionEvent) => {
                    items().removeAll(solutionsUpload)
                  }
                }
              else
                null
          }
        }
      }
    }
  )
}
