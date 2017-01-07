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

package info.gianlucacosta.twobinmanager.main

import info.gianlucacosta.helios.fx.dialogs.Alerts

import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Label, TextArea}
import scalafx.scene.layout.{Priority, VBox}

/**
  * Dialog showing the results of the import process
  *
  * @param ownerStage        The owner stage
  * @param importedProblems  The total number of imported problems
  * @param importedSolutions The total number of imported solutions
  * @param importLogString   The overall log string
  */
private class ImportOutcomeAlert(
                                  ownerStage: javafx.stage.Stage,
                                  importedProblems: Long,
                                  importedSolutions: Long,
                                  importLogString: String
                                ) extends Alert(AlertType.Information) {
  initOwner(ownerStage)

  title =
    "Import files..."

  headerText =
    "Import outcome and log"


  val summaryLabel =
    new Label(s"Imported problems: ${importedProblems}\nImported solutions: ${importedSolutions}")

  val logTextArea = new TextArea {
    text =
      importLogString

    editable =
      false

    wrapText =
      false

    minWidth =
      900

    minHeight =
      450

    vgrow =
      Priority.Always

    hgrow =
      Priority.Always

    style =
      "-fx-font-family: monospace;"
  }

  dialogPane().content =
    new VBox {
      spacing =
        20

      children = List(
        summaryLabel,
        logTextArea
      )
    }

  Alerts.fix(this)
}
