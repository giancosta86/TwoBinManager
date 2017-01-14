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

package info.gianlucacosta.twobinmanager.generators.spectrumbase

import javafx.concurrent.Task
import javafx.fxml.FXML

import info.gianlucacosta.helios.fx.dialogs.Alerts
import info.gianlucacosta.twobinmanager.sdk.generators.GeneratorFxmlController
import info.gianlucacosta.twobinmanager.util.BasicFormTextField
import info.gianlucacosta.twobinpack.core._
import info.gianlucacosta.twobinpack.rendering.gallery.BlockGalleryPane

import scalafx.Includes._
import scalafx.scene.Cursor
import scalafx.scene.control.Alert.AlertType


/**
  * Base class for controllers employing the spectrum generator
  */
abstract class SpectrumControllerBase extends GeneratorFxmlController {

  /**
    * Task creating a BlockPool
    */
  protected abstract class BlockPoolTask extends Task[BlockPool] {
    override def succeeded(): Unit = {
      blockPool =
        this.get

      scene.cursor =
        Cursor.Default
    }


    override def failed(): Unit = {
      scene.cursor =
        Cursor.Default

      Alerts.showException(
        this.getException.asInstanceOf[Exception],
        alertType = AlertType.Warning
      )
    }
  }


  @FXML
  def initialize(): Unit = {
    resolutionField.text() =
      Problem.SuggestedResolution.toString
  }


  private var _blockPool: BlockPool = _

  private def blockPool: BlockPool =
    _blockPool

  private def blockPool_=(newValue: BlockPool): Unit = {
    _blockPool =
      newValue

    val colorPalette =
      ColorPalette(
        _blockPool,
        FrameTemplate.SuggestedBlockColorsPool
      )

    val resolution =
      try {
        resolutionField.text().toInt
      } catch {
        case ex: Exception =>
          Alerts.showWarning("Invalid resolution value. A default value will be used")

          resolutionField.text =
            Problem.SuggestedResolution.toString

          Problem.SuggestedResolution
      }


    val blockGallery =
      new BlockGallery(blockPool)

    galleryScrollPane.content =
      new BlockGalleryPane(
        blockGallery,
        colorPalette,
        resolution
      ) {
        interactive =
          false
      }

    saveProblemButton.disable =
      false


    blocksBox.children.clear()
    blocksBox.children.addAll(
      new BasicFormTextField(
        "Block dimensions:",
        blockPool.blockDimensions.size
      ),

      new BasicFormTextField(
        "Total blocks:",
        blockPool.totalBlockCount
      )
    )
  }


  @FXML
  def generateBlockPool(): Unit = {
    scene.cursor =
      Cursor.Wait

    val blockPoolTask =
      createBlockPoolTask()

    new Thread(blockPoolTask) {
      setDaemon(true)
    }.start()
  }


  protected def createBlockPoolTask(): BlockPoolTask


  override protected def createFrameTemplate(): FrameTemplate = {
    val initialFrameWidth =
      frameWidthField.text().toInt

    val initialFrameHeight =
      frameHeightField.text().toInt


    val initialFrameDimension =
      FrameDimension(
        initialFrameWidth,
        initialFrameHeight
      )


    val resolution =
      resolutionField.text().toInt


    FrameTemplate(
      initialFrameDimension,
      FrameMode.Strip,
      blockPool,
      FrameTemplate.SuggestedBlockColorsPool,
      resolution
    )
  }


  @FXML
  var frameWidthField: javafx.scene.control.TextField = _

  @FXML
  var frameHeightField: javafx.scene.control.TextField = _

  @FXML
  var resolutionField: javafx.scene.control.TextField = _


  @FXML
  var galleryScrollPane: javafx.scene.control.ScrollPane = _

  @FXML
  var blocksBox: javafx.scene.layout.VBox = _


  @FXML
  var generateFrameButton: javafx.scene.control.Button = _

  @FXML
  var saveProblemButton: javafx.scene.control.Button = _
}
