
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
	private Map<Vertex, String> mOriginVertexLabels = new HashMap<Vertex, String>();
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
		
		while(!mQ.isEmpty()) {
			addStep();
			
			Vertex u = extractMinDist();
			u.setState(ElementState.VISITED);
			
			addStep();
			
			if(mGraph.getOutEdges(u) != null) {
				for(Edge e : mGraph.getOutEdges(u)) {
					e.setState(ElementState.ACTIVE);
					
					Vertex v = e.getOtherEnd(u);
					if(mQ.contains(v)) {
						v.setState(ElementState.ACTIVE);
						
						addStep();
						
						if(updateDistance(u, v, e.getWeight()))
							addStep();
						
						v.setState(ElementState.UNVISITED);
					}
					else
						addStep();
						
					e.setState(ElementState.UNVISITED);
				}
			}
		}
		
		addStep();
		showShortestPath();
		addStep();
		
		System.out.println("FINISHED!");
	}
	
	
	private void initialize() {
		mQ = new ArrayList<Vertex>();
		mOriginVertexLabels = new HashMap<Vertex, String>();
		mDistAndPrev = new HashMap<Vertex, Pair<Vertex, Double>>();
		
		for(Vertex v : mGraph.getVertices()) {
			mOriginVertexLabels.put(v, v.getLabel());
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
			v.setLabel("<html>"+ mOriginVertexLabels.get(v) +"<br> -, "+ mDistAndPrev.get(v).getR() +"</html>");
		else
			v.setLabel("<html>"+ mOriginVertexLabels.get(v) +"<br>"+ mOriginVertexLabels.get(pair.getL()) +", "+ mDistAndPrev.get(v).getR() +"</html>");
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
	
	
	public Edge getRelevantEdge(Vertex start, Vertex end) {
		
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
	
	
}


