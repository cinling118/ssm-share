package com.java.test;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class JettyUtils {

	public static Server buildDebugServer(int port, String context) {
		Server server = new Server(port);
		WebAppContext webContext = new WebAppContext("src/main/webapp", context);
		webContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		server.setHandler(webContext);
		server.setStopAtShutdown(true);
		return server;
	}

}
