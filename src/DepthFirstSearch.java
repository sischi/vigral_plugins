import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;


public class DepthFirstSearch extends AbstractAlgorithm {

	private Vertex mStartVertex;
	private int dfbCounter;
	private int dfeCounter;
	private HashMap<Vertex, Integer> dfb;
	private HashMap<Vertex, Integer> dfe;
	
	@Override
	public String getAlgorithmName() {
		return "Tiefensuche";
	}

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		ArrayList<Pair<ElementType, String>> requires = new ArrayList<Pair<ElementType, String>>();
		
		requires.add(new Pair<ElementType, String>(ElementType.VERTEX, "Startknoten"));
		
		return requires;
	}
	
	@Override
	public void setRequirements(ArrayList<GraphElement> requirements) {
		mStartVertex = (Vertex) requirements.get(0);
		
	}

	@Override
	public void perform() {
		for(Vertex v : mGraph.getVertices()) {
			v.setState(ElementState.UNVISITED);
		}
		
		dfbCounter = 0;
		dfeCounter = 0;
		dfb = new HashMap<Vertex, Integer>();
		dfe = new HashMap<Vertex, Integer>();
		System.out.println("starte tiefensuche mit Vertex "+ mStartVertex);
		depthFirstSearch(mStartVertex);
	}
	
	
	private void depthFirstSearch(Vertex u) {
		u.setState(ElementState.VISITED);
		dfb.put(u, dfbCounter);
		System.out.println("Vertex "+ u +" visited");
		dfbCounter++;
		u.setLabelAddition("("+ dfb.get(u) +", %)");
		Collection<Edge> edges = mGraph.getOutEdges(u);
		addStep("D = "+ edges);
		for(Edge e : edges) {
			System.out.println("Edge "+ e);
			Vertex v = e.getOtherEnd(u);
			System.out.println("other end of Edge "+ e +" is Vertex "+ v);
			if(v.getState() == ElementState.UNVISITED) {
				e.setCustomColor(Color.decode("#FF0000")); // rot, Baumkante
				depthFirstSearch(v);
			}
			else if(v.getState() == ElementState.VISITED) {
				System.out.println("Vertex "+ v +" is already visited");
				System.out.println("dfb(v)="+ dfb.get(v) +", dfb(u)="+ dfb.get(u) +", dfe(v)="+ dfe.get(v) +", dfe(u)="+ dfe.get(u));
				if((dfb.get(v) < dfb.get(u)) && (dfe.get(v) != null))
					e.setCustomColor(Color.decode("#0000FF")); // blau, Querkante
				else if((dfb.get(v) < dfb.get(u)) && (dfe.get(v) == null))
					e.setCustomColor(Color.decode("#3FD121")); // green, Rückwärtskante
				else if(dfb.get(v) > dfb.get(u))
					e.setCustomColor(Color.decode("#F2E30F")); // gelb, Vorwärtskante
				
				System.out.println("custom color of Edge "+ e +": "+ e.getCustomColor());
			}
		}
		dfe.put(u, dfeCounter);
		dfeCounter++;
		u.setLabelAddition("("+ dfb.get(u) +", "+ dfe.get(u) +")");
		addStep("finished Vertex "+ u);
	}
	
	

}
