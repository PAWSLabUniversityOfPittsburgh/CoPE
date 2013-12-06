package edu.pitt.sis.paws.cope;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: TestServlet
 *
 */
 public class TestServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
 {
	static final long serialVersionUID = -2L;
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public TestServlet() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	       System.out.println("Total Memory "+Runtime.getRuntime().totalMemory());    
	       System.out.println("Free Memory  "+Runtime.getRuntime().freeMemory() + "\n");
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       System.out.println("Total Memory"+Runtime.getRuntime().totalMemory());    
       System.out.println("Free Memory"+Runtime.getRuntime().freeMemory());
	}
 }