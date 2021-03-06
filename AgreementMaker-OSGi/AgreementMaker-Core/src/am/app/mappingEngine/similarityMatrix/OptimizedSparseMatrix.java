package am.app.mappingEngine.similarityMatrix;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import am.app.mappingEngine.AbstractMatcher.alignType;
import am.app.mappingEngine.AbstractSimilarityMatrix;
import am.app.mappingEngine.Mapping;
import am.app.ontology.Node;
import am.app.ontology.Ontology;


public class OptimizedSparseMatrix extends AbstractSimilarityMatrix
{
	private static final long serialVersionUID = -4828904520708897468L;
	
	private RowCell<Mapping> head;// LinkedList<LinkedList<RowCell>> rows;
	private RowCell<Mapping> lastGetRow=null;//this is the last row that was used in the get function
	private RowCell<Mapping> lastSetRow=null;//this is the last row that was used in the set function
	private RowCell<Mapping> lastGetCol=null;
	private RowCell<Mapping> lastSetCol=null;
	/** The number of rows in this matrix. */
	private final int numRows; 
	
	/** The number of columns in this matrix. */
	private final int numCols;
	
	/**
	 * Main constructor of the SparseMatrix.
	 * @param numRows The number of rows in the matrix.
	 * @param numCols The number of columns in the matrix.
	 */

	public OptimizedSparseMatrix(Ontology sourceOntology, Ontology targetOntology, alignType typeOfMatrix)
	{
		super(sourceOntology, targetOntology, typeOfMatrix);
		
		// save the dimensions
		if( typeOfMatrix == alignType.aligningClasses ) { 
    		numRows = sourceOntology.getClassesList().size();
    		numCols = targetOntology.getClassesList().size();
    	} else if ( typeOfMatrix == alignType.aligningProperties ) {
    		numRows = sourceOntology.getPropertiesList().size();
    		numCols = targetOntology.getPropertiesList().size();
    	} else {
    		System.err.println("Invalid typeOfMatrix: " + typeOfMatrix + ".  Assuming aligningClasses.");
    		numRows = sourceOntology.getClassesList().size();
    		numCols = targetOntology.getClassesList().size();
    	}
		
		//create the head of the LL
		head = new RowCell<Mapping>(-1,-1,null);
	}
	
	/** A method to clone the similarity matrix. */
	public OptimizedSparseMatrix(OptimizedSparseMatrix optimizedSparseMatrix){
		this(optimizedSparseMatrix.getSourceOntology(), optimizedSparseMatrix.getTargetOntology(), optimizedSparseMatrix.getAlignType());
		//relation = optimizedSparseMatrix.getRelation();
    	
    	RowCell<Mapping> currentCol, currentRow;//these RowCells are the originals
    	
    	currentRow=optimizedSparseMatrix.getHead().nextr;//get the first row
    	
    	while(currentRow!=null){
    		//clone stuff here
    		this.set(currentRow.row, currentRow.col, currentRow.object);
    		currentCol=currentRow.nextc;
    		
    		while(currentCol!=null){
    			//clone stuff here
    			this.set(currentCol.row, currentCol.col, currentCol.object);
    			currentCol=currentCol.nextc;
    		}
    		currentRow=currentRow.nextr;
    	}
	}
	
	/*public SparseMatrix(Ontology s, Ontology t, alignType type) {
		this();
    	relation = MappingRelation.EQUIVALENCE;
    	typeOfMatrix = type;
    	
    	sourceOntologyID=s.getID();
    	targetOntologyID=t.getID();    	
	}*/
	
	/** A constructor used in AbstractMatcher. */
/*	public OptimizedSparseMatrix(int numRows, int numCols, alignType type, MappingRelation rel){
		this(numRows, numCols);
    	typeOfMatrix = type;
	}*/
	
