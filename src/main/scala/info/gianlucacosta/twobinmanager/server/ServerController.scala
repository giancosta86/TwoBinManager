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

package info.gianlucacosta.twobinmanager.server

import java.net.NetworkInterface
import javafx.beans.property.{SimpleBooleanProperty, SimpleObjectProperty}
import javafx.fxml.FXML

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import info.gianlucacosta.helios.fx.dialogs.{Alerts, BusyDialog, InputDialogs}
import info.gianlucacosta.twobinmanager.sdk.server.TwoBinManagerServer
import info.gianlucacosta.twobinmanager.server.actors.{ProblemBundleActor, SolutionsUploadActor}
import info.gianlucacosta.twobinpack.core.ProblemBundle
import info.gianlucacosta.twobinpack.io.repositories.{ProblemRepository, SolutionRepository}

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.binding.Bindings
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Label, MenuItem}
import scalafx.scene.paint.Color
import scalafx.stage.{Stage, WindowEvent}


object ServerController {
  private val akkaConfigurationString =
    """
      |    akka {
      |      actor {
      |        provider = remote
      |
      |        warn-about-java-serializer-usage = false
      |      }
      |      remote {
      |        enabled-transports = ["akka.remote.netty.tcp"]
      |        netty.tcp {
      |          hostname = "%s"
      |          port = %s
      |        }
      |     }
      |    }
      |
      |
          """.stripMargin
}


class ServerController {
  var stage: Stage = _

  var problemRepository: ProblemRepository = _

  var solutionRepository: SolutionRepository = _

  val problemBundleOption =
    new SimpleObjectProperty[Option[ProblemBundle]](None)


  private val serverActive =
    new SimpleBooleanProperty(false)


  private var actorSystem: ActorSystem = _


  private lazy val solutionsUploadTable =
    new SolutionsUploadTableView(
      stage,
      solutionRepository
    )


  @FXML
  def initialize(): Unit = {
    stage.title <==
      when(serverActive) choose "Server ON" otherwise "Server OFF"

    addressField.text =
      TwoBinManagerServer.defaultConnectionParams.address

    addressField.disable <==
      serverActive

    addressesMenuButton.disable <==
      serverActive

    setupAddresses()

    portField.text =
      TwoBinManagerServer.defaultConnectionParams.port.toString

    portField.disable <==
      serverActive

    autoAcceptCheckbox.disable <==
      serverActive

    statusLabel.text <==
      when(serverActive) choose "working" otherwise "stopped"

    statusLed.fill <==
      when(serverActive) choose Color.SpringGreen otherwise Color.Red

    flipStatusButton.text <==
      when(serverActive) choose "Stop" otherwise "Start"


    rootPane.center =
      solutionsUploadTable


    solutionsUploadTable.placeholder <==
      Bindings.createObjectBinding[javafx.scene.Node](
        () => {
          new Label(
            if (serverActive()) {
              if (autoAcceptCheckbox.selected())
                "(solutions will be automatically accepted and stored into the db)"
              else
                "(no solutions to review)"
            } else
              "(Press Start to activate the server)"
          )
        },

        serverActive
      )


    {
      val handlingStage: Stage =
        stage

      handlingStage.handleEvent(WindowEvent.WindowCloseRequest) {
        (event: WindowEvent) => {

          if (canClose) {
            close()
          } else {
            event.consume()
          }

          ()
        }
      }
    }
  }


  private def setupAddresses() = {
    val availableIpAddresses: List[String] =
      NetworkInterface
        .getNetworkInterfaces
        .flatMap(networkInterface => {
          networkInterface
            .getInetAddresses
        })
        .map(_.getHostAddress)
        .filter(_.contains("."))
        .toList
        .sorted


    val menuItems =
      availableIpAddresses
        .map(address => {
          new MenuItem(address.toString) {
            onAction = (event: ActionEvent) => {
              addressField.text =
                address.toString
            }
          }
        })

    menuItems.foreach(addressesMenuButton.items.add(_))
  }


  private def canClose: Boolean = {
    solutionsUploadTable.items().isEmpty ||
      InputDialogs
        .askYesNoCancel("There are solutions to review! Do you wish to discard them?")
        .contains(true)
  }


  private def close(): Unit = {
    if (serverActive()) {
      stopServer()
    }
  }


  @FXML
  def flipStatus(): Unit = {
    try {
      if (serverActive()) {
        stopServer()
      } else {
        startServer()
      }
    } catch {
      case ex: Exception =>
        Alerts.showException(ex, alertType = AlertType.Warning)
    }
  }


  private def startServer(): Unit = {
    val problemBundle =
      problemBundleOption().get

    val akkaConfigurationString =
      ServerController
        .akkaConfigurationString
        .format(
          addressField.text(),
          portField.text()
        )


    val autoAccept =
      autoAcceptCheckbox.selected()


    val solutionsUploads =
      solutionsUploadTable.items()


    new BusyDialog(stage, "Starting the server...").run {
      val akkaConfiguration =
        ConfigFactory
          .parseString(
            akkaConfigurationString
          )

      actorSystem =
        ActorSystem(
          "TwoBinManager",
          akkaConfiguration
        )

      actorSystem.actorOf(
        Props(new ProblemBundleActor(problemBundle)),
        classOf[ProblemBundleActor].getSimpleName
      )

      actorSystem.actorOf(
        Props(
          new SolutionsUploadActor(
            problemRepository,
            solutionRepository,
            problemBundle,
            solutionsUploads,
            autoAccept
          )
        ),
        classOf[SolutionsUploadActor].getSimpleName
      )

      Platform.runLater {
        serverActive() =
          true
      }
    }
  }


  private def stopServer(): Unit = {
    new BusyDialog(stage, "Stopping the server...").run {
      Await.ready(
        actorSystem.terminate(),
        Duration.Inf
      )

      Platform.runLater {
        serverActive() =
          false
      }
    }
  }


  @FXML
  def showProblemBundle(): Unit = {
    val problemBundleText =
      problemBundleOption().get
        .problems
        .zipWithIndex
        .map {
          case (problem, problemIndex) =>
            val problemOrdinal =
              problemIndex + 1

            s"${problemOrdinal}. ${problem.name}"
        }
        .mkString("\n")

    Alerts.showInfo(problemBundleText)
  }


  @FXML
  var rootPane: javafx.scene.layout.BorderPane = _

  @FXML
  var addressField: javafx.scene.control.TextField = _

  @FXML
  var addressesMenuButton: javafx.scene.control.MenuButton = _

  @FXML
  var portField: javafx.scene.control.TextField = _

  @FXML
  var autoAcceptCheckbox: javafx.scene.control.CheckBox = _

  @FXML
  var statusLabel: javafx.scene.control.Label = _

  @FXML
  var statusLed: javafx.scene.shape.Circle = _

  @FXML
  var flipStatusButton: javafx.scene.control.Button = _
}
