package dbcad;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DBCADServlet
 */
@WebServlet("/DBCADServlet")
public class DBCADServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private RepositoryHandler repHandler;
    /**
     * Default constructor. 
     */
    public DBCADServlet() {
    	repHandler = new RepositoryHandler();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String reqStr = request.getParameter("req");
		switch (reqStr){
			case "check"	: check(request,response,request.getParameter("req_data")); break;
			case "manage"	: manage(request,response); break;
		}
	}

	private void manage(HttpServletRequest request, HttpServletResponse response) {
		try{
			JSONObject jsonMetadata = new JSONObject();
			ArrayList<String> databaseInstances = repHandler.getDatabaseIds();
			JSONArray jsonDatbaseInstanceList = new JSONArray();
			for (String databaseInstance : databaseInstances){
				jsonDatbaseInstanceList.put(databaseInstance);
			}
			jsonMetadata.put("database_instances", jsonDatbaseInstanceList);
			MetaDataBean metaDataBean = new MetaDataBean();
			metaDataBean.setMetadataJson(jsonMetadata);
			request.setAttribute("metaDataBean", metaDataBean);
			
			RequestDispatcher dispatcher = request.getRequestDispatcher("ManageDatabases.jsp");
			dispatcher.forward(request, response);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void check(HttpServletRequest request, HttpServletResponse response, String reqDataStr) {
		PrintWriter out=null;
		JSONObject jsonRequest = null;
		try {
			out = response.getWriter();
			jsonRequest = new JSONObject(reqDataStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String lob_id = jsonRequest.getString("lob");
		JSONArray jsonReqArray = jsonRequest.getJSONArray("db_change_ids");
		JSONObject jsonResponse = new JSONObject();
		String dbChangeId;
		for (int i=0; i < jsonReqArray.length(); i++){
			dbChangeId = jsonReqArray.getString(i);
			jsonResponse.put(dbChangeId, repHandler.checkDbChanges(dbChangeId, lob_id));
		}
		out.print(jsonResponse);
	}

}
