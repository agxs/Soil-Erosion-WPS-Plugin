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
