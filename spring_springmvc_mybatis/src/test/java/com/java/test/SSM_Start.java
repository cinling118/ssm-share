package com.java.test;

import org.mortbay.jetty.Server;

public class SSM_Start {
	public static final int PORT = 8889;
	public static final String CONTEXT = "/";
	public static final String BASE_URL = "http://localhost:"+PORT+CONTEXT;

	public static void main(String[] args) throws Exception {
		System.setProperty("B2BCENTER_HOME", "C:/supply-chain");
		System.setProperty("java.awt.headless", "true");

		Server server = JettyUtils.buildDebugServer(PORT, CONTEXT);
		server.start();
		System.out.println(BASE_URL);

		if (System.in.read() != 0) {
			server.stop();
			System.out.println("Server stopped");
		}
	}
}
