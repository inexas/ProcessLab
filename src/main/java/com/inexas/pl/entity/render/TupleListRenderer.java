package com.inexas.pl.entity.render;

import com.inexas.pl.entity.*;

/**
 * Render a tuple list. This renderer renders both the tuple list
 * and tuple objects
 * 
 * @author Keith Whittingham
 * @version $Revision: 1.1 $
 */
public class TupleListRenderer extends AbstractRenderer {
//	private final String ranges[] = { "5", "10", "20", "50", "100", "1000" }; 
//	private final TupleList tupleList;
//	private final TupleType tupleType;
//	private final boolean vertical;		// show members in vertical list
//	private final boolean showHeader, showFooter, showRowNumbers, showControls;
//	private final int size;				// the number of rows in the tuple
//	private final int minimumSize;		// cardinality minimum
//	private final TupleListView view;
	// todo private final Renderer renderer;
//	private final String id;

	public TupleListRenderer(@SuppressWarnings("unused") TupleList tupleList) {
//		this.tupleList = tupleList;
//		this.tupleType = tupleList.getType();
//		final TupleListStyle style = null; // todo (TupleListStyle)tupleType.getStyle();
//		if(style == null) {
//			vertical = false;
//			showHeader = true;
//			showFooter = true;
//			showRowNumbers = true;
//			showControls = true;
//		} else {
//			vertical = style.isVertical();
//			showHeader = style.showHeader();
//			showFooter = style.showFooter();
//			showRowNumbers = style.showRowNumbers();
//			showControls = style.showControls();
//		}
//		
//		size = tupleList.size();
//		minimumSize = tupleType.getCardinality().getFrom();
//		view = TupleListView.getTupleListView(tupleList);
//		renderer = response.getRenderer();
//		id = response.getNamespace().newNamespace(tupleList.getFullPath());
//		sb = response.getStringBuilder();
	}
	
	@Override
	public void render() {
		// todo Implement me
        throw new RuntimeException("How about implementing me?!");
	}
	
	/**
	 * Render a child tuple
	 */
//	@Override
//	public void renderField(@SuppressWarnings("unused")String fieldKey) {
//		sb.append("<table border=1>");
//		if(vertical) {
//			renderVertically();
//		} else {
//			renderHorizontally();
//		}
//		sb.append("</table>");
//	}

