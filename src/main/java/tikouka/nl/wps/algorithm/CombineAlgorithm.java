/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tikouka.nl.wps.algorithm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.opengis.parameter.ParameterValueGroup;

import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 *
 * @author niels
 */
public class CombineAlgorithm extends AbstractObservableAlgorithm
{
    private List<String> errors = new ArrayList<String>();
    final AbstractProcessor processor = new DefaultProcessor(null);

    public CombineAlgorithm()
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
        if(id.equalsIgnoreCase("nz_woody")||id.equalsIgnoreCase("reclassed")){
				return GTRasterDataBinding.class;
        }
         else if (id.equalsIgnoreCase("nonreclasseablevalue")||id.equalsIgnoreCase("reclassvalue")){
            return LiteralStringBinding.class;
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
        if(inputData==null || !inputData.containsKey("nz_woody")){
			throw new RuntimeException("Error while allocating input parameters 'nz_woody'");
		}
        GridCoverage2D nz_woody = ((GTRasterDataBinding) inputData.get("nz_woody").get(0)).getPayload();

        if(inputData==null || !inputData.containsKey("reclassed")){
			throw new RuntimeException("Error while allocating input parameters 'reclassed'");
		}
        GridCoverage2D reclassed = ((GTRasterDataBinding) inputData.get("reclassed").get(0)).getPayload();
        
        if(inputData==null || !inputData.containsKey("nonreclasseablevalue")){
			throw new RuntimeException("Error while allocating input parameters 'nonreclasseablevalue'");
		}
        String nonreclasseablevalue = ((LiteralStringBinding)inputData.get("nonreclasseablevalue").get(0)).getPayload();

        if(inputData==null || !inputData.containsKey("reclassvalue")){
			throw new RuntimeException("Error while allocating input parameters 'reclassvalue'");
		}
        String reclassvalue = ((LiteralStringBinding)inputData.get("reclassvalue").get(0)).getPayload();

        // ############################################################
        //  RUN THE MODEL
        // ############################################################

        String[] nrv = nonreclasseablevalue.split(",");

         Envelope2D res_env = new Envelope2D(nz_woody.getEnvelope2D());
                Rectangle2D.intersect(nz_woody.getEnvelope2D(), reclassed.getEnvelope2D(), res_env);

        res_env.setRect((int)res_env.x, (int)res_env.y, (int)res_env.width, (int)res_env.height);

        ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(nz_woody);
        param.parameter("envelope").setValue(res_env);
        GridCoverage2D nz_woody_cr = (GridCoverage2D) processor.doOperation(param);

        param.parameter("Source").setValue(reclassed);
        param.parameter("envelope").setValue(res_env);
        GridCoverage2D reclassed_cr = (GridCoverage2D) processor.doOperation(param);

        double minX = res_env.getMinX(); // min x extent in CRS
        double maxX = res_env.getMaxX(); // max y extent in CRS
        int width = (int)nz_woody.getGridGeometry().getGridRange2D().getWidth(); // width in pixels
        double x_x = (maxX - minX) / width; // width of pixel in CRS

        double minY = res_env.getMinY();
        double maxY = res_env.getMaxY();
        int height = (int)nz_woody.getGridGeometry().getGridRange2D().getHeight();
        double y_y = (maxY - minY) / height;

        WritableRaster raster = RasterFactory.createBandedRaster( DataBuffer.TYPE_SHORT, width, height, 1, null );

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
         */

        try{
            for ( int y = 0; y < height; y++ ) {
                for ( int x = 0; x < width; x++ ) {
                    int[] woodyval= new int[1];
                    int[] reclassedval= new int[1];
                    double[] out = new double[1];

                    Point2D pt = new DirectPosition2D((minX + x_x / 2) + x * x_x, (minY + y_y / 2) + y * y_y);

                    nz_woody_cr.evaluate(pt, woodyval);
                    reclassed_cr.evaluate(pt, reclassedval);


                    if(reclassedval[0] == Integer.parseInt(reclassvalue)){
                        for (int l=0; l< nrv.length;l++){
                            if(woodyval[0] == Integer.parseInt(nrv[l])){
                                out[0] = woodyval[0];
                                break;
                            }else{
                                out[0] = Integer.parseInt(reclassvalue);
                            }
                        }
                    }else{
                        out[0]=woodyval[0];
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
