package ch.systemsx.sybit.server.db.entitylisteners;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An adapter to serialize Double.NaN to null. In xml that's no problem but
 * in JSON NaN is not supported.
 * This converts the NaN to null with the effect that the value is not
 * serialized at all (field not present in output).
 */
public class DoubleNaNXmlAdapter extends XmlAdapter<Double, Double> {

    public Double unmarshal(Double val) throws Exception {
        if (val == null) return Double.NaN;
        return val;
    }

    public Double marshal(Double val) throws Exception {
        if (Double.isNaN(val)) return null;
        return val;
    }
}
