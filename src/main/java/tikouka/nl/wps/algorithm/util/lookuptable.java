/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tikouka.nl.wps.algorithm.util;

/**
 *
 * @author niels
 */
public class lookuptable {

    private String id;
    private String key;
    private double value;

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public lookuptable() {
        id = "";
        key = "";
    }

    public lookuptable(String pId, String pKey, double pValue) {
        id = pId;
        key = pKey;
        value = pValue;

    }
}
