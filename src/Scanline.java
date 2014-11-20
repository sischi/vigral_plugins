import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;


public class Scanline extends AbstractAlgorithm {

	
	ArrayList<Vertex> mQ;
	ArrayList<Vertex> mL;
	Pair<Vertex, Vertex> closestPair;
	int l;
	int r;
	int n;
	double min;
	
	
	@Override
	public String getAlgorithmName() {
		return "Scanline";
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
		
		if(n <= 2)
			return;
		
		while(r < n) {
			mQ.get(r).setState(ElementState.VISITED);
			addStep("check if the leftest node "+mQ.get(l)+" is still in range of active node "+mQ.get(r)+":\n"
					+mQ.get(l).getLocation().getX()+" + "+min+" > "+mQ.get(r).getLocation().getX());
			if(mQ.get(l).getLocation().getX() + min <= mQ.get(r).getLocation().getX()) {
				mL.remove(mQ.get(l));
				mQ.get(l).setState(ElementState.UNVISITED);
				addStep("NO. So remove "+mQ.get(l)+" from L = "+mL);
				l++;
			}
			else {
				min = minDist();
				addStep("YES. So calculate the shortest dist between node "+mQ.get(r)+" and all others in L = "+mL+"\n the new min = "+min+" was found between "+closestPair.getL()+" and "+closestPair.getR());
				mL.add(mQ.get(r));
				mQ.get(r).setState(ElementState.ACTIVE);
				addStep("now add active node "+mQ.get(r)+" to L = "+mL);
				r++;
			}
		}
		
		
		for(Vertex v : mQ)
			v.setState(ElementState.FINISHED_AND_NOT_RELEVANT);
		
		closestPair.getL().setState(ElementState.FINISHED_AND_RELEVANT);
		closestPair.getR().setState(ElementState.FINISHED_AND_RELEVANT);
		
		addStep("the closest pair is built by "+closestPair.getL()+" and "+closestPair.getR()+" with dist of "+min);
		
		
		
	}
	
	
	private void init() {
		
		l = 0;
		r = 2;
		
		mQ = new ArrayList<Vertex>();
		mL = new ArrayList<Vertex>();
		
		Collection<Vertex> vertices = mGraph.getVertices();
		n = vertices.size();
		if(n < 2)
			return;
		
		for(Vertex v : vertices)
			mQ.add(v);
		
		Collections.sort(mQ, new Comparator<Vertex>() {
			@Override
			public int compare(Vertex v1, Vertex v2) {
				return (int) (v1.getLocation().getX() - v2.getLocation().getX());
			}
		});
		
		mL.add(mQ.get(0));
		mL.add(mQ.get(1));
		
		closestPair = new Pair<Vertex, Vertex>(mQ.get(0), mQ.get(1));
		
		mQ.get(0).setState(ElementState.ACTIVE);
		mQ.get(1).setState(ElementState.ACTIVE);

		min = calcDist(mQ.get(0).getLocation(), mQ.get(1).getLocation());
		
		addStep("after initializing: the closest pair are "+mQ.get(0)+" and "+ mQ.get(1)+"\nSo the actual min = "+min+"\nQ = "+mQ);
	}

	
	private double calcDist(Point2D p1, Point2D p2) {
		return Math.sqrt( Math.pow((p1.getX()-p2.getX()), 2) + Math.pow((p1.getY() - p2.getY()),2));
	}
	
	
	private double minDist() {
		double m = min, dist;
		Vertex u = mQ.get(r);
		
		for(Vertex v : mL) {
			dist = calcDist(u.getLocation(), v.getLocation());
			if(dist < m) {
				m = dist;
				closestPair.setL(v);
				closestPair.setR(u);
			}
		}
		
		if(m < min)
			return m;
		return min;
	}


}