	@Override
	public void set(int row, int column, Mapping obj)
	{
		//System.out.println(rows.size());
		if(head.nextr == null)//check if the head has any rows in it
		{
			RowCell<Mapping> newRowCell = new RowCell<Mapping>(row,column,obj);
			head.nextr = newRowCell; //if there are no rows then insert a new RowCell object
			newRowCell.prevr=head;//link the head to the new rowCell
		}
		else//if there are rows in the matrix then we need to check the row value and insert the new RowCell object
		{
			RowCell<Mapping> currentCell;
			if(lastSetRow==null)
				currentCell = head.nextr;//create an object that will be used to iterate through the LL starting with head
			else{
				if(lastSetCol!=null && row==lastSetCol.row)
					currentCell=lastSetCol;
				else
					currentCell=lastSetRow;
			}
			
			RowCell<Mapping> newCell = new RowCell<Mapping>(row,column,obj);//create a new RowCell object
			boolean notDone=true;
			while(currentCell!=null && notDone)
			{
				if( row < currentCell.row) 
				{
					if(currentCell.prevr!=null)//if its null we are at the head, or something went really wrong
						currentCell=currentCell.prevr;
					else{
						newCell.nextr=currentCell.nextr;
						newCell.prevr=currentCell;
						
						currentCell.nextr.prevr=newCell;
						currentCell.nextr=newCell;
						
						lastSetRow=newCell;
						break;
					}
					/*
					newCell.nextr=currentCell;
					newCell.prevr=currentCell.prevr;
					if(newCell.prevr!=null)
						newCell.prevr.nextr=newCell; 
					currentCell.prevr=newCell;
					//currentCell.nextr=null;
					
					//notDone=false;
					//numRows++;
					break;
					*/
				}
				else if(row == currentCell.row)//if the rows are equal then we need to start looking at col to insert
				{
					lastSetRow=currentCell;
					//add to collums here
					while(currentCell != null)
					{
						//System.out.println(1);
						if(column < currentCell.col)//same as i<currentCell.row
						{
							newCell.nextc=currentCell;
							newCell.prevc=currentCell.prevc;
							if(newCell.prevc!=null) {
								newCell.prevc.nextc=newCell;
							}else {
								// we are replacing a row header
								newCell.prevr = currentCell.prevr;
								newCell.nextr = currentCell.nextr;
								
								currentCell.prevr = null;
								currentCell.nextr = null;
								
								if( newCell.prevr != null ) newCell.prevr.nextr = newCell;
								if( newCell.nextr != null ) newCell.nextr.prevr = newCell;
							}
							currentCell.prevc=newCell;
							//currentCell.nextc=null;
							
							
							lastSetCol=newCell;
							notDone=false;
							break;
						}
						else if(column == currentCell.col){//implicit removal
							if(obj==null){//gotta remove this RowCell
								if(currentCell.nextr==null && currentCell.prevr==null){//im in the middle of the row
									if(currentCell.nextc==null){//end of the row
										currentCell.prevc.nextc=null;
										notDone=false;
										lastSetCol=newCell;
										break;
									}else{//time to start linking...
										currentCell.nextc.prevc=currentCell.prevc;
										currentCell.prevc.nextc=currentCell.nextc;
										notDone=false;
										lastSetCol=newCell;
										break;
									}
								}else{//we are a header more linking..
									if(currentCell.nextc==null){//nothing in this row
										if(currentCell.nextr==null){
											currentCell.prevr=null;
										}
										else{
											currentCell.nextr.prevr=currentCell.prevr;
											currentCell.prevr.nextr=currentCell.nextr;	
										}
										notDone=false;
										lastSetCol=newCell;
										break;
									}else{//need to link the row headers to a new row header
										currentCell.nextr.prevr=currentCell.nextc;
										currentCell.prevr.nextr=currentCell.nextc;
										
										currentCell.nextc.prevr=currentCell.prevr;
										currentCell.nextc.nextr=currentCell.nextr;
										currentCell.nextc.prevc=null;
										
										notDone=false;
										lastSetCol=newCell;
										break;
									}
								}
							}else{
								//replace whats in the current rowCell
								currentCell.object = obj;
								notDone=false;
								lastSetCol=newCell;
								break;
							}
						}
						else if( currentCell.nextc==null )//if the next col is null then we know that newCell is 
						{//the largest col so it gets stuck at the end
							//System.out.println(3);
							currentCell.nextc=newCell;
							newCell.prevc=currentCell;
							notDone=false;
							lastSetCol=newCell;
							break;
						}
						currentCell=currentCell.nextc;
					}
					
					//break;
				}
				else if(row>currentCell.row)//if i is the biggest number stick it on the end
				{
					if(currentCell.nextr==null){
						currentCell.nextr=newCell;
						newCell.prevr=currentCell;
						//notDone=false;
						//numRows++;
						lastSetRow=newCell;
						break;
					}
					else if(row<currentCell.nextr.row){
						newCell.nextr=currentCell.nextr;
						newCell.prevr=currentCell;
						
						currentCell.nextr.prevr=newCell;
						currentCell.nextr=newCell;
						
						lastSetRow=newCell;
					}
					else
						currentCell=currentCell.nextr;//grab the next row in case it needs to
				}
			}
		}
	}
	
