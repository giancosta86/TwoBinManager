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

import java.io.File
import java.util.concurrent.atomic.AtomicReference
import javafx.stage.Stage

import info.gianlucacosta.helios.apps.{AppInfo, AuroraAppInfo}
import info.gianlucacosta.helios.desktop.DesktopUtils
import info.gianlucacosta.helios.fx.application.{AppBase, SplashStage}
import info.gianlucacosta.twobinmanager.main.MainScene
import info.gianlucacosta.twobinmanager.plugins.{PluginsDirectoryWatcher, PluginsRegistry}
import info.gianlucacosta.twobinpack.icons.MainIcon
import info.gianlucacosta.twobinpack.twobinmanager.ArtifactInfo

import scalafx.Includes._
import scalafx.application.Platform


object App {
  val Directory: File =
    new File(
      DesktopUtils.homeDirectory.get,
      s".${ArtifactInfo.name}"
    )


  val PluginsDirectory: File =
    new File(
      Directory,
      "plugins"
    )


  private var pluginsRegistry: AtomicReference[PluginsRegistry] =
    new AtomicReference[PluginsRegistry](null)

  def PluginsRegistry: PluginsRegistry =
    pluginsRegistry.get()


  def PluginsRegistry_=(newValue: PluginsRegistry) = {
    pluginsRegistry.set(newValue)
  }
}

/**
  * Program's entry point
  */
class App extends AppBase(AuroraAppInfo(ArtifactInfo, MainIcon)) {
  override def startup(appInfo: AppInfo, splashStage: SplashStage, primaryStage: Stage): Unit = {
    App.Directory.mkdirs()

    AppDb.DbConnection.open()


    App.PluginsDirectory.mkdirs()

    App.PluginsRegistry =
      new PluginsRegistry(
        Some(App.PluginsDirectory)
      )

    new PluginsDirectoryWatcher(App.PluginsDirectory.toPath) {
      start()
    }

    val scene =
      new MainScene(appInfo, primaryStage)

    Platform.runLater {
      primaryStage.resizable =
        false

      primaryStage.setMinWidth(1000)
      primaryStage.setMinHeight(700)

      primaryStage.setScene(scene)

      primaryStage.title =
        appInfo.name
    }
  }

  override def stop(): Unit = {
    AppDb.DbConnection.close()
  }
}