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

package tikouka.nl.wps.algorithm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.xml.sax.SAXException;

import tikouka.nl.wps.algorithm.util.GrowFactorFacade;
import tikouka.nl.wps.algorithm.util.Table;
import tikouka.nl.wps.handler.XMLHandler;

/**
 * 
 * @author niels
 */
public class SoilErosionAlgorithm extends AbstractObservableAlgorithm {

  private List<String> errors = new ArrayList<String>();

  final AbstractProcessor processor = new DefaultProcessor( null );

  public SoilErosionAlgorithm() {
    super();
  }

  @Override
  public Class getOutputDataType( String id ) {
    return GTRasterDataBinding.class;
  }

  /*
   * @see org.n52.wps.server.IAlgorithm#getErrors()
   */
  public List<String> getErrors() {
    return errors;
  }

  public Class getInputDataType( String id ) {
    if ( id.equalsIgnoreCase( "landcover" ) || id.equalsIgnoreCase( "erosionCoefficients" ) ||
         id.equalsIgnoreCase( "rainfall" ) ) {
      return GTRasterDataBinding.class;
    } else if ( id.equalsIgnoreCase( "landcoverLookup" ) ) {
      return LiteralStringBinding.class;
    } else if ( id.equalsIgnoreCase( "rainfallExponent" ) ) {
      return LiteralDoubleBinding.class;
    } else if ( id.equalsIgnoreCase( "growFactor" ) ) {
      return LiteralDoubleBinding.class;
    } else if ( id.equalsIgnoreCase( "streamConnectivity" ) ) {
      return LiteralDoubleBinding.class;
    }
    throw new RuntimeException( "Could not find datatype for id " + id );
  }

