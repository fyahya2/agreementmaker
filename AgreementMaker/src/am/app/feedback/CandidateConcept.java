package am.app.feedback;

import am.app.mappingEngine.AbstractMatcher.alignType;
import am.app.ontology.Node;

/**
 * This class extends the Alignment class in order to add more information to be used
 * in candidate selection
 * @author cosmin
 *
 */

public class CandidateConcept extends Node implements Comparable<CandidateConcept>{

	protected double relevance = 0.00;
	protected boolean patternRepeats = false;
	
	public enum ontology {
		source,
		target
	}
	
	protected ontology whichOntology;
	protected alignType whichType;
	protected Node originalNode;
	
	public CandidateConcept(Node n, double r, ontology o, alignType t ) {
		super(n.getIndex(), n.getResource(), n.getType());
		relevance = r;
		whichOntology = o;
		whichType = t;
		originalNode = n;
	}

	public double getRelevance() {
		return relevance;
	}
	
	public void setRelevance(double r){
		relevance = r;
	}
	
	public Node getNode(){
		return originalNode;
	}
	
	// to allow candidate concepts to be sorted (required by the Comparable)
	public int compareTo(CandidateConcept cc) {
		if( cc.getClass() != CandidateConcept.class ) {
			return 0;
		}
		if( cc.getRelevance() == relevance ) {
			return 0;  // equal
		}else if( relevance > cc.getRelevance() ) {
			return 1; // greater
		}else {
			return -1;
		}
	}
	
	public boolean isOntology( ontology o ) {
		if( whichOntology == o ) {
			return true;
		}
		return false;
	}

	public boolean isType( alignType t ) {
		if( whichType == t ) {
			return true;
		}
		return false;
	}
	
	public boolean equals( Node n ) {
		return super.equals( n );
	}
	
	public void setPatternRepeats( boolean p ) {
		patternRepeats = p;
	}
	
	public boolean getPatternRepeats(){
		return patternRepeats;
	}
	
}
