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

package info.gianlucacosta.twobinmanager.analytics

import java.io.{FileOutputStream, FileWriter, PrintWriter}
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.stage.Stage

import info.gianlucacosta.helios.apps.AppInfo
import info.gianlucacosta.helios.fx.Includes._
import info.gianlucacosta.helios.fx.dialogs.{Alerts, BusyDialog}
import info.gianlucacosta.helios.fx.stage.StackedStage
import info.gianlucacosta.twobinmanager.sdk.analytics.{ChartRetriever, IntermediateCache}
import info.gianlucacosta.twobinpack.core.Solution

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.binding.Bindings
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.chart.Chart
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Button, ChoiceBox, Label, ScrollPane}
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.text.TextAlignment
import scalafx.stage.FileChooser

/**
  * Stage showing the analytics
  *
  * @param appInfo             Application info
  * @param previousStage       The previous stage
  * @param allSolutions        All the solution in the repository
  * @param analyticsDimensions All the analytics dimensions detected by the app
  */
class AnalyticsStage(
                      appInfo: AppInfo,
                      val previousStage: Stage,
                      val allSolutions: Iterable[Solution],
                      val analyticsDimensions: Map[String, ChartRetriever]
                    ) extends StackedStage {
  require(
    analyticsDimensions.nonEmpty,
    "There must be at least one analytics dimension"
  )

  this.setMainIcon(appInfo)

  private val exportAsImageFileChooser: FileChooser =
    new FileChooser {
      extensionFilters.setAll(
        new FileChooser.ExtensionFilter("PNG image", "*.png")
      )

      title =
        "Save as image..."
    }


  private val exportAsCsvFileChooser: FileChooser =
    new FileChooser {
      extensionFilters.setAll(
        new FileChooser.ExtensionFilter("CSV file", "*.csv")
      )

      title =
        "Save as comma-separated values (CSV)..."
    }


  private val sortedDimensions =
    analyticsDimensions
      .keys
      .toList
      .sorted


  private val dimensionsChoiceBox: ChoiceBox[String] =
    new ChoiceBox(
      ObservableBuffer(
        sortedDimensions
      )
    )

  /**
    * Stores the chart created by the ChartRetriever related to every
    * analytics dimension that was chosen since when the stage was shown
    */
  private var chartCache: Map[String, Option[Chart]] =
  Map()

  /**
    * Stores the intermediate cache shared by ChartRetriever functions
    */
  private var intermediateCache: IntermediateCache =
  Map()


  private val noChartLabel =
    new Label {
      text =
        "(Chart not available. Perhaps insufficient or incomplete data?)"

      alignment =
        Pos.Center

      textAlignment =
        TextAlignment.Center

      maxWidth =
        Double.MaxValue

      maxHeight =
        Double.MaxValue
    }


  private val chartOption =
    new SimpleObjectProperty[Option[Chart]](None)


  dimensionsChoiceBox.selectionModel.value.selectedItem.onChange {
    val selectedDimension: String =
      dimensionsChoiceBox.selectionModel.value.selectedItem()


    if (selectedDimension != null) {
      if (chartCache.contains(selectedDimension)) {
        chartOption() =
          chartCache(selectedDimension)
      } else {
        new BusyDialog(AnalyticsStage.this, "Analyzing solutions...") {
          run {
            val chartRetriever: ChartRetriever =
              analyticsDimensions(selectedDimension)

            val (retrievedChartOption, updatedIntermediateCache) =
              chartRetriever(allSolutions, intermediateCache)

            retrievedChartOption.foreach(retrievedChart => {
              retrievedChart.styleClass.add("analyticsChart")
            })


            chartCache =
              chartCache +
                (selectedDimension -> retrievedChartOption)

            intermediateCache =
              updatedIntermediateCache


            Platform.runLater {
              chartOption() =
                retrievedChartOption
            }
          }
        }

        ()
      }
    } else {
      chartOption() =
        None
    }
  }


  scene =
    new Scene {
      stylesheets.add(
        getClass.getResource("Analytics.css").toExternalForm
      )

      root =
        new BorderPane {
          top =
            new HBox {
              spacing =
                10

              alignment =
                Pos.Center

              padding =
                Insets(15)

              children =
                List(
                  new Label("Dimensions:"),

                  dimensionsChoiceBox
                )
            }


          center =
            new ScrollPane {
              fitToWidth =
                true

              fitToHeight =
                true

              content <==
                Bindings.createObjectBinding[Node](
                  () => {
                    chartOption()
                      .map(_.delegate)
                      .getOrElse(noChartLabel)

                  },

                  chartOption
                )
            }

          bottom =
            new HBox {
              padding =
                Insets(15)

              visible <==
                chartOption =!= None

              alignment =
                Pos.CenterRight

              spacing =
                15


              children =
                List(
                  new Button("Save as image...") {
                    onAction =
                      () => {
                        exportChartAsImage()
                      }
                  },

                  new Button("Save as CSV...") {
                    disable <==
                      Bindings.createBooleanBinding(
                        () => {
                          chartOption().exists(chart =>
                            !chart.canExportAsCSV
                          )
                        },

                        chartOption
                      )

                    onAction =
                      () => {
                        exportChartAsCsv()
                      }
                  }
                )
            }
        }
    }


  private def exportChartAsImage(): Unit = {
    chartOption().foreach(chart => {
      val imageFile =
        exportAsImageFileChooser.smartSave(this)


      if (imageFile != null) {
        val outputStream =
          new FileOutputStream(imageFile)

        try {
          chart.exportAsImage(outputStream)

          Alerts.showInfo("Image saved successfully.")
        } catch {
          case ex: Exception =>
            Alerts.showException(ex, alertType = AlertType.Warning)
        } finally {
          outputStream.close()
        }
      }
    })
  }


  private def exportChartAsCsv(): Unit = {
    chartOption().foreach(chart => {
      val csvFile =
        exportAsCsvFileChooser.smartSave(this)

      if (csvFile != null) {
        val outputWriter =
          new PrintWriter(
            new FileWriter(csvFile)
          )

        try {
          chart.exportAsCSV(outputWriter)

          Alerts.showInfo("CSV file saved successfully.")
        } catch {
          case ex: Exception =>
            Alerts.showException(ex, alertType = AlertType.Warning)
        } finally {
          outputWriter.close()
        }
      }
    })
  }


  title =
    "Analytics"

  maximized =
    true


  resizable =
    true


  dimensionsChoiceBox.selectionModel.value.selectFirst()
}
