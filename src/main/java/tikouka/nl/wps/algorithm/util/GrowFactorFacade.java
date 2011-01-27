package tikouka.nl.wps.algorithm.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
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
    
    ReclassAlgorithm reclass = new ReclassAlgorithm();
    params.put( "nz_woody", Arrays.asList( (IData)new GTRasterDataBinding( landcover ) ) );
    params.put( "vtk", Arrays.asList( (IData)new LiteralIntBinding( 1 ) ) );
    result = reclass.run( params );
    
    params.clear();
    ErodeDilateAlgorithm erodeDilate = new ErodeDilateAlgorithm();
    params.put( "landuse", Arrays.asList( result.get( "result" ) ) );
    params.put( "growFactor", Arrays.asList( (IData)new LiteralIntBinding( (int)growFactor ) ) ); //todo convert from metres
    result = erodeDilate.run( params );
    
    params.clear();
    CombineAlgorithm combine = new CombineAlgorithm();
    params.put( "nz_woody", Arrays.asList( (IData)new GTRasterDataBinding( landcover ) ) );
    params.put( "reclassed", Arrays.asList( result.get( "result" ) ) );
    params.put( "nonreclasseablevalue", Arrays.asList( (IData)new LiteralStringBinding( "0,128,-9999" ) ) );
    params.put( "reclassvalue", Arrays.asList( (IData)new LiteralStringBinding( "1" ) ) );
    result = combine.run( params );
    
    return ((GTRasterDataBinding)result.get("result" )).getPayload();
  }
}
