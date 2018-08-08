package eppic.assembly.gui;

import eppic.EppicParams;
import eppic.assembly.Assembly;
import eppic.assembly.CrystalAssemblies;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.TestLatticeGraph;
import eppic.assembly.layout.LayoutUtils;
import org.biojava.nbio.structure.StructureException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TestAssemblyDiagramThumbMustache {

    @Test
    public void testTemplatePopulatesCorrectly() throws StructureException, IOException {

        String pdbId = "1smt";
        CrystalAssemblies ab = TestLatticeGraph.getCrystalAssemblies(pdbId);

        Assembly a = ab.getUniqueAssemblies().get(0);

        LatticeGraph3D latticeGraph = new LatticeGraph3D(ab.getLatticeGraph());

        LatticeGUIMustache guiThumb = new LatticeGUIMustache(LatticeGUIMustache.TEMPLATE_ASSEMBLY_DIAGRAM_THUMB, latticeGraph);
        guiThumb.setLayout2D(LayoutUtils.getDefaultLayout2D(latticeGraph.getCrystalCell()));
        guiThumb.setTitle("Assembly "+a.getId());
        guiThumb.setPdbId(pdbId);
        int dpi = 72; // 72 dots per inch for output
        // size is in inches
        guiThumb.setSize(String.valueOf((double) EppicParams.THUMBNAILS_SIZE/(double)dpi));
        guiThumb.setDpi(String.valueOf(dpi));

        StringWriter dotStringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(dotStringWriter);
        guiThumb.execute(pw);

        String dotStr = dotStringWriter.toString();

        // Here we test that the output of the template contains the 2d layout info that graphviz needs to draw the graph
        // For instance this depends on method LatticeGuiMustache.getGraph2D() existing
        assertTrue(dotStr.contains("\"A_0\" ["));
        assertTrue(dotStr.contains("\"B_1\" ["));
        assertTrue(dotStr.contains("\"B_0\" -> \"A_0\""));
        //System.out.println(dotStr);
    }
}
