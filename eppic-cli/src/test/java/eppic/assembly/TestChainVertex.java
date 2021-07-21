package eppic.assembly;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.junit.Test;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class TestChainVertex {

    /**
     * Test that ChainVertex and subclasses can be used as a key
     */
    @Test
    public void testChainVertexAsKey() {
        Map<ChainVertex, Integer> map = new HashMap<>();
        Chain dummyChain = new ChainImpl();
        dummyChain.setId("A");
        dummyChain.setName("A");
        ChainVertex v = new ChainVertex(dummyChain, 0);

        map.put(v, 1);

        ChainVertex v2 = new ChainVertex(dummyChain, 0);

        Integer value = map.get(v2);
        assertEquals(new Integer(1), value);

        ChainVertex3D v3 = new ChainVertex3D(dummyChain, 0);

        value = map.get(v3);
        assertEquals(new Integer(1), value);
    }

    /**
     * Making sure that matrix composition within a map works as expected
     */
    @Test
    public void testMatrixMultInMap() {
        Map<Integer, Matrix4d> map = new HashMap<>();
        Matrix4d m = new Matrix4d();
        m.set(1, new Vector3d(1,0,0));
        map.put(1, m);
        Matrix4d op = new Matrix4d();
        op.set(1, new Vector3d(2, 0, 0));
        map.get(1).mul(op, map.get(1));

        assertEquals(3, map.get(1).m03, 0.000001);
    }
}
