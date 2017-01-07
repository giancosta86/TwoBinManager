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

package info.gianlucacosta.twobinmanager.db;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Item in the block pool - value object
 */
@Embeddable
public class BlockPoolItemValue {
    @Column(
            nullable = false
    )
    private int width;

    @Column(
            nullable = false
    )
    private int height;

    @Column(
            nullable = false
    )
    private int quantity;

    public BlockPoolItemValue() {
    }

    public BlockPoolItemValue(int width, int height, int quantity) {
        this.width = width;
        this.height = height;
        this.quantity = quantity;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getQuantity() {
        return quantity;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPoolItemValue that = (BlockPoolItemValue) o;
        return width == that.width &&
                height == that.height &&
                quantity == that.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, quantity);
    }
}
