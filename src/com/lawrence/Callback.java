package com.lawrence;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/Callback")
/**
 * doPost is called as https://localhost/EasyStore/callback.
 * It is passed as merchantUrl in curlec so when payment is done, curlec POST 
 * data to this servlet.
 */
public class Callback extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, String> notInCurlecUrlDict = new HashMap<>(); 
	
	private HashMap<String, String> curlecResponseDict = new HashMap<>(); 
       
    public Callback() {
        super();
       
    }
    
    protected HashMap<String, String> getDatas(HttpServletRequest request, String reference){
		ServletContext context = request.getSession().getServletContext();
		Object attribute = context.getAttribute(reference);
		
		String datas = String.valueOf(attribute);
		datas = datas.substring(1, datas.length()-1);     
		String[] keyValuePairs = datas.split(",");         
		HashMap<String,String> map = new HashMap<>();               

		for(String pair : keyValuePairs)                    
		{
		    String[] entry = pair.split("=");                  
		    map.put(entry[0].trim(), entry[1].trim());          
		}
		
		return map;
    }
    
    protected String[] resultToUrl(String code, String completeLink, String cancelLink) {
		String parsedResult = "";
		String redirectUrl = "";
		switch(code) {
		case "00":
			parsedResult = "completed";
			redirectUrl = completeLink;
			break;
		case "-6": case "99":
			parsedResult = "pending";
			break;
		default:
			parsedResult = "failed";
			redirectUrl = cancelLink;
			break;
		}
		
		String[] arr = new String[2];
		arr[0] = parsedResult;
		arr[1] = redirectUrl;
		
		return arr;
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = request.getSession().getServletContext();
		Object attribute = context.getAttribute("10001");
		
		String datas = String.valueOf(attribute);
		datas = datas.substring(1, datas.length()-1);     
		String[] keyValuePairs = datas.split(",");         
		Map<String,String> map = new HashMap<>();               

		for(String pair : keyValuePairs)                    
		{
		    String[] entry = pair.split("=");                  
		    map.put(entry[0].trim(), entry[1].trim());          
		}
	}

	
	/**
	 * Post to x_url_callback
	 * Post accountId, amount, currency, gatewayReference, reference,
	 * result, signature, x_test
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		String queryString = request.getQueryString();
		// Remove https and decode until ?
		String[] queries = queryString.substring(5).split("?");
//		String[] datasNotInCurlecUrl = java.net.URLDecoder.decode(queries[0], StandardCharsets.UTF_8.name()).split("&");
		String[] curlecResponse = queries[1].split("&");
		
		// Put both array into hashMap
//		for(int i = 0; i < datasNotInCurlecUrl.length; i++) {
//			String[] datas = datasNotInCurlecUrl[i].split("=");
//			notInCurlecUrlDict.put(datas[0], datas[1]);
//		}
		for(int i = 0; i < curlecResponse.length; i++) {
			String[] datas = curlecResponse[i].split("=");
			curlecResponseDict.put(datas[0], datas[1]);
		}
		
		// Put session data in hashmap
		HashMap<String, String> datas = getDatas(request, curlecResponseDict.get("fpx_sellerOrderNo"));
		
		if(datas.get("company").equals("EasyStore")) {
			// Parse result code to either pending, failed, completed.
			String[] parsed = resultToUrl(curlecResponseDict.get("fpx_debitAuthCode"), 
					datas.get("completeLink"), datas.get("cancelLink"));
			String parsedResult = parsed[0];
			String redirectUrl = parsed[1];
			
			// Build urlParameters
			// Different companies have different callback urls
			String urlParameters = "x_account_id=" + datas.get("accountId") + 
					"&x_currency=" + curlecResponseDict.get("fpx_txnCurrency") + "&x_reference=" + datas.get("reference") + 
					"&x_amount=" + curlecResponseDict.get("fpx_txnAmount") + "&x_gateway_reference=" + curlecResponseDict.get("fpx_fpxTxnId") + 
					"&x_result=" + parsedResult + "&x_signature=" + datas.get("signature") + 
					"&x_test=" + datas.get("test");
			
			
			// Asynchronous post for EasyStore
			// HTTP POST x-www-form-urlencoded format
			String x_callback_url = datas.get("callback");
			byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
			int postDataLength = postData.length;
			URL url = new URL( x_callback_url );
			HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
			conn.setUseCaches(false);
			try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			   wr.write( postData );
			}
			
			// Redirect depending on status
			if(redirectUrl != "") {
				response.sendRedirect(redirectUrl);
			}
		}else if(datas.get("company").equals("Ecwid")) {
			// Parse result code and redirect to respective url
			
		}
		
		
		
		
	}

}
