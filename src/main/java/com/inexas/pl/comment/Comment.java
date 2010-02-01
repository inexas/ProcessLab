/*
	Copyright 2010 Inexas. All rights reserved.

	Licensed under the Inexas Software License V1.0. You may not use this file 
	except in compliance with the License. You may obtain a copy of the License
	at http://www.inexas.com/ISL-V1.0.

	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
	CONDITIONS OF ANY KIND, either express or implied. See the License for the 
	specific language governing permissions and limitations under the License.
*/
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
