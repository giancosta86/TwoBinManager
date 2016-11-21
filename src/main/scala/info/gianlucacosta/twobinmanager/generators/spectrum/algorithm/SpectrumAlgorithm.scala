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

package info.gianlucacosta.twobinmanager.generators.spectrum.algorithm

import info.gianlucacosta.twobinpack.core.{BlockDimension, BlockPool}

import scala.util.Random

/**
  * Spectrum generator algorithm.
  *
  * The name come from its algorithm, iterating over a quantized spectrum of block dimensions:
  *
  * <ol>
  * <li>
  * Generate a quantized grid whose X axis is [minBlockDimension.width .. maxBlockDimension.width]
  * and whose Y axis is [minBlockDimension.height .. maxBlockDimension.height]
  * </li>
  *
  * <li>
  * For every cell in the grid, choose a random quantity, in the given quantityRange,
  * for the block having the current width and height
  * </li>
  *
  * <li>
  * Generate a first block pool, gathering the blocks
  * </li>
  *
  * <li>
  * If rotation is enabled, grid symmetry makes horizontal blocks and their vertical
  * counterparts sum up, possibly violating the quantity constraint: in such case,
  * generate a new random quantity for the overall block pair
  * </li>
  * </ol>
  */
object SpectrumAlgorithm {
  def createRandomPool(
                        minBlockDimension: BlockDimension,
                        maxBlockDimension: BlockDimension,
                        quantityRange: Range,
                        canRotateBlocks: Boolean
                      ): BlockPool = {
    require(
      minBlockDimension.width <= maxBlockDimension.width,
      "It must be: minimum block width <= maximum block width"
    )
    require(
      minBlockDimension.height <= maxBlockDimension.height,
      "It must be: minimum block height <= maximum block height"
    )

    require(
      quantityRange.start >= 0,
      "The minimum quantity must be >= 0"
    )

    require(
      quantityRange.start <= quantityRange.end,
      "It must be: minimum quantity <= maximum quantity"
    )


    val partiallyBoundedBlocks: Map[BlockDimension, Int] =
      Range.inclusive(minBlockDimension.width, maxBlockDimension.width).flatMap(blockWidth => {
        Range.inclusive(minBlockDimension.height, maxBlockDimension.height).map(blockHeight => {
          val blockDimension =
            BlockDimension(
              blockWidth,
              blockHeight
            )

          val quantity =
            getRandomQuantity(quantityRange)

          blockDimension -> quantity
        })
      })
        .toMap
        .filter {
          case (blockDimension, quantity) =>
            quantity > 0
        }


    val partiallyBoundedBlockPool =
      BlockPool.create(
        canRotateBlocks,
        partiallyBoundedBlocks
      )


    if (canRotateBlocks) {
      val boundedBlocks: Map[BlockDimension, Int] =
        partiallyBoundedBlockPool.blocks.map {
          case (blockDimension, quantity) =>
            if (quantity > quantityRange.end) {
              val fixedQuantity =
                getRandomQuantity(quantityRange)

              blockDimension -> fixedQuantity
            } else
              blockDimension -> quantity
        }

      BlockPool.create(
        canRotateBlocks,
        boundedBlocks
      )
    } else
      partiallyBoundedBlockPool
  }


  private def getRandomQuantity(quantityRange: Range): Int =
    quantityRange.start +
      Random.nextInt(
        quantityRange.end - quantityRange.start + 1
      )
}
