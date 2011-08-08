/*
 * Copyright 2011 by EDINA, University of Edinburgh, Landcare Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tikouka.nl.wps.algorithm.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import tikouka.nl.wps.algorithm.CombineAlgorithm;
import tikouka.nl.wps.algorithm.ErodeDilateAlgorithm;
import tikouka.nl.wps.algorithm.ReclassAlgorithm;

/**
 * This class is a facade over the ReclassAlgorithm, ErodeDilateAlgorithm,
 * CombineAlgorithm to perform growth or erosion on the woody data in the land
 * cover raster. This class chains the three operations together to first
 * extract the woody part, grow or erode the later, then recombine the grown
 * layer making sure to now grow woody data over invalid areas, such as water.
 * 
 * @author Andrew Seales
 */
public class GrowFactorFacade {
  /**
   * 
   * @param growFactor grow factor in metres. Positive values represent growth,
   *                   negative values represent erosion.
   * @param landcover  landcover raster
   * @return
   */
  public GridCoverage2D computeGrowFactor( double growFactor, GridCoverage2D landcover ) {
    if ( growFactor == 0.0 ) {
      return landcover;
    }
    if ( landcover == null ) {
      throw new NullPointerException( "Landcover parameter cannot be null." );
    }
    Map<String, List<IData>> params = new HashMap<String, List<IData>>();
    Map<String, IData> result;

    // Start computing algorithms
    ReclassAlgorithm reclass = new ReclassAlgorithm();
    params.put( "landcover", Arrays.asList( (IData)new GTRasterDataBinding( landcover ) ) );
    params.put( "valueToKeep", Arrays.asList( (IData)new LiteralIntBinding( 1 ) ) );
    result = reclass.run( params );
    
    params.clear();
    ErodeDilateAlgorithm erodeDilate = new ErodeDilateAlgorithm();
    params.put( "landcover", Arrays.asList( result.get( "result" ) ) );
    params.put( "growFactor", Arrays.asList( (IData)new LiteralDoubleBinding( growFactor ) ) );
    result = erodeDilate.run( params );
    
    params.clear();
    CombineAlgorithm combine = new CombineAlgorithm();
    params.put( "originalLandcover", Arrays.asList( (IData)new GTRasterDataBinding( landcover ) ) );
    params.put( "reclassedLandcover", Arrays.asList( result.get( "result" ) ) );
    params.put( "nonReclassableValue", Arrays.asList( (IData)new LiteralStringBinding( "0,128,-9999" ) ) );
    params.put( "reclassableValue", Arrays.asList( (IData)new LiteralStringBinding( "1" ) ) );
    result = combine.run( params );
    
    return ((GTRasterDataBinding)result.get( "result" )).getPayload();
  }
}
