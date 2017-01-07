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

package info.gianlucacosta.twobinmanager.plugins

import org.scalatest.{FlatSpec, Matchers}

class TestPluginsRegistry extends FlatSpec with Matchers {
  "The bundled analytics dimensions" should "NOT be empty" in {
    val pluginsRegistry =
      new PluginsRegistry(None)

    pluginsRegistry.analyticsDimensions should not be empty
  }


  "The bundled problem generators" should "NOT be empty" in {
    val pluginsRegistry =
      new PluginsRegistry(None)

    pluginsRegistry.problemGenerators should not be empty
  }


  "The bundled importers" should "NOT be empty" in {
    val pluginsRegistry =
      new PluginsRegistry(None)

    pluginsRegistry.importers should not be empty
  }
}
