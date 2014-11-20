import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;



public class Prims extends AbstractAlgorithm {
	
	HashMap<Vertex, Double> mDist = new HashMap<Vertex, Double>();
	HashMap<Vertex, Vertex> mPred = new HashMap<Vertex, Vertex>();
	PriorityQueue<Vertex> mQ;
	

	@Override
	public String getAlgorithmName() {
		return "Prims";
	}

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		return null;
	}
	
	@Override
	public void setRequirements(ArrayList<GraphElement> arg0) {}

	@Override
	public void perform() {
		
		init();
		
		while(!mQ.isEmpty()) {
			Vertex v = mQ.poll();
			v.setState(ElementState.ACTIVE);
			addStep("remove Vertex "+ v +" from Queue and mark it as VISITED");
			for(Edge e : mGraph.getOutEdges(v)) {
				Vertex w = mGraph.getOpposite(v, e);
				e.setState(ElementState.ACTIVE);
				addStep("check if neighbour Vertex "+ w +" is marked as VISITED");
				if(w.getState() != ElementState.VISITED) {
					w.setState(ElementState.ACTIVE);
					addStep("NO. So check if\ndist("+w+") = "+ getDist(w)+" > "+e.getWeight()+" = weight("+e+")?");
					if(mDist.get(w) > e.getWeight()) {
						mQ.remove(w);
						mDist.put(w, e.getWeight());
						mPred.put(w, v);
						mQ.add(w);
						updateLabels();
						addStep("YES. So update\ndist("+w+") = "+e.getWeight()+" = weight("+e+")!");
					}
					else {
						addStep("NO. So leave everything as it is.");
					}
					w.setState(ElementState.UNVISITED);
				}
				else {
					addStep("YES. So leave everything as it is.");
				}
				e.setState(ElementState.UNVISITED);
			}
			v.setState(ElementState.VISITED);
			addStep("Q = "+mQ);
		}
		
		printMST();
		addStep("the minimum spanning tree!");
		
	}
	
	private void init() {
		mQ = new PriorityQueue<Vertex>(mGraph.getVertexCount(), new Comparator<Vertex>() {
			@Override
			public int compare(Vertex v1, Vertex v2) {
				if(mDist.get(v1) < mDist.get(v2))
					return -1;
				else if(mDist.get(v1) > mDist.get(v2))
					return 1;
				else
					return 0;
			}
		});
		
		for(Vertex v : mGraph.getVertices()) {
			v.setState(ElementState.UNVISITED);
			mDist.put(v, Double.MAX_VALUE);
			mPred.put(v, null);
			mQ.add(v);
		}
		
		addStep("after initializing:\n================================\nQ = "+mQ);
	}
	
	private void printMST() {
		for(Vertex v : mGraph.getVertices()) {
			Vertex u = mPred.get(v);
			if(u != null)
				mGraph.getEdgesFromTo(u, v).get(0).setState(ElementState.FINISHED_AND_RELEVANT);
		}
		
		for(Edge e : mGraph.getEdges()) {
			if(e.getState() != ElementState.FINISHED_AND_RELEVANT)
				e.setState(ElementState.FINISHED_AND_NOT_RELEVANT);
		}
	}
	
	
	private String getDist(Vertex v) {
		double d = mDist.get(v);
		if(d == Double.MAX_VALUE)
			return "infinity";
		return ""+d;
	}
	
	private void updateLabels() {
		for(Vertex v : mGraph.getVertices()) {
			Vertex w = mPred.get(v);
			if(w != null)
				v.setLabelAddition(getDist(v)+", "+w);
			else
				v.setLabelAddition(getDist(v)+", -");
		}
	}
	
}
