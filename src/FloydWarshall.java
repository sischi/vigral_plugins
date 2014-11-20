import java.util.ArrayList;
import java.util.HashMap;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;


public class FloydWarshall extends AbstractAlgorithm {

	
	private ArrayList<Vertex> mVertices = new ArrayList<Vertex>();
	int mN;
	double[][] mDist;
	private HashMap<Vertex, Integer> mIndizes = new HashMap<Vertex, Integer>();
	private HashMap<Vertex, Vertex> mPred = new HashMap<Vertex, Vertex>();
	
	@Override
	public String getAlgorithmName() {
		return "Floyd/Warshall";
	}

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		return null;
	}
	
	@Override
	public void setRequirements(ArrayList<GraphElement> r) {}

	
	
	@Override
	public void perform() {
		
		init();
		
		for(int k = 0; k < mN; k++) {
			addStep("k = "+ k+ "\n====================\n");
			for(int i = 0; i < mN; i++) {
				for(int j = 0; j < mN; j++) {
					ArrayList<Edge> edges = mGraph.getEdgesFromTo(mVertices.get(i), mVertices.get(j));
					
					checkPath(i, j, k, edges);
					
					addStep("check if dist between "+mVertices.get(i)+" and "+mVertices.get(j)+" is shorter over "+mVertices.get(k)+":\n"
							+getDist(i, j)+" > "+getDist(i, k)+" + "+getDist(k, j)+"?");
					if(mDist[i][j] > mDist[i][k] + mDist[k][j]) {
						mDist[i][j] = mDist[i][k] + mDist[k][j];
						addStep("Yes, so update dist between "+mVertices.get(i)+" and "+mVertices.get(j)+" to "+getDist(i, j));
					}
					
					uncheckPath(i, j, k, edges);
				}
			}
			addStep(printDists());
		}
		
	}

	
	
	private void init() {
		String expl = "after initialisation:\n==========================\n";
		for(Vertex v : mGraph.getVertices()) {
			mIndizes.put(v, mVertices.size());
			mVertices.add(v);
			expl += v+"="+(mVertices.size()-1)+"\n";
		}
		
		mN = mVertices.size();
		mDist = new double[mN][mN];
		
		for(int i = 0; i < mN; i++)
			for(int j = 0; j < mN; j++)
				if(i == j)
					mDist[i][j] = 0;
				else
					mDist[i][j] = Double.MAX_VALUE;

		for(Edge e : mGraph.getEdges()) {
			Vertex start = e.getStartVertex();
			Vertex end = e.getEndVertex();
			
			int starti = mIndizes.get(start);
			int endi = mIndizes.get(end);
			
			mDist[starti][endi] = e.getWeight();
			
			
			if(!e.isDirected()) {
				mDist[endi][starti] = e.getWeight();
			}
		}
		
		addStep(expl);
	}
	
	
	private String printDists() {
		String s = "";
		for(int i = 0; i < mVertices.size(); i++) {
			for(int j = 0; j < mVertices.size(); j++) {
				s += "d["+mVertices.get(i)+"]["+mVertices.get(j)+"] = "+getDist(i, j)+"\n";
			}
		}
		return s;
	}
	
	private String getDist(int i, int j) {
		if(mDist[i][j] == Double.MAX_VALUE)
			return "infinity";
		return ""+mDist[i][j];
	}
	
	private void checkPath(int i, int j, int k, ArrayList<Edge> edges) {
		mVertices.get(i).setState(ElementState.ACTIVE);
		if(j != i)
			mVertices.get(j).setState(ElementState.ACTIVE);
		if(k != j && k != i)
			mVertices.get(k).setState(ElementState.VISITED);
		if(!edges.isEmpty())
			edges.get(0).setState(ElementState.ACTIVE);
		
	}
	
	private void uncheckPath(int i, int j, int k, ArrayList<Edge> edges) {
		mVertices.get(i).setState(ElementState.UNVISITED);
		if(i != j)
			mVertices.get(j).setState(ElementState.UNVISITED);
		if(k != j && k != i)
			mVertices.get(k).setState(ElementState.UNVISITED);
		if(!edges.isEmpty())
			edges.get(0).setState(ElementState.UNVISITED);
	}
	
}
