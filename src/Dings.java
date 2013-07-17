import java.util.ArrayList;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.util.Pair;


public class Dings extends AbstractAlgorithm {

	@Override
	public String getAlgorithmName() {
		return "Dings";
	}

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		ArrayList<Pair<ElementType, String>> req = new ArrayList<Pair<ElementType, String>>();
		
		req.add(new Pair<ElementType, String>(ElementType.EDGE, "Ecke 1"));
		req.add(new Pair<ElementType, String>(ElementType.VERTEX, "Vertex von dems losgeht"));
		req.add(new Pair<ElementType, String>(ElementType.OPTIONAL_EDGE, "Ecke über die es evtl gehen könnte"));
		req.add(new Pair<ElementType, String>(ElementType.OPTIONAL_VERTEX, "eventueller weg"));
		req.add(new Pair<ElementType, String>(ElementType.EDGE, "Ecke 1"));
		req.add(new Pair<ElementType, String>(ElementType.VERTEX, "Vertex von dems losgeht"));
		req.add(new Pair<ElementType, String>(ElementType.OPTIONAL_EDGE, "Ecke über die es evtl gehen könnte"));
		req.add(new Pair<ElementType, String>(ElementType.OPTIONAL_VERTEX, "eventueller weg"));
		
		return req;
	}

	@Override
	public void perform() {
		System.out.println("Lass mich, ich bin noch am rechnen :(");
		
	}

	@Override
	public void setRequirements(ArrayList<GraphElement> requiredElements) {
		System.out.println("Danke für die Angaben!!!");
	}

}
