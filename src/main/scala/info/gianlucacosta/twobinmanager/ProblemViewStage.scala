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

package info.gianlucacosta.twobinmanager

import info.gianlucacosta.helios.Includes._
import info.gianlucacosta.helios.apps.AppInfo
import info.gianlucacosta.helios.fx.Includes._
import info.gianlucacosta.helios.fx.stage.StackedStage
import info.gianlucacosta.twobinmanager.util.BasicFormTextField
import info.gianlucacosta.twobinpack.core.Problem
import info.gianlucacosta.twobinpack.rendering.gallery.BlockGalleryPane

import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.{BorderPane, HBox, VBox}

/**
  * Stage showing a problem
  *
  * @param appInfo       Application info
  * @param previousStage The previous stage
  * @param problem       The problem to show
  */
class ProblemViewStage(appInfo: AppInfo, val previousStage: javafx.stage.Stage, problem: Problem) extends StackedStage {
  this.setMainIcon(appInfo)

  private val blockGalleryPane = {
    new BlockGalleryPane(
      problem.frameTemplate.blockGallery,
      problem.frameTemplate.colorPalette,
      problem.frameTemplate.resolution
    ) {
      interactive =
        false

      style =
        "-fx-background-color: white;"
    }
  }


  scene =
    new Scene {

      root =
        new BorderPane() {

          top =
            new VBox {
              padding =
                Insets(15)

              spacing =
                15

              children = List(
                new BasicFormTextField(
                  "Name:",
                  problem.name
                ),


                new HBox {
                  spacing =
                    35

                  children =
                    List(
                      new VBox {
                        spacing =
                          15

                        children = List(
                          new BasicFormTextField(
                            "Id:",
                            problem.id
                          ),

                          new BasicFormTextField(
                            "Frame mode:",
                            problem.frameTemplate.frameMode
                          ),

                          new BasicFormTextField(
                            "Initial frame dimension:",
                            s"${problem.frameTemplate.initialDimension.width} x ${problem.frameTemplate.initialDimension.height} units"
                          ),

                          new BasicFormTextField(
                            "Block dimensions:",
                            s"${problem.frameTemplate.blockPool.blockDimensions.size}"
                          )
                        )
                      },


                      new VBox {
                        spacing = 15

                        children = List(
                          new BasicFormTextField(
                            "Can rotate blocks?",
                            problem.frameTemplate.blockPool.canRotateBlocks
                          ),

                          new BasicFormTextField(
                            "Time limit:",
                            problem.timeLimitOption match {
                              case Some(timeLimit) =>
                                timeLimit.digitalFormat

                              case None =>
                                "(no time limit)"
                            }
                          ),

                          new BasicFormTextField(
                            "Resolution:",
                            s"${problem.frameTemplate.resolution} pixels per unit"
                          ),

                          new BasicFormTextField(
                            "Total blocks:",
                            s"${problem.frameTemplate.blockPool.totalBlockCount}"
                          )
                        )
                      }
                    )
                }
              )
            }


          center =
            new ScrollPane {
              fitToWidth =
                true

              content =
                blockGalleryPane
            }
        }
    }


  title =
    s"View problem '${problem.name}'..."

  maximized =
    true
}
