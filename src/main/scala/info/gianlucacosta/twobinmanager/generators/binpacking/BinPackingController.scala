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

package info.gianlucacosta.twobinmanager.generators.binpacking

import javafx.fxml.FXML

import info.gianlucacosta.twobinmanager.generators.spectrum.algorithm.SpectrumAlgorithm
import info.gianlucacosta.twobinmanager.generators.spectrumbase.SpectrumControllerBase
import info.gianlucacosta.twobinpack.core._

import scalafx.Includes._

private class BinPackingController extends SpectrumControllerBase {

  private class BinPackingTask extends BlockPoolTask {
    override def call(): BlockPool = {
      val minBlockWidth =
        1

      val minBlockHeight =
        minBlockHeightField.text().toInt

      val minBlockDimension =
        BlockDimension(
          minBlockWidth,
          minBlockHeight
        )


      val maxBlockWidth =
        1

      val maxBlockHeight =
        maxBlockHeightField.text().toInt

      val maxBlockDimension =
        BlockDimension(
          maxBlockWidth,
          maxBlockHeight
        )


      val minBlockQuantity =
        minBlockQuantityField.text().toInt

      val maxBlockQuantity =
        maxBlockQuantityField.text().toInt


      val blockQuantityRange =
        Range.inclusive(minBlockQuantity, maxBlockQuantity)

      val canRotateBlocks =
        false

      SpectrumAlgorithm.createRandomPool(
        minBlockDimension,
        maxBlockDimension,
        blockQuantityRange,
        canRotateBlocks
      )
    }
  }


  override protected def createBlockPoolTask(): BlockPoolTask =
    new BinPackingTask


  @FXML
  var minBlockHeightField: javafx.scene.control.TextField = _

  @FXML
  var maxBlockHeightField: javafx.scene.control.TextField = _


  @FXML
  var minBlockQuantityField: javafx.scene.control.TextField = _

  @FXML
  var maxBlockQuantityField: javafx.scene.control.TextField = _
}
