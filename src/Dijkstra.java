
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;


public class Dijkstra extends AbstractAlgorithm {

	private int mSrcVertexID;
	private int mDestVertexID;

	private ArrayList<Vertex> mQ = new ArrayList<Vertex>();
	private Map<Vertex, Pair<Vertex, Double>> mDistAndPrev = new HashMap<Vertex, Pair<Vertex, Double>>();
	
	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		ArrayList<Pair<ElementType, String>> requires = new ArrayList<Pair<ElementType, String>>();
		
		requires.add(new Pair<ElementType, String>(ElementType.VERTEX, "Source Vertex"));
		requires.add(new Pair<ElementType, String>(ElementType.OPTIONAL_VERTEX, "Destination Vertex"));
		
		return requires;
	}

	@Override
	public void setRequirements(ArrayList<Integer> requiredIDs) {
		mSrcVertexID = (int) requiredIDs.get(0);
		mDestVertexID = (int) requiredIDs.get(1);
		
		
		System.out.println("start: "+ mSrcVertexID +", dest:"+ mDestVertexID);
	}

	@Override
	public String getAlgorithmName() {
		return "Dijkstra";
	}

	@Override
	public void perform() {
		initialize();
		addStep("Q = "+ printQueue());
		
		while(!mQ.isEmpty()) {
						
			Vertex u = extractMinDist();
			u.setState(ElementState.VISITED);
			
			if(u.getId() == mDestVertexID) {
				addStep("remove Vertex '"+ u.getLabel() +"' from the Queue and mark it as 'VISITED'\nQ = "+ printQueue() +"\nThe Destination is reached");
				break;
			}
			
			addStep("remove Vertex '"+ u.getLabel() +"' from the Queue and mark it as 'VISITED'\nQ = "+ printQueue());
			
			if(mGraph.getOutEdges(u) != null) {
				for(Edge e : mGraph.getOutEdges(u)) {
					e.setState(ElementState.ACTIVE);
					
					Vertex v = e.getOtherEnd(u);
					if(mQ.contains(v)) {
						v.setState(ElementState.ACTIVE);
						
						addStep(mDistAndPrev.get(u).getR() +" + "+ e.getWeight() +" < "+ mDistAndPrev.get(v).getR() +"?");
						
						if(updateDistance(u, v, e.getWeight()))
							addStep("YES. So update the distance and predecessor of Vertex '"+ v.getLabel() +"'");
						else
							addStep("NO. So leave everything as it is");
						
						v.setState(ElementState.UNVISITED);
					}
					else
						addStep("Vertex '"+ v.getLabel() +"' was already visited. So leave everything as it is");
						
					e.setState(ElementState.UNVISITED);
				}
			}
		}

		showShortestPath();
		addStep("show the shortest path");
		
		System.out.println("FINISHED!");
	}
	
	
	private void initialize() {
		mQ = new ArrayList<Vertex>();
		mDistAndPrev = new HashMap<Vertex, Pair<Vertex, Double>>();
		
		for(Vertex v : mGraph.getVertices()) {
			if(v.getId() == mSrcVertexID)
				mDistAndPrev.put(v, new Pair<Vertex, Double>(null, 0.0));
			else
				mDistAndPrev.put(v, new Pair<Vertex, Double>(null, Double.POSITIVE_INFINITY));
			updateLabel(v);
			mQ.add(v);
			System.out.println(v.getLabel());
		}
		System.out.println(mQ);
	}
	
	
	private void updateLabel(Vertex v) {
		
		Pair<Vertex, Double> pair = mDistAndPrev.get(v);
		if(pair.getL() == null)
			v.setLabelAddition("-, "+ mDistAndPrev.get(v).getR());
		else
			v.setLabelAddition(pair.getL().getLabel() +", "+ mDistAndPrev.get(v).getR());
	}
	
	
	private Vertex extractMinDist() {
		Vertex minDist = null;
		
		for(Vertex v : mQ) {
			if(minDist == null)
				minDist = v;
			else {
				if(mDistAndPrev.get(v).getR() < mDistAndPrev.get(minDist).getR())
					minDist = v;
			}
		}
		
		minDist.setState(ElementState.VISITED);
		mQ.remove(minDist);
		return minDist;
	}
	
	
	private boolean updateDistance(Vertex u, Vertex v, double distance) {
		double newDist = mDistAndPrev.get(u).getR() + distance;
		if(newDist < mDistAndPrev.get(v).getR()) {
			mDistAndPrev.put(v, new Pair<Vertex, Double>(u, newDist));
			updateLabel(v);
			return true;
		}
		return false;
	}
	
	
	// TODO implement visualisation of the shortest path
	private void showShortestPath() {
		for(Vertex v : mGraph.getVertices())
			v.setState(ElementState.FINISHED_AND_NOT_RELEVANT);
		
		for(Edge e : mGraph.getEdges())
			e.setState(ElementState.FINISHED_AND_NOT_RELEVANT);
		
		if(mDestVertexID != -1) {
			Vertex v = mGraph.getVertexById(mDestVertexID);
			Vertex prev = mDistAndPrev.get(v).getL();
			while(prev != null) {
				v.setState(ElementState.FINISHED_AND_RELEVANT);
				
				Edge e = getRelevantEdge(prev, v);
				if(e != null)
					e.setState(ElementState.FINISHED_AND_RELEVANT);
				
				v = prev;
				prev = mDistAndPrev.get(v).getL();
			}
			v.setState(ElementState.FINISHED_AND_RELEVANT);
		}
		else {
			for(Vertex v : mGraph.getVertices()) {
				v.setState(ElementState.FINISHED_AND_RELEVANT);
				Vertex prev = mDistAndPrev.get(v).getL();
				if(prev != null) {
					Edge e = getRelevantEdge(prev, v);
					if(e != null)
						e.setState(ElementState.FINISHED_AND_RELEVANT);
				}
			}
		}
	}
	
	
	private Edge getRelevantEdge(Vertex start, Vertex end) {
		
		ArrayList<Edge> allEdgesBetween = mGraph.getEdgesFromTo(start, end);
		
		if(allEdgesBetween.isEmpty())
			return null;
		
		Edge minDist = allEdgesBetween.get(0);
		if(allEdgesBetween.size() > 1) {
			for(Edge e : allEdgesBetween) {
				if(e.getWeight() < minDist.getWeight())
					minDist = e;
			}
		}
		return minDist;
	}
	
	private String printQueue() {
		String str = "";
		
		for(Vertex v : mQ) {
			str += v.toString() +"("+ mDistAndPrev.get(v).getR() +"),  ";
		}
			
		if(str.length() > 0)
			str = str.substring(0, str.length()-3);
		return str;
	}
	
	
}