	/**
	 * This method is a wrapper for {@link OptimizedSparseMatrix#set(int, int, Mapping)}.
	 */
	@Override
	public void setSimilarity(int i, int j, double similarity) {
		try {
	    	Node sourceNode = getSourceOntology().getNodefromIndex(i, typeOfMatrix);
	    	Node targetNode = getTargetOntology().getNodefromIndex(j, typeOfMatrix);
	    	Mapping m = new Mapping(sourceNode, targetNode, similarity);
	    	set(i, j, m);
    	}
    	catch (Exception e) {
    		// TODO: Use a logger here.
    		e.printStackTrace();
    	}
	}
	
	public Mapping get(int i, int j)
	{
		boolean done=false;
		RowCell<Mapping> currentCell;
		if(lastGetRow==null)
			currentCell=head.nextr;
		else{
			if(lastGetCol!=null && i==lastGetCol.row)
				currentCell=lastGetCol;
			else
				currentCell=lastGetRow;
		}
		
		
		while(!done && currentCell!=null){
			if(currentCell.row<i){
				currentCell=currentCell.nextr;
			}
			else if(currentCell.row>i){
				currentCell=currentCell.prevr;
			}
			else if(currentCell.row==i){
				lastGetRow=currentCell.clone();
				while(!done)
				{
					if(currentCell.col==j){
						lastGetCol=currentCell;
						return currentCell.object;
					}
					else if(currentCell.nextc!=null)
						currentCell=currentCell.nextc;
					else
						done=true;
				}
			}
		}
		return null;
	}
	
	/** TODO: Does protected work? */
	protected RowCell<Mapping> getHead() { return head; }
	
	@Override
	public String toString() {
		String print = new String();
		
		RowCell<Mapping> currentRow = head.nextr;
		while( currentRow != null ) {
			print += currentRow.row + ". ";
			RowCell<Mapping> currentCol = currentRow;
			while(currentCol != null) {
				print += currentCol.col + ", ";
				currentCol = currentCol.nextc;
			}
			print += ".\n";
			currentRow = currentRow.nextr;
		}
		
		return print;

	}
	
	@Override
	public List<Mapping> chooseBestN() {
		System.err.println("Not implemented");
		return null;
	}
	@Override
	public List<Mapping> chooseBestN(List<Integer> rowsIncludedList,
			List<Integer> colsIncludedList, boolean considerThreshold,
			double threshold) {
		System.err.println("Not implemented");
		return null;
	}
	@Override
	public List<Mapping> chooseBestN(boolean considerThreshold, double threshold) {
		System.err.println("Not implemented");
		return null;
	}
	@Override
	public List<Mapping> chooseBestN(List<Integer> rowsIncludedList,
			List<Integer> colsIncludedList) {
		System.err.println("Not implemented");
		return null;
	}
	
	@Override
	public SimilarityMatrix clone() {
		return new OptimizedSparseMatrix(this);
	}
	
	@Override
	public void fillMatrix(double d, List<Node> sourceList,
			List<Node> targetList) {
		// TODO not needed yet
		System.err.println("Not implemented yet");
	}
	
	@Override
	public alignType getAlignType() { return typeOfMatrix;}
	
