package com.inexas.pl.loader;

import java.util.*;
import com.inexas.pl.entity.*;
import com.inexas.util.*;

public interface IKtcvTypeLoader extends ILoader {
	
	String getValue();

	DataType getDataType();
	
	Collection<NameValue<String>> getConstraints();

}