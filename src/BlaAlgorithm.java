import java.util.ArrayList;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.util.Pair;


public class BlaAlgorithm extends AbstractAlgorithm {

	@Override
	public String getAlgorithmName() {
		return "Bla-Algorithmus";
	}

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		return null;
	}

	@Override
	public void perform() {
		System.out.println("jetz mach ich hier die übelste Welle!");
		
	}

	@Override
	public void setRequirements(ArrayList<GraphElement> requiredElements) {
	}

}
