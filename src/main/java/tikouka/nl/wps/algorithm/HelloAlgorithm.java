/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tikouka.nl.wps.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAlgorithm;

/**
 *
 * @author niels
 */
public class HelloAlgorithm extends AbstractAlgorithm{

    private static Logger LOGGER = Logger.getLogger(HelloAlgorithm.class);

    public HelloAlgorithm() {
            super();
    }

    private List<String> errors = new ArrayList<String>();
    public List<String> getErrors() {
            return errors;
    }

    public Class getInputDataType(String id) {
        return LiteralStringBinding.class;
    }

    public Class getOutputDataType(String id) {
        return LiteralStringBinding.class;
    }

    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        if (inputData == null || !inputData.containsKey("name"))
        {
                throw new RuntimeException("Error while allocating input parameters 'name'");
        }
        String name = ((LiteralStringBinding) inputData.get("name").get(0)).getPayload();

        String result = "Hello" + name;

        HashMap<String,IData> resulthash = new HashMap<String,IData>();
        resulthash.put("result", new LiteralStringBinding(result));
        return resulthash;
    }
}