	@Override
	public Mapping[] getColMaxValues(int col, int numMaxValues) {
		//i do not check for maxAlignements < numMaxValues
		Mapping[] maxAlignments= new Mapping[numMaxValues];
		
		for(int h = 0; h<maxAlignments.length;h++) {
			maxAlignments[h] = new Mapping(-1); //intial max alignments have sim equals to -1, don't put 0 could create problem in the next for
		}
		
		Mapping currentValue;
		Mapping currentMax;
		
		RowCell<Mapping> currentRow=head.nextr;
		
		while(currentRow!=null){
			RowCell<Mapping> currentCol=currentRow;
			while(currentCol!=null){
				if(currentCol.col==col){
					currentValue= currentCol.object;
					for(int k = 0;k<maxAlignments.length; k++) {
						currentMax = maxAlignments[k];
						if(currentValue.getSimilarity() >= currentMax.getSimilarity()) { //if so switch the new value with the one in array and then i have to continue scanning the array to put in the switched value
							maxAlignments[k] = currentValue;
							currentValue = currentMax;
						}
					}
					break;
				}
				currentCol=currentCol.nextc;
			}
			currentRow=currentRow.nextr;
		}
		
		return maxAlignments;
	}
	
	@Override
	public double getColSum(int col) {
		// returns a sum of similarities for a col
		double sum=0;
		RowCell<Mapping> currentRow;//these RowCells are the originals
    	
    	currentRow=head.nextr;//get the first row
    	
    	while(currentRow!=null){    		
    		Mapping temp=get(currentRow.row,col);
    		if(temp!=null)
    			sum+=temp.getSimilarity();
    		
    		currentRow=currentRow.nextr;
    	}
		return sum;
	}
	
	@Override public int getColumns() { return numCols; }
	@Override public int getRows() { return numRows; }
	
	@Override
	public double[][] getCopiedSimilarityMatrix() {
		
		double[][] copiedMatrix = new double[numRows][numCols];
		
		for( int i = 0; i < numRows; i++ ) {
			for( int j = 0; j < numCols; j++ ) {
				copiedMatrix[i][j] = getSimilarity(i, j);
			}
		}
		
		return copiedMatrix;
	}
	
	@Override
	public double getMaxValue() {
		// returns the max similarity of the matrix
		double max=0.0;
		RowCell<Mapping> currentCol, currentRow;
    	currentRow=head.nextr;//get the first row
    	
    	while(currentRow!=null){
    		if(currentRow.object.getSimilarity()>max)
    			max=currentRow.object.getSimilarity();
 
    		currentCol=currentRow.nextc;
    		while(currentCol!=null){
    			if(currentCol.object.getSimilarity()>max)
        			max=currentCol.object.getSimilarity();
    			currentCol=currentCol.nextc;
    		}
    		currentRow=currentRow.nextr;
    	}
		return max;
	}
	
	@Override
	public Mapping[] getRowMaxValues(int row, int numMaxValues) {
		//i do not check for maxAlignements < numMaxValues
		Mapping[] maxAlignments= new Mapping[numMaxValues];
		
		for(int h = 0; h<maxAlignments.length;h++) {
			maxAlignments[h] = new Mapping(-1); //intial max alignments have sim equals to -1, don't put 0 could create problem in the next for
		}
		
		Mapping currentValue;
		Mapping currentMax;
		
		RowCell<Mapping> currentRow=head.nextr;
		
		while(currentRow!=null){
			if(currentRow.row==row){
				RowCell<Mapping> currentCol=currentRow;
				while(currentCol!=null){
					currentValue= currentCol.object;
					for(int k = 0;k<maxAlignments.length; k++) {
						currentMax = maxAlignments[k];
						if(currentValue.getSimilarity() >= currentMax.getSimilarity()) { //if so switch the new value with the one in array and then i have to continue scanning the array to put in the switched value
							maxAlignments[k] = currentValue;
							currentValue = currentMax;
						}
					}
					currentCol=currentCol.nextc;
				}
				break;
			}
			currentRow=currentRow.nextr;
		}
		
		return maxAlignments;
	}
	
