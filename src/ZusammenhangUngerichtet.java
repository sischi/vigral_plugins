
/**
 * Algorithmus-Plugin für ViGral 
 * 
 * Dieses Plugin implementiert die Suche von Zusammenhangskomponenten auf ungerichteten Graphen.
 * Das Plugin wurde im Rahmen des Praktikums der Veranstaltung "Effizienten Algorithmen" 
 * im 1. Mastersemester erstellt.
 * 
 * Der Algorithmus wurde mithilfe der Folien zur Veranstaltung "Effizienten Algorithmen" von 
 * Prof. Dr. rer. nat. Jochen Rethmann implementiert.
 *  
 * @author Marc-Maurice Vollmer
 * @email marc-maurice.vollmer@stud.hn.de
 * @datum 16.02.2019
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import de.chiller.vigral.algorithm.AbstractAlgorithm;
import de.chiller.vigral.graph.Edge;
import de.chiller.vigral.graph.ElementState;
import de.chiller.vigral.graph.ElementType;
import de.chiller.vigral.graph.Graph;
import de.chiller.vigral.graph.GraphElement;
import de.chiller.vigral.graph.Vertex;
import de.chiller.vigral.util.Pair;

public class ZusammenhangUngerichtet extends AbstractAlgorithm {
	private String endl = "\r\n";
	private Vertex mStartVertex;
	private int mDfbZähler;
	private Map<Vertex, Integer> mDfbVerticies = new HashMap<Vertex, Integer>();
	private Map<Vertex, Integer> mLow = new HashMap<Vertex, Integer>();
	private Stack<Edge> mStack;
	private Map<Vertex, Vertex> mPred = new HashMap<Vertex, Vertex>();

	// Quick and dirty... should be extended for more components, actually max 9
	// components
	private static final Color[] componentColors = { Color.LIGHT_GRAY, Color.GREEN, Color.BLUE, Color.YELLOW,
			Color.CYAN, Color.PINK, Color.WHITE, Color.MAGENTA, Color.ORANGE };
	int componentCount = 0;

	@Override
	public ArrayList<Pair<ElementType, String>> getRequirements() {
		ArrayList<Pair<ElementType, String>> r = new ArrayList<Pair<ElementType, String>>();
		r.add(new Pair<ElementType, String>(ElementType.VERTEX, "Startknoten"));
		return r;
	}

	@Override
	public void setRequirements(ArrayList<GraphElement> requiredElements) {
		mStartVertex = (Vertex) requiredElements.get(0);
	}

	@Override
	public void perform() {

		addStep("Starte Suche von Zusammenhangskomponenten für ungerichtete Graphen");

		componentCount = 0;

		// markiere alle Knoten als unbesucht
		for (Vertex v : mGraph.getVertices()) {
			v.setState(ElementState.UNVISITED);
		}

		mDfbZähler = 1;
		addStep("Initialisiere dfbZähler mit 1");

		// initialisiere einen leeren Stack
		mStack = new Stack<Edge>();

		// Starte mit Startknoten
		zsuche(mGraph, mStartVertex);

		// jetzt für alle uebrigen Knoten v e V, die noch nicht besucht wurden:
		for (Vertex v : mGraph.getVertices()) {
			// falls v als unbesucht markiert ist
			if (v.getState() == ElementState.UNVISITED) {
				// ZSuche(v);
				zsuche(mGraph, v);
			}
		}

		// Ausgabe aller dfb und low nummern
		String dfbStr = "";
		String lowStr = "";
		for (Vertex v : mGraph.getVertices()) {
			dfbStr += "[" + v.getLabel() + "] =" + mDfbVerticies.get(v) + "\r\n";
			lowStr += "[" + v.getLabel() + "] =" + mLow.get(v) + "\r\n";
		}
		addStep("dfb: " + dfbStr);
		addStep("low: " + lowStr);
	}

	private void refreshLabel(Vertex v) {
		v.setLabelAddition(String.format("dfb=%d, low=%d", mDfbVerticies.get(v), mLow.get(v)));
	}

	private void setVertexLow(Vertex v, int value) {
		mLow.put(v, value);
		refreshLabel(v);
	}

	private void setVertexDfb(Vertex v, int value) {
		mDfbVerticies.put(v, mDfbZähler);
		refreshLabel(v);
	}

	private void zsuche(Graph aGraph, Vertex v) {
//		markiere v als besucht
		v.setState(ElementState.VISITED);
		// dfb[v] := dfbZähler
		setVertexDfb(v, mDfbZähler);

//		dfbZähler := dfbZähler + 1
		mDfbZähler++;
		addStep("Inkrementiere dfbZähler auf " + mDfbZähler);

//		low[v] := dfb[v]		
		setVertexLow(v, mDfbVerticies.get(v));
		addStep(String.format("low[%s] = dfb[%s] = %d", v.getLabel(), v.getLabel(), mDfbVerticies.get(v)));

//		betrachte alle mit v inzidenten Kanten {v,w} e E:
		for (Edge e : aGraph.getIncidentEdges(v)) {
//			lege {v,w} auf Stack, falls noch nicht geschehen
			// "falls noch nicht geschehen" bedeutet auch, das wenn eine Komponente
			// ausgegeben wurde
			// und somit die Kanten wieder vom Stack entfernt wurden, diese
			// auch zu keinem späteren Zeitpunkt mehr dem Stack hinzugefügt werden dürfen.
			// Dazu wurde der Visisted State genutzt.
			if (!mStack.contains(e) && e.getState() == ElementState.UNVISITED) {
				mStack.push(e);
				e.setState(ElementState.VISITED);
			}
//			falls w als unbesucht markiert ist:
			Vertex w = e.getOtherEnd(v);
			if (w.getState() == ElementState.UNVISITED) {
				// pred[w] := v
				mPred.put(w, v);

				// ZSuche(w) // berechnet low[w]
				zsuche(aGraph, w);

				// falls low[w] >= dfb[v]:
				if (mLow.get(w) >= mDfbVerticies.get(v)) {
					// alle Kanten bis {v,w} vom Stack nehmen und als Komponente ausgeben
					addStep(String.format("Der Knoten %s ist ein Schnittpunkt.", v.getLabel()));
					String edges = "Neue Komponente gefunden" + endl;
					edges += String.format("low[%s]=%d >= dfb[%s]=%d" + endl, w.getLabel(), mLow.get(w), v.getLabel(),
							mDfbVerticies.get(v));
					Edge stackEdge;

					Boolean componentFinished = false;
					while (!componentFinished) {
						stackEdge = mStack.pop();
						stackEdge.setCustomColor(componentColors[componentCount%10]);
						edges += String.format("{%s, %s}" + endl, stackEdge.getStartVertex().getLabel(),
								stackEdge.getEndVertex().getLabel());
						if (stackEdge == e)
							componentFinished = true;
					}

					componentCount++;
					if(componentCount >= componentColors.length) {
						componentCount = 0;
						addStep("Die Auswahl der Colorierung beginnt von vorne, da zu viele Komponenten gefunden wurden.");
					}
					addStep(edges);
				}
				// low[v] := min(low[v], low[w])
				setVertexLow(v, Math.min(mLow.get(v), mLow.get(w)));
				addStep(String.format("low[%s] = min(low[%s], low[%s]) = %d", v.getLabel(), v.getLabel(), w.getLabel(),
						mLow.get(v)));
			} // sonst: falls w != pred[v]:
			else if (w != mPred.get(v)) { // unterscheidung zwischen Rueckwaerts(!=) und Baumkante(==)
//				low[v] := min(low[v],dfb[w])
				e.setCustomLabel("RK");
				addStep(String.format("Die Kante %s-%s ist eine Rückwärtskante", e.getStartVertex(), e.getEndVertex()));
				setVertexLow(v, Math.min(mLow.get(v), mDfbVerticies.get(w)));
				addStep(String.format("low[%s] = min(low[%s], dfb[%s]) = %d", v.getLabel(), v.getLabel(), w.getLabel(),
						mLow.get(w)));
			}
		}
	}

	@Override
	public String getAlgorithmName() {
		return "Zusammenhangsproblem auf ungerichteten Graphen";
	}

}
