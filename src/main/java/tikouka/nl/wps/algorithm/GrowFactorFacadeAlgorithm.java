/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tikouka.nl.wps.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractAlgorithm;

import tikouka.nl.wps.algorithm.util.GrowFactorFacade;

/**
 * 
 * @author Andrew Seales
 */
public class GrowFactorFacadeAlgorithm extends AbstractAlgorithm {

  public GrowFactorFacadeAlgorithm() {
    super();
  }

  private List<String> errors = new ArrayList<String>();

  public List<String> getErrors() {
    return errors;
  }

  public Class getInputDataType( String id ) {
    if ( id.equalsIgnoreCase( "landcover" ) ) {
     return GTRasterDataBinding.class;
   } else if ( id.equalsIgnoreCase( "growFactor" ) ) {
     return LiteralDoubleBinding.class;
   }
   throw new RuntimeException( "Could not find datatype for id " + id );
  }

  public Class getOutputDataType( String id ) {
    return GTRasterDataBinding.class;
  }

  public Map<String, IData> run( Map<String, List<IData>> inputData ) {
    if ( inputData == null || !inputData.containsKey( "growFactor" ) ) {
      throw new RuntimeException( "Error while allocating input parameters 'growFactor'" );
    }
    double growFactor = ((LiteralDoubleBinding)inputData.get( "growFactor" ).get( 0 )).getPayload();
    
    if ( inputData == null || !inputData.containsKey( "landcover" ) ) {
      throw new RuntimeException( "Error while allocating input parameters 'landcover'" );
    }
    GridCoverage2D landCover = ((GTRasterDataBinding)inputData.get( "landcover" ).get( 0 )).getPayload();

    HashMap<String, IData> resulthash = new HashMap<String, IData>();
    
    GrowFactorFacade growFactorFacade = new GrowFactorFacade();
    landCover = growFactorFacade.computeGrowFactor( growFactor, landCover );
    
    resulthash.put( "result", new GTRasterDataBinding( landCover ) );
    return resulthash;
  }
}
