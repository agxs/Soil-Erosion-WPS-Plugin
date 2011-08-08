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
