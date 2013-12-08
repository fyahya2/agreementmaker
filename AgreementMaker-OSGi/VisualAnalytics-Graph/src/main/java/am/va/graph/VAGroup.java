package am.va.graph;

import java.util.ArrayList;
import java.util.HashMap;

public class VAGroup {
	private static int nodeCount = 0;
	private int groupID;
	private int parent;
	private VAData rootNode;
	// private HashMap<String, VAData> mapVAData;
	private ArrayList<VAData> VADataArray;
	private HashMap<String, Integer> slotCountMap;
	private ArrayList<Integer> arcIntervalIndexArray;

	public VAGroup() {
		this.groupID = ++nodeCount;
		this.parent = -1;
		this.rootNode = null;
		// this.mapVAData = new HashMap<String, VAData>();
		this.VADataArray = new ArrayList<VAData>();
		this.slotCountMap = new HashMap<String, Integer>();
		this.arcIntervalIndexArray = new ArrayList<Integer>();
	}

	/**
	 * Set the parent pie chart
	 * 
	 * @param parent
	 */
	public void setParent(int parent) {
		this.parent = parent;
	}

	/**
	 * Set the root node of this group
	 * 
	 * @param rootNode
	 */
	public void setRootNode(VAData rootNode) {
		this.rootNode = rootNode;
	}

	/**
	 * Calculate the number of 10 slotCountMap
	 */
	private void setslotCountMap() {
		// for (VAData data : mapVAData.values()) {
		for (VAData data : VADataArray) {
			double sim = data.getSimilarity();
			for (int i = 0; i < VAVariables.slotNum; i++) {
				if (sim > VAVariables.threshold[i]
						&& sim <= VAVariables.threshold[i + 1]) {
					String key = VAVariables.thresholdName[i];
					if (!slotCountMap.containsKey(VAVariables.thresholdName[i])) {
						slotCountMap.put(key, 1);
					} else {
						slotCountMap.put(key, slotCountMap.get(key) + 1);
					}
					break;
				}
			}
		}
	}

	private void setArcIntervalIndex() {
		// Data with smallest similarity starts from 0
		for (int i = 0; i < VAVariables.totalArcNumOfPieChart; i++) {
			arcIntervalIndexArray.add(-1);
		}

		int VADataNum = VADataArray.size();
		arcIntervalIndexArray.add(VAVariables.totalArcNumOfPieChart); // add the
																		// last

		int lastPos = -1; // end index of last arc interval
		int intervalCount = 1; // start from the first interval
		// Iterate through all the sorted data, get the index for each arc
		// interval in dataArray
		for (int i = 0; i < VADataNum;) {
			double thresh = 1.0 / VAVariables.totalArcNumOfPieChart
					* intervalCount;
			int idx = (int) (VADataArray.get(i).getSimilarity() / VAVariables.arcInterval);
			// System.out.println("thresh " + thresh + " sim " +
			// VADataArray.get(i).getSimilarity() +
			// " idx " + idx + " lastPos " + lastPos + " intervalCount " +
			// intervalCount);
			// new slot, add previous index as the end of last slot
			if (VADataArray.get(i).getSimilarity() <= thresh) {
				arcIntervalIndexArray.set(intervalCount, i);
				lastPos = i;
				i++;
				// System.out.println("set IndexArray[" + intervalCount + "] = "
				// + i + " last Pos = " + lastPos + " intervalCount " +
				// intervalCount);
			} else {
				intervalCount++;
				arcIntervalIndexArray.set(intervalCount, lastPos);
				// System.out.println("set IndexArray[" + intervalCount + "] = "
				// + lastPos);
			}
		}
		while (intervalCount <= VAVariables.totalArcNumOfPieChart) {
			arcIntervalIndexArray.set(intervalCount++, lastPos);
		}

		/*
		 * 
		 * System.out.println( VADataNum + " data"); for (int i = 0; i <
		 * VADataNum; i++) { System.out.println(
		 * VADataArray.get(i).getSimilarity() + " "); } System.out.println(
		 * " data, interval index array " + arcIntervalIndexArray.size()); for
		 * (int i = 0; i <= VAVariables.totalArcNumOfPieChart; i++) {
		 * System.out.print(arcIntervalIndexArray.get(i)+ " "); }
		 * System.out.print("\n");
		 */
	}

	// public void setMapVAData(HashMap<String, VAData> mapVAData) {
	// this.mapVAData = mapVAData;
	// setslotCountMap();
	// }

	/**
	 * Set the children of this node
	 * 
	 * @param listVAData
	 */
	public void setListVAData(ArrayList<VAData> listVAData) {
		this.VADataArray = listVAData;
		setslotCountMap();
		setArcIntervalIndex();
	}

	public static int getNodeCount() {
		return nodeCount;
	}

	public int getGroupID() {
		return groupID;
	}

	public VAData getRootNode() {
		return rootNode;
	}

	public int getParent() {
		return parent;
	}

	// public HashMap<String, VAData> getMapVAData() {
	// return mapVAData;
	// }

	public ArrayList<VAData> getVADataArray() {
		return VADataArray;
	}

	public HashMap<String, Integer> getslotCountMap() {
		return slotCountMap;
	}

	public ArrayList<Integer> getArcIntervalIndexArray() {
		return arcIntervalIndexArray;
	}

}
