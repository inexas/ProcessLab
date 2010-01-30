package com.inexas.wikibi.scheduler;

import java.util.*;
import com.ecyrd.jspwiki.*;


public interface HasJobs {

	Job getJobs(WikiContext context, Map<String, String> map);

}
