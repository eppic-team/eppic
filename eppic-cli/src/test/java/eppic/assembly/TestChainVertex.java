package eppic.assembly;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.junit.Test;
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
        dummyChain.setInternalChainID("A");
        dummyChain.setChainID("A");
        ChainVertex v = new ChainVertex(dummyChain, 0);

        map.put(v, 1);

        ChainVertex v2 = new ChainVertex(dummyChain, 0);

        Integer value = map.get(v2);
        assertEquals(new Integer(1), value);

        ChainVertex3D v3 = new ChainVertex3D(dummyChain, 0);

        value = map.get(v3);
        assertEquals(new Integer(1), value);
    }
}
