package com.inexas.pl.pl;
import org.junit.*;

public class TestKtcv {

    @Test
	public void testBoolean0() throws Exception {
//		final String key = "testBoolean";
//		final KtcvType<Boolean> type = new KtcvType<Boolean>(DataType.BOOLEAN, key);
//		assertTrue(type.getKey().equals(key));
//		assertTrue(!type.getConstraintIterator().hasNext());
//		
//		final Ktcv<Boolean> ktcv = type.newInstance();
//		assertTrue(ktcv.getType() == type);
//		assertTrue(ktcv.getParentTuple() == null);
//		assertTrue(ktcv.getFullPath().equals("/" + key));
//		assertTrue(ktcv.getValue() == null);
//		assertTrue(ktcv.getValueAsString() == null);
//		
//		ktcv.setValue(Boolean.TRUE);
//		assertTrue(ktcv.getValue().booleanValue());
//		assertTrue(ktcv.getValueAsString().equals("true"));
//		ktcv.setValue(Boolean.FALSE);
//		assertTrue(!ktcv.getValue().booleanValue());
//		assertTrue(ktcv.getValueAsString().equals("false"));
//		ktcv.setValue(null);
//		assertTrue(ktcv.getValueAsString() == null);
//		assertTrue(ktcv.getValue() == null);
//		
//		ktcv.parseAndSetValue("true");
//		assertTrue(ktcv.getValue().booleanValue());
//		assertTrue(ktcv.getValueAsString().equals("true"));
//		ktcv.parseAndSetValue("false");
//		assertTrue(!ktcv.getValue().booleanValue());
//		assertTrue(ktcv.getValueAsString().equals("false"));
	}
	
	@SuppressWarnings({ })
    @Test
	public void testBoolean1() throws Exception {
//		final KtcvType<Boolean> type = new KtcvType<Boolean>(DataType.BOOLEAN, "test");
//		final Ktcv<Boolean> ktcv = type.newInstance();
//		ktcv.setValue(Boolean.FALSE);
//		final KtcvType<Boolean> gotType = (KtcvType<Boolean>) ktcv.getType();
//		assertTrue(gotType.getKey().equals("test"));
//		assertTrue(!gotType.getConstraintIterator().hasNext());
//		assertTrue(ktcv.getFullPath().equals("/test"));
//		assertTrue(ktcv.getValueAsString().equals("false"));
//		assertTrue(ktcv.getParentTuple() == null);
	}
	
	// ?todo Add tests for other types
	
	// todo add tests for tuples
}
