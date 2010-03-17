package com.inexas.pl.entity.render;

import java.util.*;
import com.inexas.pl.entity.*;

/**
 * This class is used to store a number of control parameters 
 * for displaying tuple lists. For example we store the index of 
 * the first tuple to display. The class converts the parameters
 * to and from a single string and stores the values in a
 * hidden input. If anything fails with the parsing then the
 * default values are set for all parameters.
 * 
 * @author Keith Whittingham
 * @version $Revision: 1.1 $
 */
public class TupleListView {
	private final static int DEFAULT_RANGE = 5;
	private static Map<String,TupleListView> views = new HashMap<String,TupleListView>();
	private final String key;
	private int tupleListSize;
	private int firstRow, lastRowPlusOne;
	private int range;
	private boolean vertical;
	
	public static TupleListView getTupleListView(final TupleList tupleList) {
		// todo this should be written to a database and associated with a user from the session
		final String key = tupleList.getFullPath();
		TupleListView returnValue = views.get(key);
		if(returnValue == null) {
			returnValue = new TupleListView(tupleList);
			views.put(key, returnValue);
		}
		return returnValue;
	}
	
	private TupleListView(TupleList tupleList) {
		key = tupleList.getFullPath();
		tupleListSize = tupleList.size();
		range = DEFAULT_RANGE;
		lastRowPlusOne = tupleListSize < range ? tupleListSize : range;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getRange() {
		return range;
	}

	public int getLastRowPlusOne() {
		return lastRowPlusOne;
	}
	
	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}

	public boolean isVertical() {
		return vertical;
	}

	public void setTupleListSize(int tupleListSize) {
		this.tupleListSize = tupleListSize;
	}

	public int getTupleListSize() {
		return tupleListSize;
	}

	public void commit() {
		// keep range within limits
		if(range < 5 || range > 1000) {
			range = DEFAULT_RANGE;
		}
		
		// check first row...
		if(firstRow < 0) {
			this.firstRow = 0;
		} else if(firstRow >= tupleListSize) {
			firstRow = tupleListSize - range;
			if(firstRow < 0) {
				firstRow = 0;
			}
		}
		
		lastRowPlusOne = firstRow + range;
		if(lastRowPlusOne > tupleListSize) {
			lastRowPlusOne = tupleListSize;
			firstRow = lastRowPlusOne - range;
			if(firstRow < 0) {
				firstRow = 0;
			}
		}
		
		views.put(key, this);
	}

	public void movePageUp() {
		firstRow -= range;
	}

	public void movePageDown() {
		firstRow += range;
	}

	public void moveEnd() {
		firstRow = tupleListSize;
	}

}
