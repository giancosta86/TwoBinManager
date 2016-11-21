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

package info.gianlucacosta.twobinmanager.util

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}

import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

/**
  * Shows a description field (in bold font) followed by a value field
  *
  * @param fieldDescription  The field's description (shown by the leftmost label)
  * @param initialFieldValue The field's value
  * @param internalMargin    The space between description and value
  */
class BasicFormTextField(
                          fieldDescription: String,
                          initialFieldValue: Any = "",
                          internalMargin: Int = 5
                        ) extends HBox {

  styleClass +=
    "basicFormTextField"


  private val _fieldValue =
    new SimpleObjectProperty[Any](initialFieldValue)

  /**
    * The field value property, which updated the related label
    *
    * @return
    */
  def fieldValue: ObjectProperty[Any] =
  _fieldValue

  def fieldValue_=(newValue: Any): Unit =
    _fieldValue() =
      newValue


  children = List(
    new Label {
      text =
        fieldDescription

      margin =
        Insets(0, internalMargin, 0, 0)

      style =
        "-fx-font-weight: bold;"
    },

    new Label {
      text <==
        Bindings.createStringBinding(
          () =>
            fieldValue().toString,

          fieldValue
        )
    }
  )
}
