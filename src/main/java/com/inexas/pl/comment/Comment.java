package com.inexas.pl.comment;

import java.util.*;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.plugin.*;

/**
 * This is just a dumb plugin that lets you hide content on a page. There
 * is nothing in the wiki markup to let you comment.
 */
public class Comment implements WikiPlugin {

	@SuppressWarnings("unchecked")
    public String execute(WikiContext context, Map params) throws PluginException {
		return "";
    }

}
