package org.drugis.mtc.parameterization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drugis.mtc.graph.GraphUtil;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.search.BreadthFirstSearch;
import org.drugis.mtc.search.DepthFirstSearch;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class InconsistencyParameterization implements Parameterization {
	/**
	 * Get the classes of fundamental cycles that share the same inconsistency factor (if any).
	 * The fundamental cycles are those cycles that are generated by the given spanning tree in the given study graph.
	 * @param studyGraph The comparison graph.
	 * @param tree Spanning tree of the comparison graph.
	 * @return
	 */
	public static Map<Partition, Set<List<Treatment>>> getCycleClasses(UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph, Tree<Treatment, FoldedEdge<Treatment, Study>> tree) {
		Set<FoldedEdge<Treatment, Study>> nonTreeEdges = new HashSet<FoldedEdge<Treatment,Study>>(cGraph.getEdges());
		nonTreeEdges.removeAll(tree.getEdges());

		Map<Partition, Set<List<Treatment>>> cycleClasses = new HashMap<Partition, Set<List<Treatment>>>();

		// Each of the non-tree edges generates a fundamental cycle in the comparison graph.
		// We classify the fundamental cycles according to their reduced partitions.
		for (FoldedEdge<Treatment,Study> edge : nonTreeEdges) {
			Pair<Treatment> vertices = new Pair<Treatment>(cGraph.getIncidentVertices(edge));
			List<Treatment> cycle = GraphUtil.findPath(tree, vertices.getFirst(), vertices.getSecond());
			cycle.add(vertices.getFirst());
			cycle = standardizeCycle(cycle);
			
			List<Part> parts = new ArrayList<Part>(cycle.size() - 1);
			for (int i = 1; i < cycle.size(); ++i) {
				parts.add(new Part(cycle.get(i - 1), cycle.get(i), cGraph.findEdge(cycle.get(i - 1), cycle.get(i)).getFolded()));
			}
			Partition partition = new Partition(parts).reduce();
			
			if (!cycleClasses.containsKey(partition)) {
				cycleClasses.put(partition, new HashSet<List<Treatment>>());	
			}
			cycleClasses.get(partition).add(cycle);
		}
		
		return cycleClasses;
	}

	/**
	 * Determine the inconsistency degree given a classification of cycles into classes with equivalent reductions.
	 * @see getCycleClasses
	 */
	public static int getInconsistencyDegree(Map<Partition, Set<List<Treatment>>> cycleClasses) {
		int icd = 0;
		for (Partition p : cycleClasses.keySet()) {
			if (p.getParts().size() >= 3) {
				++icd;
			}
		}
		return icd;
	}
	
	/**
	 * Find a baseline assignment that covers all comparisons.
	 * @param studies List of studies to find baselines for.
	 * @param cGraph Comparison graph.
	 */
	public static Map<Study, Treatment> findStudyBaselines(Collection<Study> studies, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph) {
		return new DepthFirstSearch<Map<Study, Treatment>>().search(new InconsistencyBaselineSearchProblem(studies, cGraph));
	}
	
	/**
	 * Find a baseline assignment suitable for the given cycle classes.
	 * @param studies List of studies to find baselines for.
	 * @param cGraph Comparison graph.
	 * @param cycleClasses Cycle classes.
	 */
	public static Map<Study, Treatment> findStudyBaselines(Collection<Study> studies, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph, Map<Partition, Set<List<Treatment>>> cycleClasses) {
		return new DepthFirstSearch<Map<Study, Treatment>>().search(new InconsistencyBaselineSearchProblem(studies, cGraph, cycleClasses));
	}
	
	/**
	 * Standardize the given cycle by making the "least" treatment the first element,
	 * and by making the "least" neighbor of the first element the second element.
	 * @see TreatmentComparator
	 * @param cycle The cycle to standardize.
	 * @return Standardized version of the given cycle.
	 */
	public static List<Treatment> standardizeCycle(List<Treatment> cycle) {
		assertCycle(cycle);
		
		// find the least treatment
		Treatment least = TreatmentComparator.findLeast(cycle);
		
		// start the cycle from the least treatment
		List<Treatment> std = new ArrayList<Treatment>();
		std.addAll(cycle.subList(cycle.indexOf(least), cycle.size()));
		std.addAll(cycle.subList(1, cycle.indexOf(least) + 1));
		
		// reverse the order if necessary
		if (TreatmentComparator.INSTANCE.compare(std.get(1), std.get(std.size() - 2)) > 0) {
			Collections.reverse(std);
		}
		
		return std;
	}

	/**
	 * Check that the given list represents a cycle.
	 */
	private static void assertCycle(List<Treatment> cycle) {
		Set<Treatment> set = new HashSet<Treatment>(cycle);
		if (set.size() != cycle.size() - 1 || !cycle.get(0).equals(cycle.get(cycle.size() - 1))) {
			throw new IllegalStateException(cycle + " is not a cycle.");
		}
	}

	private Tree<Treatment, FoldedEdge<Treatment, Study>> d_tree;
	private Map<Study, Treatment> d_baselines;
	
	/**
	 * Construct an inconsistency parameterization with the given spanning tree and study baselines.
	 * @param network Network to parameterize.
	 * @param tree Spanning tree that defines the basic parameters.
	 * @param baselines Map of studies to baseline treatments.
	 */
	public InconsistencyParameterization(Network network, Tree<Treatment, FoldedEdge<Treatment, Study>> tree, Map<Study, Treatment> baselines) {
		d_tree = tree;
		d_baselines = baselines;
	}
	
	@Override
	public List<NetworkParameter> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Treatment getStudyBaseline(Study s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<NetworkParameter, Integer> parameterize(Treatment base, Treatment subj) {
		// TODO Auto-generated method stub
		return null;
	}

}
