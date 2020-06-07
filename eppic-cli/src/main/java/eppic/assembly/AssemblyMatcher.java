package eppic.assembly;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.InterfaceFinder;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.quaternary.BioAssemblyInfo;
import org.biojava.nbio.structure.quaternary.BiologicalAssemblyBuilder;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AssemblyMatcher {

    private static final Logger logger = LoggerFactory.getLogger(AssemblyMatcher.class);

    private static final int MIN_NUM_CONTACTS = 5;
    private static final double CONTACT_OVERLAP_SCORE_CUTOFF = 0.2; // same value as in StructureInterfaceList in biojava

    private final Structure structure;
    private final CrystalAssemblies allAssemblies;
    private final StructureInterfaceList interfaces;

    public AssemblyMatcher(CrystalAssemblies allAssemblies, StructureInterfaceList interfaces) {
        this.structure = allAssemblies.getStructure();
        this.allAssemblies = allAssemblies;
        this.interfaces = interfaces;
    }

    public Assembly getMatchingAssembly(BioAssemblyInfo bioUnit) {

        BiologicalAssemblyBuilder builder = new BiologicalAssemblyBuilder();
        Structure pdbBioAssembly = builder.rebuildQuaternaryStructure(structure, bioUnit.getTransforms(), true, false);

        InterfaceFinder finder = new InterfaceFinder(pdbBioAssembly);
        StructureInterfaceList pdbInterfaces = finder.getAllInterfaces();
        pdbInterfaces.getList().removeIf(i->i.getGroupContacts().size()<MIN_NUM_CONTACTS);
        pdbInterfaces.getClusters(); // init the clusters

        List<Integer> pdbEntityIds = pdbBioAssembly.getPolyChains().stream().map(c->c.getEntityInfo().getMolId()).collect(Collectors.toList());

        for (Assembly a:allAssemblies) {
            if (areSameAssembly(a, pdbInterfaces, pdbEntityIds))
                return a;
        }

        // TODO how do we deal with this problem now?
        // if nothing returns, we still have to try whether any of our assemblies is a parent of pdbAssembly
        // We need this kind of matching for cases like 3cfh (see issue https://github.com/eppic-team/eppic/issues/47):
        //   in 3cfh the list of engaged interfaces that we detect from the PDB contains
        //   a tiny interface cluster (12) that is thrown away by our validity detector (the
        //   edge that would make the graph isomorphic is missing because it falls below the 35A2 area cutoff)
        //   Thus none of our valid assemblies match the PDB one strictly and we need to do the trick below

        logger.warn("No matching EPPIC assembly found for PDB biounit {}-{}", structure.getPDBCode(), bioUnit.getId());
        return null;
    }

    private boolean areSameAssembly(Assembly a, StructureInterfaceList pdbInterfaces, List<Integer> pdbEntityIds) {

        // TODO check what do do with other subassemblies
        SubAssembly ourSubAssembly = a.getAssemblyGraph().getSubAssemblies().get(0);

        // 1. is node stoichiometry same? if not return false
        Stoichiometry<Integer> sto = ourSubAssembly.getStoichiometry();
        Stoichiometry<Integer> pdbSto = new Stoichiometry<>(pdbEntityIds, sto.getValues());
        if (!sto.equals(pdbSto))
            return false;

        // 2. is edge (interfaces) "stoichiometry" the same?

        UndirectedGraph<ChainVertex, InterfaceEdge> ourGraph = ourSubAssembly.getConnectedGraph();
        Set<InterfaceEdge> engagedIfaces = ourGraph.edgeSet();
        if (engagedIfaces.size() != pdbInterfaces.size())
            return false;
        List<Integer> icOurs = engagedIfaces.stream().map(InterfaceEdge::getClusterId).sorted().collect(Collectors.toList());
        List<Integer> icPdb = pdbInterfaces.getList().stream().map(i->i.getCluster().getId()).collect(Collectors.toList());
        if (icOurs.stream().distinct().count() != icPdb.stream().distinct().count())
            return false;
        Map<Integer, Integer> pdbToOurs = mapInterfaceClusters(engagedIfaces, pdbInterfaces);
        // convert the ids to our ids
        List<Integer> icPdbInOurs = icPdb.stream().map(pdbToOurs::get).sorted().collect(Collectors.toList());
        // now we can compare the stoichiometry
        Stoichiometry<Integer> ifaceStoOurs = new Stoichiometry<>(icOurs, null);
        Stoichiometry<Integer> ifaceStoPdb = new Stoichiometry<>(icPdbInOurs, null);
        if (!ifaceStoOurs.equals(ifaceStoPdb))
            return false;

        // 3. is graph the same?
        UndirectedGraph<ChainVertex, InterfaceEdge> pdbGraph = createGraph(pdbInterfaces);
        if (pdbGraph == null)
            return false;
        return areIsomorphic(ourGraph, pdbGraph);
    }

    private Map<Integer, Integer> mapInterfaceClusters(Set<InterfaceEdge> refIfaces, StructureInterfaceList ifaces) {
        Map<Integer, Integer> map = new HashMap<>();
        for (StructureInterfaceCluster cluster : ifaces.getClusters()) {

            for (InterfaceEdge edge : refIfaces) {
                StructureInterfaceCluster refCluster = interfaces.get(edge.getInterfaceId()).getCluster();

                double score = refCluster.getMembers().get(0).getContactOverlapScore(cluster.getMembers().get(0), false);
                double invScore = refCluster.getMembers().get(0).getContactOverlapScore(cluster.getMembers().get(0), true);
                if (score > CONTACT_OVERLAP_SCORE_CUTOFF || invScore > CONTACT_OVERLAP_SCORE_CUTOFF) {
                    map.put(cluster.getId(), refCluster.getId());
                }
            }

        }
        for (StructureInterfaceCluster cluster : ifaces.getClusters()) {
            cluster.setId(map.get(cluster.getId()));
        }

        return map;
    }

    private static UndirectedGraph<ChainVertex, InterfaceEdge> createGraph(StructureInterfaceList ifaces) {
        UndirectedGraph<ChainVertex,InterfaceEdge> graph = new Pseudograph<>(InterfaceEdge.class);
        Map<String, ChainVertex> vertices = new HashMap<>();
        for (StructureInterface iface : ifaces) {
            if (iface.getMolecules().getFirst().length == 0 || iface.getMolecules().getSecond().length == 0) {
                logger.warn("Some chains in list of interfaces have empty atom arrays.");
                return null;
            }
            ChainVertex v = new ChainVertex();
            // TODO review if transformId is the right opId to use
            v.setOpId(iface.getTransforms().getFirst().getTransformId());
            v.setChain(iface.getMolecules().getFirst()[0].getGroup().getChain());
            vertices.put(v.getChainId()+v.getOpId(), v);

            v = new ChainVertex();
            v.setOpId(iface.getTransforms().getSecond().getTransformId());
            v.setChain(iface.getMolecules().getSecond()[0].getGroup().getChain());
            vertices.put(v.getChainId()+v.getOpId(), v);
        }

        for(ChainVertex vert : vertices.values()) {
            graph.addVertex(vert);
        }
        for(StructureInterface iface : ifaces) {
            ChainVertex source = vertices.get(iface.getMoleculeIds().getFirst() + iface.getTransforms().getFirst().getTransformId());
            ChainVertex target = vertices.get(iface.getMoleculeIds().getSecond() + iface.getTransforms().getSecond().getTransformId());
            InterfaceEdge edge = new InterfaceEdge(iface, iface.getTransforms().getSecond().getCrystalTranslation());

            graph.addEdge(source, target, edge);
        }
        return graph;
    }

    private static boolean areIsomorphic(UndirectedGraph<ChainVertex, InterfaceEdge> automGraph, UndirectedGraph<ChainVertex, InterfaceEdge> graph2) {
        // start at random node and check isomorphic in other graph
        // here we assume one of the 2 input graphs is automorphic
        Map<Integer, ChainVertex> lookupAutomGraph = new HashMap<>();
        for (ChainVertex v : automGraph.vertexSet()) {
            lookupAutomGraph.put(v.getChain().getEntityInfo().getMolId(), v);
        }
        for (ChainVertex v2 : graph2.vertexSet()) {
            ChainVertex vAutomGraph = lookupAutomGraph.get(v2.getChain().getEntityInfo().getMolId());
            List<Integer> ifaceClusterIds2 = graph2.edgesOf(v2).stream().map(InterfaceEdge::getClusterId).sorted().collect(Collectors.toList());
            List<Integer> ifaceClusterIds1 = automGraph.edgesOf(vAutomGraph).stream().map(InterfaceEdge::getClusterId).sorted().collect(Collectors.toList());
            if (!ifaceClusterIds1.equals(ifaceClusterIds2))
                return false;
        }
        return true;
    }

}
