import java.util.ArrayList;
import java.util.HashMap;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;


public class BellmanFord extends AbstractAlgorithm {

	private Vertex mSrcVertex;
	private HashMap<Integer, Double> mDist = new HashMap<Integer, Double>();
	private HashMap<Integer, Integer> mPred = new HashMap<Integer, Integer>();
	
	@Override
	public String getAlgorithmName() {
		return "Bellman/Ford";
	}

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		ArrayList<Pair<ElementType, String>> r = new ArrayList<Pair<ElementType, String>>();
		r.add(new Pair<ElementType, String>(ElementType.VERTEX, "source"));
		return r;
	}
	
	@Override
	public void setRequirements(ArrayList<GraphElement> r) {
		mSrcVertex = (Vertex) r.get(0);
	}

	@Override
	public void perform() {
		
		init();
		
		for(int i = 1; i <= mGraph.getVertexCount() - 1; i++) {
			addStep("lets start round "+ i+"\n============================\n");
			for(Edge e : mGraph.getEdges()) {
				
				e.setState(ElementState.ACTIVE);
				addStep("choose edge "+ e);
				Vertex start = e.getStartVertex();
				Vertex end = e.getEndVertex();
				
				end.setState(ElementState.ACTIVE);
				addStep("check if d["+end.getLabel()+"] > d["+start.getLabel()+"] + "+ e.getWeight());
				if(mDist.get(end.getId()) > mDist.get(start.getId()) + e.getWeight()) {
					mDist.put(end.getId(), mDist.get(start.getId()) + e.getWeight());
					mPred.put(end.getId(), start.getId());
					updateLabel(end);
					addStep("yes it is! so update d["+end.getLabel()+"] = d["+start.getLabel()+"] + "+ e.getWeight() +"="+mDist.get(end.getId()));
				}
				end.setState(ElementState.UNVISITED);
				
				if(!e.isDirected()) {
					start.setState(ElementState.ACTIVE);
					addStep("check if d["+start.getLabel()+"] > d["+end.getLabel()+"] + "+ e.getWeight());
					if(mDist.get(start.getId()) > mDist.get(end.getId()) + e.getWeight()) {
						mDist.put(start.getId(), mDist.get(end.getId()) + e.getWeight());
						mPred.put(start.getId(), end.getId());
						updateLabel(start);
						addStep("yes it is! so update d["+start.getLabel()+"] = d["+end.getLabel()+"] + "+ e.getWeight() +"="+mDist.get(start.getId()));
					}
					start.setState(ElementState.UNVISITED);
				}
				e.setState(ElementState.UNVISITED);
				//updateLabels();
				addStep("result of edge "+ e);
			}
		}
		
		boolean negativeCycleDetected = false;
		addStep("checking for negative-weight cycles\n============================\n");
		for(Edge e : mGraph.getEdges()) {
			Vertex start = e.getStartVertex();
			Vertex end = e.getEndVertex();
			if(mDist.get(end.getId()) > mDist.get(start.getId()) + e.getWeight()) {
				addStep("negative-weight cycle exists!");
				negativeCycleDetected = true;
				break;
			}
			if(!e.isDirected()) {
				if(mDist.get(start.getId()) > mDist.get(end.getId()) + e.getWeight()) {
					addStep("negative-weight cycle exists!");
					negativeCycleDetected = true;
					break;
				}
			}
		}
		if(negativeCycleDetected)
			drawErrorState();
		else
			drawShortestPaths();
		
		addStep("the result!");
	}

	
	private void init() {
		for(Vertex v : mGraph.getVertices()) {
			mDist.put(v.getId(), Double.MAX_VALUE);
			mPred.put(v.getId(), -1);
			v.setLabelAddition("infty, -");
		}
		
		mDist.put(mSrcVertex.getId(), 0.0);
		mSrcVertex.setLabelAddition("0, -");
		
	}
	
	private void updateLabel(Vertex v) {
		Double d = mDist.get(v.getId());
		Vertex pred = mGraph.getVertexById(mPred.get(v.getId()));
		if(pred == null)
			v.setLabelAddition("infty, -");
		else
			v.setLabelAddition(d+", "+pred.getLabel());
	}
	
	private void updateLabels() {
		for(Vertex v : mGraph.getVertices()) {
			Double d = mDist.get(v.getId());
			Vertex pred = mGraph.getVertexById(mPred.get(v.getId()));
			if(pred == null)
				v.setLabelAddition("infty, -");
			else
				v.setLabelAddition(d+", "+pred.getLabel());
		}
	}
	
	
	private void drawShortestPaths() {
		for(Vertex v : mGraph.getVertices()) {
			Vertex pred = mGraph.getVertexById(mPred.get(v.getId()));
			ArrayList<Edge> edges = mGraph.getEdgesFromTo(pred, v);
			if(edges != null && edges.size() > 0)
				edges.get(0).setState(ElementState.FINISHED_AND_RELEVANT);
		}
		
		for(Edge e : mGraph.getEdges()) {
			if(e.getState() != ElementState.FINISHED_AND_RELEVANT)
				e.setState(ElementState.FINISHED_AND_NOT_RELEVANT);
		}
		
		for(Vertex v : mGraph.getVertices()) {
			if(mDist.get(v.getId()) != Double.MAX_VALUE)
				v.setState(ElementState.FINISHED_AND_RELEVANT);
		}
	}
	
	private void drawErrorState() {
		for(Vertex v : mGraph.getVertices())
			v.setState(ElementState.FINISHED_AND_NOT_RELEVANT);
		for(Edge e : mGraph.getEdges())
			e.setState(ElementState.FINISHED_AND_NOT_RELEVANT);
	}

}
