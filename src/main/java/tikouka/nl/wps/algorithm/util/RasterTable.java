/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tikouka.nl.wps.algorithm.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author niels
 */
public class RasterTable {

        private String name;
	private List tables = new ArrayList();

	public void addTable(Table tab) {
		tables.add(tab);
	}

        public List getTable(){
            return this.tables;
        }

        public String getName(){
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public RasterTable(String name){
            this.name = name;
        }

}
