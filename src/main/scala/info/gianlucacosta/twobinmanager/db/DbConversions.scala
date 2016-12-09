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

import info.gianlucacosta.twobinpack.core._

import scala.collection.JavaConversions._
import scala.language.implicitConversions
import scalafx.scene.paint.Color

/**
  * Object providing extensions for implicitly converting Scala model instances to the
  * related Java db POJOs
  */
object DbConversions {
  implicit def convertAnchoredBlock(block: AnchoredBlock): AnchoredBlockValue =
    new AnchoredBlockValue(
      block.dimension.width,
      block.dimension.height,
      block.anchor.left,
      block.anchor.top
    )


  implicit def convertAnchoredBlockValue(blockValue: AnchoredBlockValue): AnchoredBlock =
    AnchoredBlock(
      BlockDimension(
        blockValue.getWidth,
        blockValue.getHeight
      ),

      QuantizedPoint2D(
        blockValue.getAnchorLeft,
        blockValue.getAnchorTop
      )
    )


  implicit def convertColor(color: Color): ColorValue =
    new ColorValue(
      color.red,
      color.green,
      color.blue,
      color.opacity
    )


  implicit def convertColorValue(colorValue: ColorValue): Color =
    Color.color(
      colorValue.getRed,
      colorValue.getGreen,
      colorValue.getBlue,
      colorValue.getOpacity
    )


  implicit def convertProblem(problem: Problem): ProblemEntity = {
    if (problem == null)
      null
    else {
      val blockPoolItems: Set[BlockPoolItemValue] =
        problem.frameTemplate.blockPool.blocks
          .map {
            case (blockDimension, quantity) =>
              new BlockPoolItemValue(
                blockDimension.width,
                blockDimension.height,
                quantity
              )
          }.toSet


      val blockColorsPool: List[ColorValue] =
        problem.frameTemplate.blockColorsPool
          .map(convertColor)


      new ProblemEntity(
        problem.id,
        problem.name,

        problem.frameTemplate.initialDimension.width,
        problem.frameTemplate.initialDimension.height,
        FrameMode.All.indexOf(problem.frameTemplate.frameMode),

        problem.timeLimitInMinutesOption.map(value => value: Integer).getOrElse(null: Integer),
        problem.frameTemplate.blockPool.canRotateBlocks,
        problem.frameTemplate.resolution,

        blockPoolItems,
        blockColorsPool
      )
    }
  }


  implicit def convertProblemEntity(problemEntity: ProblemEntity): Problem = {
    if (problemEntity == null)
      null
    else {
      val initialDimension =
        FrameDimension(
          problemEntity.getInitialFrameWidth,
          problemEntity.getInitialFrameHeight
        )


      val blocks: Map[BlockDimension, Int] =
        problemEntity
          .getBlockPoolItems
          .map(blockPoolItem =>
            BlockDimension(
              blockPoolItem.getWidth,
              blockPoolItem.getHeight
            ) -> blockPoolItem.getQuantity
          ).toMap


      val blockPool =
        BlockPool.create(
          problemEntity.isCanRotateBlocks,
          blocks
        )


      val blockColorsPool: List[Color] =
        problemEntity.getBlockColorsPool
          .map(convertColorValue)
          .toList


      val frameTemplate =
        FrameTemplate(
          initialDimension,
          FrameMode.All.get(problemEntity.getFrameMode),
          blockPool,
          blockColorsPool,
          problemEntity.getResolution
        )

      val timeLimitInMinutesOption: Option[Int] =
        Option(problemEntity.getTimeLimitInMinutes).asInstanceOf[Option[Int]]


      Problem(
        frameTemplate,
        timeLimitInMinutesOption,
        problemEntity.getName,
        problemEntity.getId
      )
    }
  }


  implicit def convertSolution(solution: Solution): SolutionEntity =
    if (solution == null)
      null
    else
      new SolutionEntity(
        solution.id,
        solution.problem,
        solution.blocks.map(convertAnchoredBlock),
        solution.solverOption.orNull
      )


  implicit def convertSolutionEntity(solutionEntity: SolutionEntity): Solution =
    if (solutionEntity == null)
      null
    else {
      val solverString =
        solutionEntity.getSolver

      val solverOption: Option[String] =
        if (solverString != null && solverString.nonEmpty)
          Some(solverString)
        else
          None

      Solution(
        solutionEntity.getProblem,
        solverOption,
        solutionEntity.getBlocks.map(convertAnchoredBlockValue).toSet,
        solutionEntity.getId
      )
    }
}


