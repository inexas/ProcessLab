package com.inexas.pl.datacache;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import com.ibm.icu.util.GregorianCalendar;
import com.google.visualization.datasource.datatable.*;
import com.google.visualization.datasource.datatable.value.*;

public class SerializableTableCell implements Serializable {
	private static final long serialVersionUID = -7310633324724890643L;
	private final static int TYPE_BOOLEAN 	= 0;
	private final static int TYPE_DATE 		= 1;
	private final static int TYPE_DATETIME 	= 2;
	private final static int TYPE_NUMBER 	= 3;
	private final static int TYPE_TEXT 		= 4;
	private final static int TYPE_TIMEOFDAY = 5;
	private final int type;
	private final Serializable value;
	public final Map<String, String> customProperties;
	
    public SerializableTableCell(TableCell cell) {
    	final Value v = cell.getValue();
    	switch(v.getType()) {
    	case BOOLEAN:
    		type = TYPE_BOOLEAN;
    		if(v.isNull()) {
    			value = null;
    		} else {
    			value = ((BooleanValue)v).getValue() ? Boolean.TRUE : Boolean.FALSE;
    		}
    		break;
    	case DATE:
    		type = TYPE_DATE;
    		if(v.isNull()) {
    			value = null;
    		} else {
    			value = ((DateValue)v).getObjectToFormat();
    		}
    		break;
    	case DATETIME:
    		type = TYPE_DATETIME;
    		if(v.isNull()) {
    			value = null;
    		} else {
    			value = ((DateTimeValue)v).getObjectToFormat();
    		}
    		break;
    	case NUMBER:
    		type = TYPE_NUMBER;
    		if(v.isNull()) {
    			value = null;
    		} else {
    			value = new Double(((NumberValue)v).getValue());
    		}
    		break;
    	case TEXT:
    		type = TYPE_TEXT;
    		if(v.isNull()) {
    			value = null;
    		} else {
    			value = ((TextValue)v).getValue();
    		}
    		break;
    	case TIMEOFDAY:
    		type = TYPE_TIMEOFDAY;
    		if(v.isNull()) {
    			value = null;
    		} else {
    			value = ((TimeOfDayValue)v).getObjectToFormat();
    		}
    		break;
    	default:
    		throw new RuntimeException("Should never happen: " + v.getType());
    	}
    	customProperties = cell.getCustomProperties();
    }

	public TableCell getTableCell() {
		final TableCell result;
		final Value v;
		if(value == null) {
			switch(type) {
			case TYPE_BOOLEAN:
				v = BooleanValue.getNullValue();
				break;
			case TYPE_DATE:
				v = DateValue.getNullValue();
				break;
			case TYPE_DATETIME:
				v = DateTimeValue.getNullValue();
				break;
			case TYPE_NUMBER:
				v = NumberValue.getNullValue();
				break;
			case TYPE_TEXT:
				v = TextValue.getNullValue();
				break;
			case TYPE_TIMEOFDAY:
				v = TimeOfDayValue.getNullValue();
				break;
			default:
				throw new RuntimeException("Should never happen: " + type);
			}
		} else {
			switch(type) {
			case TYPE_BOOLEAN:
				v = ((Boolean)value).booleanValue() ? BooleanValue.TRUE : BooleanValue.FALSE; 
				break;
			case TYPE_DATE:
				v = new DateValue((GregorianCalendar)value);
				break;
			case TYPE_DATETIME:
				v = new DateTimeValue((GregorianCalendar)value);
				break;
			case TYPE_NUMBER:
				v = new NumberValue(((Double)value).doubleValue());
				break;
			case TYPE_TEXT:
				v = new TextValue((String)value);
				break;
			case TYPE_TIMEOFDAY:
				v = new TimeOfDayValue((GregorianCalendar)value);
				break;
			default:
				throw new RuntimeException("Should never happen: " + type);
			}
		}
		result = new TableCell(v);
		
		if(customProperties != null) {
			for(final Entry<String, String> entry : customProperties.entrySet())  {
				result.setCustomProperty(entry.getKey(), entry.getValue());
			}
		}

		return result;
    }

}
