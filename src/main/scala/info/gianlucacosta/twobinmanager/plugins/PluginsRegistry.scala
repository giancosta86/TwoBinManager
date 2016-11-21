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

package info.gianlucacosta.twobinmanager.plugins

import java.io.File

import info.gianlucacosta.helios.reflections.PluginsReflections
import info.gianlucacosta.helios.services.ServiceExplorer
import info.gianlucacosta.twobinmanager.AppDb
import info.gianlucacosta.twobinmanager.sdk.analytics.{AnalyticsProvider, ChartRetriever}
import info.gianlucacosta.twobinmanager.sdk.generators.ProblemGenerator
import info.gianlucacosta.twobinmanager.sdk.importers.Importer

/**
  * Contains all the plugins for the application
  *
  * @param pluginsDirectoryOption The directory containing the JAR plugin files - or
  *                               None, if no external plugins should be loaded
  */
class PluginsRegistry(pluginsDirectoryOption: Option[File]) {
  private val serviceExplorer: ServiceExplorer =
    new ServiceExplorer(
      PluginsReflections(
        AppDb.getClass.getPackage.getName,
        pluginsDirectoryOption
      )
    )

  private val analyticsProviders: List[AnalyticsProvider] =
    serviceExplorer.findServicesOfType(classOf[AnalyticsProvider])

  val analyticsDimensions: Map[String, ChartRetriever] =
    analyticsProviders
      .flatMap(provider =>
        provider.analyticsDimensions
      )
      .toMap


  val problemGenerators: List[ProblemGenerator] =
    serviceExplorer.findServicesOfType(classOf[ProblemGenerator])
      .sortBy(_.name)


  val importers: List[Importer] =
    serviceExplorer.findServicesOfType(classOf[Importer])
}