  /*
   * @see org.n52.wps.server.IAlgorithm#run(java.util.Map)
   */
  public Map<String, IData> run( Map<String, List<IData>> inputData ) {
    // ############################################################
    // READ THE INPUT DATA
    // ############################################################
    
    // Landcover Lookup Table
    if ( inputData == null || !inputData.containsKey( "landcoverLookup" ) ) {
      throw new RuntimeException( "Error while allocating landcoverLookup parameters" );
    }
    List<IData> landcoverLookup = inputData.get( "landcoverLookup" );

    // Rainfall Exponent
    if ( inputData == null || !inputData.containsKey( "rainfallExponent" ) ) {
      throw new RuntimeException( "Error while allocating rainfallExponent parameters" );
    }
    double rainfallExponent = ((LiteralDoubleBinding)inputData.get( "rainfallExponent" ).get( 0 )).getPayload();

    // Grow Factor
    if ( inputData == null || !inputData.containsKey( "growFactor" ) ) {
      throw new RuntimeException( "Error while allocating growFactor parameters" );
    }
    double growFactor = ((LiteralDoubleBinding)inputData.get( "growFactor" ).get( 0 )).getPayload();
    
    // Stream Connectivity
    if ( inputData == null || !inputData.containsKey( "streamConnectivity" ) ) {
      throw new RuntimeException( "Error while allocating streamConnectivity parameters" );
    }
    double streamConnectivity = ((LiteralDoubleBinding)inputData.get( "streamConnectivity" ).get( 0 )).getPayload();
    
    // Landcover raster
    if ( inputData == null || !inputData.containsKey( "landcover" ) ) {
      throw new RuntimeException( "Error while allocating landcover parameters" );
    }
    GridCoverage2D landcover = ((GTRasterDataBinding)inputData.get( "landcover" ).get( 0 )).getPayload();

    // Erosion Coefficient Raster
    if ( inputData == null || !inputData.containsKey( "erosionCoefficients" ) ) {
      throw new RuntimeException( "Error while allocating erosionCoefficients parameters" );
    }
    GridCoverage2D erosionCoefficients = ((GTRasterDataBinding)inputData.get( "erosionCoefficients" ).get( 0 )).getPayload();

    // Rainfall Raster
    if ( inputData == null || !inputData.containsKey( "rainfall" ) ) {
      throw new RuntimeException( "Error while allocating rainfall parameters" );
    }
    GridCoverage2D rainfall = ((GTRasterDataBinding)inputData.get( "rainfall" ).get( 0 )).getPayload();
    // ############################################################
    // PARSE THE LOOKUPTABLE
    // ############################################################
    Map<Integer, Integer> landcoverLutList = getLookupTableData( landcoverLookup );

    // ############################################################
    // RUN THE MODEL
    // ############################################################

    Envelope2D res_env = new Envelope2D( landcover.getEnvelope2D() );
    Rectangle2D.intersect( landcover.getEnvelope2D(), erosionCoefficients.getEnvelope2D(), res_env );

    Rectangle2D.intersect( res_env, rainfall.getEnvelope2D(), res_env );

    res_env.setRect( (int)res_env.x, (int)res_env.y, (int)res_env.width, (int)res_env.height );

    // Choose width per pixel based on original image?
    // Use a supplied parameter?
    // What about interpolation algorithm?
    // Nearest neighbour by default by the looks of it
    double minX = res_env.getMinX(); // min x extent in CRS
    double maxX = res_env.getMaxX(); // max y extent in CRS
    int width = (int)landcover.getGridGeometry().getGridRange2D().getWidth(); // width in pixels
    double x_x = (maxX - minX) / width; // width of pixel in CRS

    double minY = res_env.getMinY();
    double maxY = res_env.getMaxY();
    int height = (int)landcover.getGridGeometry().getGridRange2D().getHeight();
    double y_y = (maxY - minY) / height;

    float[][] raster = new float[height][width];

    /*
     * Algorithm: a is the geology & slope information C is the land use, either woody/forest or
     * other bare ground/grass D is the connectivity to water/streams(a constant of 1 in the case of
     * New Zealand) R is the mean annual rainfall squared SE is the resulting soil erosion
     * 
     * a * grow(C,woody/forest, g) * D * r^2 * R = SE, with g and r provided by the student.
     * 
     * The grow function can be thought of as similar to buffer, but it acts on a single category
     * (ie woody/forest) within a raster and ?grows? the extent of that category by a fixed width
     * that I have specified as g in the formula, which would be provided by the student. g values
     * can be +ve or -ve, which would indicate a contraction in forest area.
     * 
     * I think SE is in t/km^2/yr/mm^2
     * 
     * Output is in millions of t/km^2/yr/mm^2, ie, a pixel of 1 means
     * 1E6 t/km^2/yr/mm^2
     */
    
    // Apply grow factor
    GrowFactorFacade growFactorFacade = new GrowFactorFacade();
    landcover = growFactorFacade.computeGrowFactor( growFactor, landcover );

    // GeometryFactory geomFac = new GeometryFactory();

    try {
      for ( int y = 0; y < height; y++ ) {
        for ( int x = 0; x < width; x++ ) {
          int[] landcoverValue = new int[1];
          double[] rainfallValue = new double[1];
          double[] erosionCoefValue = new double[1];
          double[] out = new double[1];

          // Translates pixel space to world coordinates
          Point2D pt = new DirectPosition2D( (minX + x_x / 2) + x * x_x, (minY + y_y / 2) + y * y_y );

          landcover.evaluate( pt, landcoverValue );
          rainfall.evaluate( pt, rainfallValue );
          erosionCoefficients.evaluate( pt, erosionCoefValue );

          int landcoverValueLookup = landcoverLutList.get( landcoverValue[0] );

          out[0] = landcoverValueLookup * Math.pow( rainfallValue[0], rainfallExponent ) * 
                   erosionCoefValue[0] * streamConnectivity;

          // Geotools assumes y=0 is the bottom and not the top, unless screen
          // coordinates.
          raster[height-y-1][x] = (float)out[0];
          // Infinite values cause the exporter to break. Ideally NaN would be
          // used instead but Mapserver doesn't like that.
          if ( Float.isInfinite( raster[height-y-1][x] ) ||
               Float.isNaN( raster[height-y-1][x] ) ) {
            raster[height-y-1][x] = 0.0f;
          }
          else {
            raster[height-y-1][x] /= 1000000.0f; // convert units to millions for easier viewing
          }
        }
      }
    } catch ( NullPointerException npe ) {
      npe.printStackTrace();
    } catch ( ArrayIndexOutOfBoundsException aie ) {
      aie.printStackTrace();
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    GridCoverageFactory coverageFactory = new GridCoverageFactory();

    // ############################################################
    // WRITE THE OUTPUT DATA
    // ############################################################
    HashMap<String, IData> resulthash = new HashMap<String, IData>();
    try {
      GridCoverage2D coverage = coverageFactory.create( "output", raster, res_env );

      resulthash.put( "result", new GTRasterDataBinding( coverage ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return resulthash;
  }

  private Map<Integer, Integer> getLookupTableData( List<IData> lookup ) {
    XMLHandler handler = new XMLHandler();
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

      parser.setProperty( "http://xml.org/sax/properties/lexical-handler", handler );
      parser.parse( new ByteArrayInputStream( ((LiteralStringBinding)lookup.get( 0 )).getPayload()
          .getBytes() ), handler );

    } catch ( SAXException se ) {
      se.printStackTrace();
      throw new RuntimeException( se );
    } catch ( ParserConfigurationException pe ) {
      pe.printStackTrace();
      throw new RuntimeException( pe );
    } catch ( IOException ioe ) {
      ioe.printStackTrace();
      throw new RuntimeException( ioe );
    }

    List<Table> rasterList = handler.getRasterRable().getTable();
    Map<Integer, Integer> rasterMap = new HashMap<Integer, Integer>();
    for ( Table t : rasterList ) {
      rasterMap.put( Integer.parseInt( t.getId() ), t.getIntValue() );
    }

    return rasterMap;
  }
}
