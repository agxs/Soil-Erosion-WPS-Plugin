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
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.AbstractAlgorithm;

import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.geotools.GTOutputFactory;
import es.unex.sextante.geotools.GTRasterLayer;
import es.unex.sextante.imageAnalysis.erosionDilation.ErosionDilationAlgorithm;
import es.unex.sextante.outputs.Output;

/**
 * 
 * @author Andrew Seales
 */
public class ErodeDilateAlgorithm extends AbstractAlgorithm {

  public ErodeDilateAlgorithm() {
    super();
  }

  private List<String> errors = new ArrayList<String>();

  public List<String> getErrors() {
    return errors;
  }

  public Class getInputDataType( String id ) {
    if ( id.equalsIgnoreCase( "landuse" ) ) {
     return GTRasterDataBinding.class;
   } else if ( id.equalsIgnoreCase( "growFactor" ) ) {
     return LiteralIntBinding.class;
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
    int growFactor = ((LiteralIntBinding)inputData.get( "growFactor" ).get( 0 )).getPayload();
    
    if ( inputData == null || !inputData.containsKey( "landuse" ) ) {
      throw new RuntimeException( "Error while allocating input parameters 'landuse'" );
    }
    GridCoverage2D landuse = ((GTRasterDataBinding)inputData.get( "landuse" ).get( 0 )).getPayload();

    HashMap<String, IData> resulthash = new HashMap<String, IData>();
    
    // Grow test
    try {
      Sextante.initialize();
      GTRasterLayer landCover = new GTRasterLayer();
      landCover.create( landuse );
      ErosionDilationAlgorithm alg = new ErosionDilationAlgorithm();
      
      ParametersSet params = alg.getParameters();
      if ( growFactor < 0 ) {
        params.getParameter( ErosionDilationAlgorithm.OPERATION )
              .setParameterValue( ErosionDilationAlgorithm.ERODE );
      }
      else if ( growFactor > 0 ) {
        params.getParameter( ErosionDilationAlgorithm.OPERATION )
              .setParameterValue( ErosionDilationAlgorithm.DILATE );
      }
      else {
        resulthash.put( "result", new GTRasterDataBinding( landuse ) );
        return resulthash;
      }
      params.getParameter( ErosionDilationAlgorithm.LAYER ).setParameterValue( landCover );
      params.getParameter( ErosionDilationAlgorithm.RADIUS ).setParameterValue( growFactor );
      
      OutputObjectsSet outputs = alg.getOutputObjects();
      Output output = outputs.getOutput( ErosionDilationAlgorithm.RESULT );
      OutputFactory outputFactory = new GTOutputFactory();
      alg.execute( null, outputFactory );
      IRasterLayer result = (IRasterLayer)output.getOutputObject();
      GridCoverage2D woody_grown = (GridCoverage2D)result.getBaseDataObject();

      resulthash.put( "result", new GTRasterDataBinding( woody_grown ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return resulthash;
  }
}
