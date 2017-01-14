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