	@Override
	public double getRowSum(int row) {
		// gets the sum of the similarities for a row
		double sum=0.0;
		
		RowCell<Mapping> currentCol, currentRow;
		currentRow=head.nextr;//get the first row
		while(currentRow!=null){
			if(currentRow.row==row){
				sum+=currentRow.object.getSimilarity();
	    		currentCol=currentRow.nextc;
	    		while(currentCol!=null){
	    			sum+=currentCol.object.getSimilarity();
	    			currentCol=currentCol.nextc;
	    		}
			}
    		currentRow=currentRow.nextr;
    	}
		return sum;
	}
	
	@Override
	public double getSimilarity(int i, int j) {
		// returns the similarity of the specified row
		Mapping temp=get(i,j);
		if(temp==null)
			return 0.00d;
		return temp.getSimilarity();
	}
	
	@Override
	public Mapping[] getTopK(int k) {
		// FIXME implemnt
		System.err.println("Not implemented yet topK");
		return null;
	}
	
	@Override
	public Mapping[] getTopK(int k, boolean[][] filteredCells) {
		// FIXME implement
		System.err.println("Not implemented yet topK2");
		return null;
	}
	
	@Override
	public void initFromNodeList(List<Node> sourceList, List<Node> targetList) {
		// FIXME remove
		System.err.println("initFromNodeList: to be deleted");
		
	}
	
	/*@Override
	public void setSimilarity(int i, int j, double d) {
		// sets the similarity of a specified entry in the matrix
		Mapping temp=get(i,j);
		if(temp!=null)
			temp.setSimilarity(d);
		
	}*/
	
	@Override
	public List<Mapping> toMappingArray() {
		ArrayList<Mapping> mappings=new ArrayList<Mapping>();
		RowCell<Mapping> currentCol, currentRow;
		currentRow=head.nextr;//get the first row
		while(currentRow!=null){
			mappings.add(currentRow.object);
    		currentCol=currentRow.nextc;
    		while(currentCol!=null){
    			mappings.add(currentCol.object);
    			currentCol=currentCol.nextc;
    		}
    		currentRow=currentRow.nextr;
    	}
		return mappings;
	}
	
	@Override
	public List<Mapping> toMappingArray(FileWriter fw, int round) {
		ArrayList<Mapping> mappingArray = new ArrayList<Mapping>();
		RowCell<Mapping> currentCol, currentRow;
		currentRow=head.nextr;//get the first row
		while(currentRow!=null){
			mappingArray.add(currentRow.object);
			if(round == 1)
			try {
				fw.append(currentRow.object.toString() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
    		currentCol=currentRow.nextc;
    		while(currentCol!=null){
    			mappingArray.add(currentCol.object);
    			if(round == 1)
    			try {
    				fw.append(currentCol.object.toString() + "\n");
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			currentCol=currentCol.nextc;
    		}
    		currentRow=currentRow.nextr;
    	}
		
		
		for(int i = 0; i < getRows(); i++){
			for(int j = 0; j < getColumns(); j++){
				if(this.get(i, j) != null){
					
				}
			}
		}
		return mappingArray;
	}
	
	@Override
	public List<Double> toSimilarityArray(List<Mapping> mapsArray) {
		ArrayList<Double> similarities=new ArrayList<Double>();
		for(int i=0;i<mapsArray.size();i++)
			similarities.add(mapsArray.get(i).getSimilarity());
		return similarities;
	}

	/** This class is used in the implementation of the SparseMatrix. */
	protected class RowCell <E>
	{
		public int row, col;
		public E object;
		public RowCell<E> nextr=null, nextc=null, prevr=null, prevc=null;
		public RowCell(int r, int c, E o)
		{
			row=r;
			col=c;
			object=o;
		}
		public RowCell<E> clone(){
			RowCell<E> clone=new RowCell<E>(row,col,object);
			clone.nextc=nextc;
			clone.nextr=nextr;
			clone.prevc=prevc;
			clone.prevr=prevr;
			return clone;
		}
	}	
}
