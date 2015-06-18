import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.Graph;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;
import edu.uci.ics.jung.graph.util.EdgeType;


public class PushRelabel extends AbstractAlgorithm {
	
	private Vertex mSrcVertex;
	private Vertex mDestVertex;
	
	private Graph mResidualgraph;
	
	private HashMap<Integer, Integer> mHeight = new HashMap<Integer, Integer>(); // for vertices
	private HashMap<Integer, Double> mFlow = new HashMap<Integer, Double>(); // for edges
	private HashMap<Integer, Double> mRestCapacity = new HashMap<Integer, Double>(); // for edges
	
	
	

	@Override
	public String getAlgorithmName() {
		return "Push-Relabel";
	}

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		ArrayList<Pair<ElementType, String>> requires = new ArrayList<Pair<ElementType, String>>();
		requires.add(new Pair<ElementType, String>(ElementType.VERTEX, "Source Vertex"));
		requires.add(new Pair<ElementType, String>(ElementType.VERTEX, "Destination Vertex"));
		
		return requires;
	}
	
	@Override
	public void setRequirements(ArrayList<GraphElement> requiredElements) {
		mSrcVertex = (Vertex) requiredElements.get(0);
		mDestVertex = (Vertex) requiredElements.get(1);
	}
	

	@Override
	public void perform() {
		
		createResidualGraph();
		
		// for all edges (u, v) in E do f(u, v) = 0
		for(Edge e : mResidualgraph.getEdges())
			mFlow.put(e.getId(), 0.0);
		
		
		for(Edge e : mGraph.getEdges())
			mRestCapacity.put(e.getId(), e.getMaxCapacity());
		
		// for all neighbors v of s do f(s, v) = c(s, v)
		for(Edge e : mGraph.getOutEdges(mSrcVertex)) {
			mFlow.put(e.getId(), e.getMaxCapacity());
			mFlow.put(mResidualgraph.getEdgesFromTo(mResidualgraph.getVertexById(e.getEndVertex().getId()), mResidualgraph.getVertexById(mSrcVertex.getId())).get(0).getId(), e.getMaxCapacity());
			mRestCapacity.put(e.getId(), e.getMaxCapacity() - mFlow.get(e.getId()));
		}
		
		
		// for all nodes v in V - {s} do height(v) = 0
		for(Vertex v : mGraph.getVertices())
			mHeight.put(v.getId(), 0);
		
		// height(s) = V
		mHeight.put(mSrcVertex.getId(), mGraph.getVertexCount());
		
		
		// edit labels and hide non existing edges (set custom color to white)
		updateResidualgraph();
		
		
		// mark s and t as visited
		mResidualgraph.getVertexById(mSrcVertex.getId()).setState(ElementState.VISITED);
		mSrcVertex.setState(ElementState.VISITED);
		mResidualgraph.getVertexById(mDestVertex.getId()).setState(ElementState.VISITED);
		mDestVertex.setState(ElementState.VISITED);
		
		
		// save snapshot of the graph an the resulting residual graph
		addStep(mGraph, mResidualgraph, "after initializing");
		
		// get first active node
		Vertex activeNode = getActiveNode();
		
		
		while(activeNode != null) {
			// change color of active node and save a snapshot of the resulting graph
			activeNode.setState(ElementState.ACTIVE);
			mGraph.getVertexById(activeNode.getId()).setState(ElementState.ACTIVE);
			
			addStep(mGraph, mResidualgraph, "choose Node "+ activeNode +" as active node, because:\nexcess("+ activeNode +") = "+ excess(activeNode) +" > 0");
			
			// search for an admissible edge
			Edge admissibleEdge = getAdmissibleEdge(activeNode);
			
			// if there is an existing admissible edge (v, w) ...
			if(admissibleEdge != null) {
				// change color of admissible edge in residual graph
				admissibleEdge.setState(ElementState.ACTIVE);
				
				addStep(mGraph, mResidualgraph, "residual graph:\nchoose edge "+ admissibleEdge +" as admissible edge, because:\nheight("+ admissibleEdge.getStartVertex() +") = height("+ admissibleEdge.getEndVertex() +") + 1");
				
				// change color of admissible edge in result graph
				if(mRestCapacity.containsKey(admissibleEdge.getId()))
					mGraph.getEdgeById(admissibleEdge.getId()).setState(ElementState.ACTIVE);
				else {
					int startid = admissibleEdge.getStartVertex().getId();
					int endid = admissibleEdge.getEndVertex().getId();
					mGraph.getEdgesFromTo(mGraph.getVertexById(endid), mGraph.getVertexById(startid)).get(0).setState(ElementState.ACTIVE);
				}
				
				// ... then push(v, w) ...
				push(admissibleEdge);
			}
			// ... else ...
			else {
				addStep(mGraph, mResidualgraph, "residual graph:\ncall relabel("+ activeNode +"):\nbecause Node "+ activeNode +" has no admissible edges");
				
				// ... relabel
				relabel(activeNode);
				
				updateResidualgraph();
				addStep(mGraph, mResidualgraph, "changed height of node "+ activeNode +" to minimum height of accessible nodes + 1 = "+ mHeight.get(activeNode.getId()));
			}
			
			// save snapshot of resulting graph
			updateResidualgraph();
			addStep(mGraph, mResidualgraph, "the resulting graph");

			// restore color of active node and admissible edge in resulting graph and residual graph
			if(admissibleEdge != null) {
				admissibleEdge.setState(ElementState.UNVISITED);
				if(mRestCapacity.get(admissibleEdge.getId()) != null)
					mGraph.getEdgeById(admissibleEdge.getId()).setState(ElementState.UNVISITED);
				else {
					int startid = admissibleEdge.getStartVertex().getId();
					int endid = admissibleEdge.getEndVertex().getId();
					mGraph.getEdgesFromTo(mGraph.getVertexById(endid), mGraph.getVertexById(startid)).get(0).setState(ElementState.UNVISITED);
				}
			}
			activeNode.setState(ElementState.UNVISITED);
			mGraph.getVertexById(activeNode.getId()).setState(ElementState.UNVISITED);
			
			// search for another active node
			activeNode = getActiveNode();
		}
		
		// save snapshot of resulting graphs
		addStep(mGraph, mResidualgraph, "The result, because there are no more active Nodes");
	}
	
	
	

	
	
	
	/**
	 * updates the custom labels of the vertices and edges of the resulting and residual graph
	 */
	private void updateLabels() {
		for(Edge e : mGraph.getEdges())
			e.setCustomLabel(mFlow.get(e.getId()) +" / "+ e.getMaxCapacity());
		
		for(Edge e : mResidualgraph.getEdges())
			if(mRestCapacity.containsKey(e.getId())) {
				if(mRestCapacity.get(e.getId()) != 0)
					e.setCustomLabel(""+ mRestCapacity.get(e.getId()));
				else
					e.setCustomLabel(" ");
			}
			else {
				if(mFlow.get(e.getId()) != 0)
					e.setCustomLabel(""+ mFlow.get(e.getId()));
				else
					e.setCustomLabel(" ");
			}
		
		for(Vertex v : mResidualgraph.getVertices())
			v.setLabelAddition("["+ mHeight.get(v.getId()) +"]");
		
		for(Vertex v : mGraph.getVertices())
			v.setLabelAddition(""+ excess(v));
	}
	
	
	
	
	/**
	 * hide the non-existing edges in the residual graph by changing the color to white
	 */
	private void updateResidualgraph() {
		for(Edge e : mResidualgraph.getEdges()) {
			if(mRestCapacity.containsKey(e.getId())) {
				if(mRestCapacity.get(e.getId()) == 0)
					e.setCustomColor(Color.WHITE);
				else
					e.resetCustomColor();
			}
			else {
				if(mFlow.get(e.getId()) == 0)
					e.setCustomColor(Color.WHITE);
				else
					e.resetCustomColor();
			}
		}
		
		updateLabels();
	}
	
	
	
	/**
	 * creates the residual graph
	 */
	private void createResidualGraph() {
		mResidualgraph = new Graph(mGraph);
		Collection<Edge> edges = mGraph.getEdges();
		for(Edge e : edges) {
			Vertex start = e.getStartVertex();
			Vertex end = e.getEndVertex();
			Vertex startRest = mResidualgraph.getVertexById(start.getId());
			Vertex endRest = mResidualgraph.getVertexById(end.getId());
			mResidualgraph.addEdge(Edge.EdgeFactory.getInstance().create(endRest, startRest, true), endRest, startRest, EdgeType.DIRECTED);
		}
		
		/*
		for(Edge e : mResidualgraph.getEdges())
			System.out.println(e);
		
		System.out.println(mGraph);*/
	}
	
	
	/**
	 * searches an amissible edge in the residual graph
	 * @param v the vertex to what an admissible edge is requested
	 * @return returns an edge of the residual graph
	 */
	private Edge getAdmissibleEdge(Vertex v) {
		
		for(Edge e : mResidualgraph.getOutEdges(v))
			if(mHeight.get(v.getId()) == mHeight.get(e.getEndVertex().getId()) + 1) {
				
				Double rest = mRestCapacity.get(e.getId());
				// if e is a forwardedge and it exists in the restgraph (restcapacity > 0)
				if(rest != null && rest > 0)
					return e;
				// else if e is an backwardedge and it exists in the restgraph (flow > 0)
				else if(rest == null && mFlow.get(e.getId()) > 0)
					return e;
			}
		
		return null;
	}
	
	
	/**
	 * searches an active node in the residual graph
	 * @return returns an active node of the residual graph
	 */
	private Vertex getActiveNode() {
		
		for(Vertex v : mResidualgraph.getVertices()) {
			if(v.getId() != mSrcVertex.getId() && v.getId() != mDestVertex.getId()) {
				if(excess(v) > 0)
					return v;
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * changes the flow of the given edge
	 * @param e the edge to be adjusted
	 */
	private void push(Edge e) {
		Vertex v = e.getStartVertex();
		Vertex w = e.getEndVertex();

		
		// if (v, w) is in E then... (if e is a forwardedge)
		if(mRestCapacity.containsKey(e.getId())) {
			Edge g = mResidualgraph.getEdgesFromTo(w, v).get(0);
			
			// f(v, w) = f(v, w) + min{excess_f(v), c_f(v, w)}
			double flow = mFlow.get(e.getId()) + min(excess(v), mRestCapacity.get(e.getId()));
			
			// remember old values to be able to print them later on
			double oldflowE = mFlow.get(e.getId());
			double restE = mRestCapacity.get(e.getId());
			double oldExcess = excess(v);
			
			// apply the new values
			mFlow.put(e.getId(), flow);
			mFlow.put(g.getId(), flow);
			mRestCapacity.put(e.getId(), e.getMaxCapacity() - mFlow.get(e.getId()));
			
			// save snapshot of residual graph
			updateResidualgraph();
			addStep(mGraph, mResidualgraph, "adjust flow of edge "+ e +" to "+ oldflowE +" + min("+ oldExcess +", "+ restE +") = "+ flow);
		}
		
		// ... else e = (v, w) is in E(G_f)\E(G) and e is backedge of g in E(G) ... (if e is a backwardedge)
		else {
			Edge g = mResidualgraph.getEdgesFromTo(w, v).get(0);
			
			// f(g) = f(g) - min{excess_f(v), c_f(e)}
			double flow = mFlow.get(g.getId()) - min(excess(v), mFlow.get(e.getId()));
			
			// remember old values to be able to print them later on
			double oldflowG = mFlow.get(g.getId());
			double oldflowE = mFlow.get(e.getId());
			double oldExcess = excess(v);
			
			// apply the new values
			mFlow.put(g.getId(), flow);
			mFlow.put(e.getId(), flow);
			mRestCapacity.put(g.getId(), g.getMaxCapacity() - mFlow.get(g.getId()));
			
			// save snapshot of residual graph
			updateResidualgraph();
			addStep(mGraph, mResidualgraph, "adjust flow of backedge "+ g +" to "+ oldflowG +" + min("+ oldExcess +", "+ oldflowE +") = "+ flow);
		}
	}
	
	
	/**
	 * adjust the height of the given vertex
	 * @param v the vertex to be relabeled
	 */
	private void relabel(Vertex v) {

		int min = Integer.MAX_VALUE;
		
		
		// height(v) = min{height(w)+1 | (v, w) in E_f}
		for(Edge e : mResidualgraph.getOutEdges(v)) {
			// if e is a backwardedge
			if(mRestCapacity.get(e.getId()) == null) {
				if(mFlow.get(e.getId()) > 0 && mHeight.get(e.getEndVertex().getId()) < min)
					min = mHeight.get(e.getEndVertex().getId());
			}
			// else if e is a forwardedge
			else {
				if(mRestCapacity.get(e.getId()) > 0 && mHeight.get(e.getEndVertex().getId()) < min)
					min = mHeight.get(e.getEndVertex().getId());
			}
		}
		mHeight.put(v.getId(), min+1);
	}
	
	
	/**
	 * calculates the excess value of the given vertex
	 * @param v the vertex
	 * @return returns the excess value
	 */
	private double excess(Vertex v) {
		
		double in = 0;
		double out = 0;
		
		// sum the incoming flow
		for(Edge e : mGraph.getInEdges(mGraph.getVertexById(v.getId())))
			in += mFlow.get(e.getId());
		
		// sum the outgoing flow
		for(Edge e : mGraph.getOutEdges(mGraph.getVertexById(v.getId())))
			out += mFlow.get(e.getId());
		
		if((in - out) < 0)
			return 0;
		return in - out;
	}

	
	/**
	 * calculates the minimum of two double values
	 * @param a the first double value
	 * @param b the second double value
	 * @return returns the minimum as a double value
	 */
	private double min(double a, double b) {
		if(a < b)
			return a;
		return b;
	}
	

}
