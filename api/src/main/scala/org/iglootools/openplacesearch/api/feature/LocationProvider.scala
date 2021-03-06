package org.iglootools.openplacesearch.api.feature

import com.vividsolutions.jts.geom.{Coordinate, Point}

trait LocationProvider {

  def location: Point
  def longitude: Double = location.getX()
  def latitude: Double = location.getY()
  def coordinate: Coordinate = {
    location.getCoordinate()
  }

}