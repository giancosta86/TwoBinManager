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

package info.gianlucacosta.twobinmanager.generators.guillotine

import javafx.concurrent.Task
import javafx.fxml.FXML

import info.gianlucacosta.helios.fx.dialogs.Alerts
import info.gianlucacosta.twobinmanager.generators.guillotine.algorithm.GuillotineAlgorithm
import info.gianlucacosta.twobinmanager.sdk.generators.GeneratorFxmlController
import info.gianlucacosta.twobinmanager.util.BasicFormTextField
import info.gianlucacosta.twobinpack.core._
import info.gianlucacosta.twobinpack.rendering.frame.Frame

import scalafx.Includes._
import scalafx.scene.Cursor
import scalafx.scene.control.Alert.AlertType

/**
  * Controller providing the Guillotine random generator
  */
private class GuillotineController extends GeneratorFxmlController {

  private class FrameTask extends Task[Frame] {
    override def call(): Frame = {
      val frameWidth =
        frameWidthField.text().toInt

      val frameHeight =
        frameHeightField.text().toInt


      val frameDimension =
        FrameDimension(
          frameWidth,
          frameHeight
        )


      val maxBlockWidth =
        maxBlockWidthField.text().toInt

      val maxBlockHeight =
        maxBlockHeightField.text().toInt

      val maxBlockDimension =
        BlockDimension(
          maxBlockWidth,
          maxBlockHeight
        )


      val canRotateBlocks =
        canRotateCheckBox.isSelected


      val blockSelectionProbability =
        blockSelectionProbabilityField.text().toInt / 100.0

      val resolution =
        resolutionField.text().toInt


      val (frameTemplate, blocks) =
        GuillotineAlgorithm.createRandomKnapsack(
          frameDimension,
          maxBlockDimension,
          canRotateBlocks,
          blockSelectionProbability,
          FrameTemplate.SuggestedBlockColorsPool,
          resolution
        )

      val generatedFrame =
        new Frame(frameTemplate) {
          interactive =
            false
        }


      blocks.foreach(block => {
        generatedFrame.addBlock(block)
      })

      generatedFrame
    }

    override def succeeded(): Unit = {
      frame =
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
    frameModeChoiceBox.items().addAll(
      FrameMode.All: _*
    )

    frameModeChoiceBox.getSelectionModel.select(FrameMode.Knapsack)


    resolutionField.text() =
      Problem.SuggestedResolution.toString
  }


  private var _frame: Frame = _

  private def frame: Frame =
    _frame

  private def frame_=(newValue: Frame): Unit = {
    _frame =
      newValue

    frameScrollPane.content =
      _frame

    saveProblemButton.disable =
      false


    blocksBox.children.clear()
    blocksBox.children.addAll(
      new BasicFormTextField(
        "Block dimensions:",
        frame.frameTemplate.blockPool.blockDimensions.size
      ),

      new BasicFormTextField(
        "Total blocks:",
        frame.frameTemplate.blockPool.totalBlockCount
      )
    )
  }

  @FXML
  def generateFrame(): Unit = {
    scene.cursor =
      Cursor.Wait

    val frameTask =
      new FrameTask

    new Thread(frameTask) {
      setDaemon(true)
    }.start()
  }


  override protected def createFrameTemplate(): FrameTemplate =
    frame.frameTemplate.copy(
      frameMode =
        frameModeChoiceBox.getSelectionModel.getSelectedItem
    )


  @FXML
  var frameModeChoiceBox: javafx.scene.control.ChoiceBox[FrameMode] = _


  @FXML
  var canRotateCheckBox: javafx.scene.control.CheckBox = _


  @FXML
  var frameWidthField: javafx.scene.control.TextField = _

  @FXML
  var frameHeightField: javafx.scene.control.TextField = _


  @FXML
  var maxBlockWidthField: javafx.scene.control.TextField = _


  @FXML
  var maxBlockHeightField: javafx.scene.control.TextField = _


  @FXML
  var resolutionField: javafx.scene.control.TextField = _


  @FXML
  var blockSelectionProbabilityField: javafx.scene.control.TextField = _


  @FXML
  var blocksBox: javafx.scene.layout.VBox = _


  @FXML
  var generateFrameButton: javafx.scene.control.Button = _

  @FXML
  var saveProblemButton: javafx.scene.control.Button = _

  @FXML
  var frameScrollPane: javafx.scene.control.ScrollPane = _
}