	/**
	 * Render so that the tuple is displayed on one horizontal row
	 */
//	void renderHorizontally() {
//		// Optional first row...
//		if(showHeader) {
//			sb.append("<tr>");
//			
//			// optional left column...
//			if(showRowNumbers) {
//				sb.append("<th colspan=2></th>");
//			}
//			
//			// member headers...
//			for(final TupleTypeMember member : tupleType) {
//				sb.append("<th>");
//				renderLabel(member);
//				sb.append("</th>");
//			}
//			
//			sb.append("</tr>");
//		}
//		
//		// middle rows....
//		for(int rowNumber = view.getFirstRow(); rowNumber < view.getLastRowPlusOne(); rowNumber++) {
//			final Tuple tuple = tupleList.get(rowNumber);
//			final String tupleName = tuple.getFullPath();
//			final String tupleKey = response.getNamespace().newNamespace(tupleName);
//			
//			sb.append("<tr>");
//			
//			if(showRowNumbers) {
//				sb.append("<th>");
//				sb.append(rowNumber + 1);
//				sb.append("</th>");
//			}
//			sb.append("<th>");
//			renderTupleControls(rowNumber, tupleKey);
//			sb.append("</th>");
//			
//			for(final TupleMember member : tuple) {
//				sb.append("<td>");
//				final AbstractRenderer memberRenderer = renderer.getRenderer(member);
//				memberRenderer.renderField(tupleKey);
//				sb.append("</td>");
//			}
//			
//			sb.append("</tr>");
//		}
//		
//		// bottom row: controls...
//		if(showFooter) {
//			sb.append("<tr>");
//			
//			// controls cell
//			sb.append("<th align=center colspan=");
//			sb.append(tupleType.getMembers().size() + (showRowNumbers ? 2 : 0));
//			sb.append('>');
//			renderNavigator();
//			sb.append("</th></tr>");
//		}
//	}
//	
//	void renderVertically() {
//		// top row...
//		if(showRowNumbers) {
//			sb.append("<tr>");
//			if(showHeader) {
//				sb.append("<th></th>");
//			}
//			
//			for(int i = view.getFirstRow(); i < view.getLastRowPlusOne(); i++) {
//				sb.append("<th>");
//				sb.append(i + 1);
//				sb.append("</th>");
//			}
//			
//			if(showFooter) {
//				sb.append("<th></th></tr>");
//			}
//		}
//
//		// middle rows...
//		final int memberCount = tupleType.getMembers().size();
//		for(int memberIndex = 0; memberIndex < memberCount; memberIndex++) {
//			final TupleTypeMember memberType = tupleType.getMembers().get(memberIndex);
//			sb.append("<tr>");
//			
//			// Left column...
//			if(showHeader) {
//				sb.append("<th>");
//				renderLabel(memberType);
//				sb.append("</th>");
//			}
//			
//			// middle columns...
//			for(int i = view.getFirstRow(); i < view.getLastRowPlusOne(); i++) {
//				final Tuple tuple = tupleList.get(i);
//				final String tupleName = tuple.getFullPath();
//				final String tupleKey = response.getNamespace().newNamespace(tupleName);
//				final TupleMember member = tuple.getMember(memberIndex);
//				final AbstractRenderer memberRenderer = renderer.getRenderer(member);
//				sb.append("<td>");
//				memberRenderer.renderField(tupleKey);
//				sb.append("</td>");
//			}
//			
//			// right column...
//			if(showFooter) {
//				final AbstractRenderer memberRenderer =
//					renderer.getRenderer(memberType.newInstance());
//				sb.append("<th>");
//				memberRenderer.renderField("what?");
//				sb.append("</th>");
//			}
//			
//			sb.append("</tr>");
//		}
//
//		// bottom row...
//		if(showControls) {
//			sb.append("<tr>");
//			
//			// first column...
//			if(showHeader) {
//				sb.append("<th>");
//				renderNavigator();
//				sb.append("</th>");
//			}
//			
//			// middle columns...
//			for(int rowIndex = view.getFirstRow(); rowIndex <= view.getLastRowPlusOne(); rowIndex++) {
//				sb.append("<th>");
//				renderTupleControls(rowIndex, id);
//				sb.append("</th>");
//			}
//			
//			// last column...
//			if(showFooter) {
//				sb.append("<th>");
//				final Action append = Cms.Control.TUPLE_APPEND.getAction();
//				try {
//					append.getParameter(Cms.TUPLE_KEY).setValue(id);
//				} catch(ConstraintViolationException e) {
//					// todo Auto-generated catch block
//					e.printStackTrace();
//					throw new EiapRuntimeException("", e);
//				}
//				renderer.renderButton("cms", append, size < tupleType.getCardinality().getTo());
//				sb.append("</th>");
//				// todo! Implement me! 
//				throw new EiapRuntimeException("Implement me! Set 'uniqueId in a hidden field");
//			}
//			
//			sb.append("</tr>");
//		}
//	}
//	
//	void renderLabel(TupleTypeMember member) {
//		if(member instanceof TupleType || size < 2) {
//			sb.append(member.getName());
//		} else {
//			sb.append("<a href=\"sort\">");
//			sb.append(member.getName());
//			sb.append("</a>");
//		}
//	}
//
//	void renderNavigator() {
//		// top  prv    scope   app nxt btm    rng
//		// "<<" "<" "1..20/25" "+" ">" ">>" "[20|V]"
//		boolean enabled = view.getFirstRow() != 0;
//		final Action top = Cms.Control.TUPLE_TOP.getAction();
//		top.getParameter(Cms.TUPLE_KEY).setValue(id);
//		renderer.renderButton("cms", top, enabled);
//		
//		final Action previous = Cms.Control.TUPLE_PREVIOUS.getAction();
//		previous.getParameter(Cms.TUPLE_KEY).setValue(id);
//		renderer.renderButton("cms", previous, enabled);
//		
//		// span
//		if(size == 0) {
//			sb.append("0..0/0");
//		} else {
//			sb.append(view.getFirstRow() + 1);
//			sb.append("..");
//			sb.append(view.getLastRowPlusOne());
//			sb.append('/');
//			sb.append(size);
//		}
//		
//		final Action append = Cms.Control.TUPLE_APPEND.getAction();
//		append.getParameter(Cms.TUPLE_KEY).setValue(id);
//		
//		renderer.renderButton("cms", append, size < tupleType.getCardinality().getTo());
//		
//		enabled = view.getLastRowPlusOne() < size;
//		final Action next = Cms.Control.TUPLE_NEXT.getAction();
//		next.getParameter(Cms.TUPLE_KEY).setValue(id);
//		renderer.renderButton("cms", next, enabled);
//		
//		final Action bottom = Cms.Control.TUPLE_BOTTOM.getAction();
//		bottom.getParameter(Cms.TUPLE_KEY).setValue(id);
//		renderer.renderButton("cms", bottom, enabled);
//		
//		final Action range = Cms.Control.TUPLE_RANGE.getAction();
//		range.getParameter(Cms.TUPLE_KEY).setValue(id);
//		renderer.renderOption(
//				id,
//				"cms",
//				range,
//				ranges,
//				Integer.toString(view.getRange()));
//		
//	}
//
//	void renderTupleControls(int rowNumber, String tupleListName) {
//		final Action up = Cms.Control.TUPLE_UP.getAction();
//		up.getParameter(Cms.TUPLE_KEY).setValue(tupleListName);
//		renderer.renderButton("cms", up, rowNumber > 0);
//		
//		final Action down = Cms.Control.TUPLE_DOWN.getAction();
//		down.getParameter(Cms.TUPLE_KEY).setValue(tupleListName);
//		renderer.renderButton("cms", down, rowNumber < (size-1));
//		
//		final Action delete = Cms.Control.TUPLE_DELETE.getAction();
//		delete.getParameter(Cms.TUPLE_KEY).setValue(tupleListName);
//		renderer.renderButton("cms", delete, size > minimumSize);
//	}

	@Override
	public void renderLabel() {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
    public void renderField(String id) {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

}
