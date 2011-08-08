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

package tikouka.nl.wps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
/**
 * A static repository to retrieve the available algorithms.
 * @author foerster
 * 05/08/2010 NH copied from LocalAlgorithmRepository to start Tikouka Repository
 *
 */
public class TikoukaAlgorithmRepository implements ITransactionalAlgorithmRepository{

    private static Logger LOGGER = Logger.getLogger(TikoukaAlgorithmRepository.class);
	private Map<String, IAlgorithm> algorithmMap;

	public TikoukaAlgorithmRepository() {
		algorithmMap = new HashMap<String, IAlgorithm>();

		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("Algorithm")){
				addAlgorithm(property.getStringValue());
			}
		}

	}

	public boolean addAlgorithms(String[] algorithms)  {
		for(String algorithmClassName : algorithms) {
			addAlgorithm(algorithmClassName);
		}
		LOGGER.info("Algorithms registered!");
		return true;

	}

	public IAlgorithm getAlgorithm(String className) {
		return algorithmMap.get(className);
	}

	public Collection<IAlgorithm> getAlgorithms() {
		return algorithmMap.values();
	}

	public Collection<String> getAlgorithmNames() {
		return new ArrayList<String>(algorithmMap.keySet());
	}

	public boolean containsAlgorithm(String className) {
		return algorithmMap.containsKey(className);
	}

	public boolean addAlgorithm(Object processID) {
		if(!(processID instanceof String)){
			return false;
		}
		String algorithmClassName = (String) processID;
		try {
			IAlgorithm algorithm = (IAlgorithm)TikoukaAlgorithmRepository.class.getClassLoader().loadClass(algorithmClassName).newInstance();
			if(!algorithm.processDescriptionIsValid()) {
				LOGGER.warn("Algorithm description is not valid: " + algorithmClassName);
				return false;
			}
			algorithmMap.put(algorithmClassName, algorithm);
			LOGGER.info("Algorithm class registered: " + algorithmClassName);


			if(algorithm.getWellKnownName().length()!=0) {
				algorithmMap.put(algorithm.getWellKnownName(), algorithm);
			}
		}
		catch(ClassNotFoundException e) {
			LOGGER.warn("Could not find algorithm class: " + algorithmClassName, e);
			return false;
		}
		catch(IllegalAccessException e) {
			LOGGER.warn("Access error occured while registering algorithm: " + algorithmClassName);
			return false;
		}
		catch(InstantiationException e) {
			LOGGER.warn("Could not instantiate algorithm: " + algorithmClassName);
			return false;
		}
		return true;

	}

	public boolean removeAlgorithm(Object processID) {
		if(!(processID instanceof String)){
			return false;
		}
		String className = (String) processID;
		if(algorithmMap.containsKey(className)){
			algorithmMap.remove(className);
			return true;
		}
		return false;
	}
}
