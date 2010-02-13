/*
  Copyright (c) Inexas 2010

  Modifications licensed under the Inexas Software License V1.0. You
  may not use this file except in compliance with the License.

  The License is available at: http://www.inexas.com/ISL-V1.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  The original file and contents are licensed under a separate license:
  see below.
*/
/*
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package com.ecyrd.jspwiki.xmlrpc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.*;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;

/**
 * Handles all incoming servlet requests for XML-RPC calls.
 * <P>
 * Uses two initialization parameters:
 * <UL>
 * <LI><B>handler</B> : the class which is used to handle the RPC calls.
 * <LI><B>prefix</B> : The command prefix for that particular handler.
 * </UL>
 * 
 * @since 1.6.6
 */
@SuppressWarnings("unchecked")
public class RPCServlet extends HttpServlet {
	private static final long serialVersionUID = 3976735878410416180L;

	/**
	 * This is what is appended to each command, if the handler has not been
	 * specified.
	 */
	// FIXME: Should this be $default?
	public static final String XMLRPC_PREFIX = "wiki";

	private WikiEngine m_engine;
	private XmlRpcServer m_xmlrpcServer = new XmlRpcServer();

	static Logger log = Logger.getLogger(RPCServlet.class);

	public void initHandler(String prefix, String handlerName)
	        throws ClassNotFoundException,
	        InstantiationException,
	        IllegalAccessException {
		/*
		 * Class handlerClass = Class.forName( handlerName ); WikiRPCHandler
		 * rpchandler = (WikiRPCHandler) handlerClass.newInstance();
		 * rpchandler.initialize( m_engine ); m_xmlrpcServer.addHandler( prefix,
		 * rpchandler );
		 */
		Class handlerClass = Class.forName(handlerName);
		m_xmlrpcServer.addHandler(prefix, new LocalHandler(handlerClass));
	}

	/**
	 * Initializes the servlet.
	 */
	@Override
    public void init(ServletConfig config)
	        throws ServletException {
		m_engine = WikiEngine.getInstance(config);

		String handlerName = config.getInitParameter("handler");
		String prefix = config.getInitParameter("prefix");

		if(handlerName == null)
			handlerName = "com.ecyrd.jspwiki.xmlrpc.RPCHandler";
		if(prefix == null)
			prefix = XMLRPC_PREFIX;

		try {
			initHandler(prefix, handlerName);

			//
			// FIXME: The metaweblog API should be possible to turn off.
			//
			initHandler("metaWeblog",
			        "com.ecyrd.jspwiki.xmlrpc.MetaWeblogHandler");
		} catch(Exception e) {
			log.fatal("Unable to start RPC interface: ", e);
			throw new ServletException("No RPC interface", e);
		}
	}

	/**
	 * Handle HTTP POST. This is an XML-RPC call, and we'll just forward the
	 * query to an XmlRpcServer.
	 */
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException {
		log.debug("Received POST to RPCServlet");

		try {
			WikiContext ctx = m_engine.createContext(request, WikiContext.NONE);

			XmlRpcContext xmlrpcContext = new WikiXmlRpcContext(m_xmlrpcServer.getHandlerMapping(),
			        ctx);

			byte[] result = m_xmlrpcServer.execute(request.getInputStream(), xmlrpcContext);

			//
			// I think it's safe to write the output as UTF-8:
			// The XML-RPC standard never creates other than USASCII
			// (which is UTF-8 compatible), and our special UTF-8
			// hack just creates UTF-8. So in all cases our butt
			// should be covered.
			//
			response.setContentType("text/xml; charset=utf-8");
			response.setContentLength(result.length);

			OutputStream out = response.getOutputStream();
			out.write(result);
			out.flush();

			// log.debug("Result = "+new String(result) );
		} catch(IOException e) {
			throw new ServletException("Failed to build RPC result", e);
		}
	}

	/**
	 * Handles HTTP GET. However, we do not respond to GET requests, other than
	 * to show an explanatory text.
	 */
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException {
		log.debug("Received HTTP GET to RPCServlet");

		try {
			String msg = "We do not support HTTP GET here.  Sorry.";
			response.setContentType("text/plain");
			response.setContentLength(msg.length());

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));

			writer.println(msg);
			writer.flush();
		} catch(IOException e) {
			throw new ServletException("Failed to build RPC result", e);
		}
	}

	private static class LocalHandler
	        implements ContextXmlRpcHandler {
		private Class m_clazz;

		public LocalHandler(Class clazz) {
			m_clazz = clazz;
		}

		public Object execute(String method, Vector params, XmlRpcContext context) throws Exception {
			WikiRPCHandler rpchandler = (WikiRPCHandler)m_clazz.newInstance();
			rpchandler.initialize(((WikiXmlRpcContext)context).getWikiContext());

			Invoker invoker = new Invoker(rpchandler);

			return invoker.execute(method, params);
		}
	}

	private static class WikiXmlRpcContext
	        implements XmlRpcContext {
		private XmlRpcHandlerMapping m_mapping;
		private WikiContext m_context;

		public WikiXmlRpcContext(XmlRpcHandlerMapping map, WikiContext ctx) {
			m_mapping = map;
			m_context = ctx;
		}

		public XmlRpcHandlerMapping getHandlerMapping() {
			return m_mapping;
		}

		public String getPassword() {
			// XXX Auto-generated method stub
			return null;
		}

		public String getUserName() {
			// XXX Auto-generated method stub
			return null;
		}

		public WikiContext getWikiContext() {
			return m_context;
		}
	}
}
