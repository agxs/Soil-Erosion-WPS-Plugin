/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tikouka.nl.wps.algorithm.util;

/**
 *
 * @author niels
 */
public class Table {
	private String id;
	private String key;
	private Integer value;

	public Table(String id, String key, Integer value) {
		this.id = id;
		this.key = key;
                this.value = value;
	}

        public String getId() {
    return id;
    }

    public String getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

     public int getIntValue() {
        return value;
    }

}
