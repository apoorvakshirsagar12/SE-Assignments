package com.mvc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mvc.util.DBConnection;

/**
 * Servlet implementation class ViewParkingSpace
 */
@WebServlet("/ViewParkinSpace")
public class ViewParkingSpace extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet rSet = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ViewParkingSpace() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String output = "";
		String id = (String) request.getSession().getAttribute("user_id");
		PrintWriter out = null;
		try {
			out = response.getWriter();

			connection = DBConnection.createConnection();
			statement = connection.createStatement();
			String sql = "select * from parking where status = 'approved' and Owner_PO_id=(select PO_id from parkingowner where Users_user_id = " + id + " )";
			rSet = statement.executeQuery(sql);

			output = output + "<table class='table table-striped table-list' height=30px border=1px>";
			output = output + "<th>Parking Name</th><th>Address</th><th>State</th><th>City</th><th>Zip</th><th>Action</th>";

			while (rSet.next()) {
				output = output + "<tr>";

				output = output + "<td>" + rSet.getString("P_name") + "</td><td>" + rSet.getString("P_address")
						+ "</td><td>" + rSet.getString("P_state") + "</td><td>" + rSet.getString("P_city") + "</td><td>"
						+ rSet.getString("P_zip") + "</td>";

				output = output + "<td><button class='btn btn-primary' onclick=\"editRecord(" + rSet.getInt("P_id")
						+ ");\"> Edit</button></td>";
				output = output + "</tr>";
			}
			output = output + "</table>";

			/*if (!rSet.isBeforeFirst() ) {    
			    output=null; 
			}*/ 
			
			statement.close();
			rSet.close();

			out.println(output);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
