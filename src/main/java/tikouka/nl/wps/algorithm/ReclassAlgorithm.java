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
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;

import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 *
 * @author niels
 */
public class ReclassAlgorithm extends AbstractObservableAlgorithm
{
    private List<String> errors = new ArrayList<String>();
    final AbstractProcessor processor = new DefaultProcessor(null);

    public ReclassAlgorithm()
    {
        super();
    }

    public Class getOutputDataType(String id) {
        return GTRasterDataBinding.class;
    }
/*
     * @see org.n52.wps.server.IAlgorithm#getErrors()
     */
    public List<String> getErrors()
    {
        return errors;
    }

    public Class getInputDataType(String id) {
        if(id.equalsIgnoreCase("landcover")){
				return GTRasterDataBinding.class;
        }   
        else if (id.equalsIgnoreCase("valueToKeep")){
            return LiteralIntBinding.class;
        }
	throw new RuntimeException("Could not find datatype for id " + id);
    }

    /*
     * @see org.n52.wps.server.IAlgorithm#run(java.util.Map)
     */
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
       // ############################################################
        // READ THE INPUT DATA
        // ############################################################
        if(inputData==null || !inputData.containsKey("landcover")){
			throw new RuntimeException("Error while allocating input parameters 'landcover'");
		}
        GridCoverage2D landcover = ((GTRasterDataBinding) inputData.get("landcover").get(0)).getPayload();

        if(inputData==null || !inputData.containsKey("valueToKeep")){
			throw new RuntimeException("Error while allocating valueToKeep parameters");
		}
        int valueToKeep= ((LiteralIntBinding) inputData.get("valueToKeep").get(0)).getPayload();

        // ############################################################
        //  RUN THE MODEL
        // ############################################################

        Envelope2D res_env = new Envelope2D(landcover.getEnvelope2D());

        double minX = res_env.getMinX(); // min x extent in CRS
        double maxX = res_env.getMaxX(); // max y extent in CRS
        int width = (int)landcover.getGridGeometry().getGridRange2D().getWidth(); // width in pixels
        double x_x = (maxX - minX) / width; // width of pixel in CRS

        double minY = res_env.getMinY();
        double maxY = res_env.getMaxY();
        int height = (int)landcover.getGridGeometry().getGridRange2D().getHeight();
        double y_y = (maxY - minY) / height;

        BufferedImage image = new BufferedImage((int)width,(int)height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();

        /* Algorithm:
         * a is the geology & slope information
         * C is the land use, either woody/forest or other bare ground/grass
         * D is the connectivity to water/streams(a constant of 1 in the case of New Zealand)
         * R is the mean annual rainfall squared
         * SE is the resulting soil erosion
         *
         * a * grow(C,woody/forest, g) * D * r^2 * R = SE, with g and r provided by the student.
         *
         *  The grow function can be thought of as similar to buffer, but it acts on a single category
         * (ie woody/forest) within a raster and ?grows? the extent of that category by a fixed width that
         * I have specified as g in the formula, which would be provided by the student.
         * g values can be +ve or -ve, which would indicate a contraction in forest area.
         *
         * I think SE is in t/km^2/yr/mm^2
         *
         * reclass reclassifies the input raster so that the output can be buffered.
         */

        //int woodyvallookup = woodylutList.get(valuetokeep).getIntValue();

         try{
             for ( int y = 0; y < height; y++ ) {
                 for ( int x = 0; x < width; x++ ) {
                    int[] landcoverValue= new int[1];
                    double[] out = new double[1];

                    Point2D pt = new DirectPosition2D((minX + x_x / 2) + x * x_x, (minY + y_y / 2) + y * y_y);

                    landcover.evaluate(pt, landcoverValue);

                    if (landcoverValue[0] == valueToKeep){
                        out[0] = valueToKeep;
                    }else{
                        out[0] = 0;
                    }

                    raster.setPixel( x, height-y-1, out);
                }
            }
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }catch(ArrayIndexOutOfBoundsException aie){
            aie.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        GridCoverageFactory coverageFactory = new GridCoverageFactory();

        // ############################################################
        //  WRITE THE OUTPUT DATA
        // ############################################################
        HashMap<String,IData> resulthash = new HashMap<String,IData>();
        try{
            GridCoverage2D coverage = coverageFactory.create("output", raster,res_env);
            resulthash.put("result", new GTRasterDataBinding(coverage));
         }catch(Exception e){
            e.printStackTrace();
        }
        return resulthash;
    }

}
