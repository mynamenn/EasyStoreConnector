package com.lawrence;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.net.www.http.HttpClient;


@WebServlet("/Connector")
public class Connector extends HttpServlet {
	private final String secretKey = "TEST";
	
	private final HashMap<String, String> data = new HashMap<>();
	
	private String x_account_id;
	
	private String x_amount;
	
	private String x_currency;
	
	private String x_reference;
	
	private String x_store_country;
	
	private String x_store_name;
	
	private String x_test;
	
	private String x_url_callback;
	
	private String x_url_cancel;
	
	private String x_url_complete;
    
	private String x_signature;
	
	private String x_customer_first_name;
	
	private String x_customer_last_name;
	
	private String x_customer_email;
	
	private String x_customer_billing_phone;
	
    public Connector() {
        super();
    }
    
    
    private boolean generateSignature( HashMap<String, String> data, String secretkey, String x_signature){
    	// Sort data in alphabetical order
    	TreeMap<String, String> sorted = new TreeMap<>();
    	sorted.putAll(data);
    	
    	String sourceString = "";
    	String signature = "";
    	
    	for(Map.Entry<String, String> entry : sorted.entrySet()) {
    		if(entry.getKey().equals(x_signature)) {
    			continue;
    		}
    		sourceString += entry.getKey() + entry.getValue();
    	}
    	
    	// Hash sourceString with sercretKey
    	try {
			signature = hashMac(sourceString, secretkey);
		} catch (SignatureException e) {
			e.printStackTrace();
		}
    	
    	if(signature.equals(x_signature)) {
    		return true;
    	}else {
    		return false;
    	} 
    }
    
    private static String toHexString(byte[] bytes) {  
	    StringBuilder sb = new StringBuilder(bytes.length * 2);  

	    Formatter formatter = new Formatter(sb);  
	    for (byte b : bytes) {  
	        formatter.format("%02x", b);  
	    }  

	    return sb.toString();  
	} 
    
    private static String hashMac(String text, String secretKey) throws SignatureException {
		String HASH_ALGORITHM = "HmacSHA256";
		try {
			  Key sk = new SecretKeySpec(secretKey.getBytes(), HASH_ALGORITHM);
			  Mac mac = Mac.getInstance(sk.getAlgorithm());
			  mac.init(sk);
			  final byte[] hmac = mac.doFinal(text.getBytes());
			  return toHexString(hmac);
			  
			 }catch (NoSuchAlgorithmException e1) {
				 
				  // throw an exception or pick a different encryption method
				  throw new SignatureException(
				    "error building signature, no such algorithm in device "
				      + HASH_ALGORITHM);
				  
			 } catch (InvalidKeyException e) {
			  throw new SignatureException(
			    "error building signature, invalid key " + HASH_ALGORITHM);
			 }
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		HashMap<String, String> datas = new HashMap<>();
		datas.put("amount", "44");
		datas.put("firstName", "");
		datas.put("lastName", "Ngu");
		datas.put("phoneNumber", "0167888888");
		datas.put("email", "lawrencengu@gmail.com");
		PrintWriter out = response.getWriter();
		out.print(datas);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		String queryString = request.getQueryString();
		x_signature = request.getParameter("x_signature");
		
		// Verify x_signature. If verified, allocate the variables and post to curlec.
		if(generateSignature(data, secretKey, x_signature)) {
			// Required fields
			x_account_id = request.getParameter("x_account_id");
			x_amount = request.getParameter("x_amount");
			x_currency = request.getParameter("x_currency");
			x_reference = request.getParameter("x_reference");
			x_store_country = request.getParameter("x_store_country");
			x_store_name = request.getParameter("x_store_name");
			x_test = request.getParameter("x_test");
			x_url_callback = request.getParameter("x_url_callback");
			x_url_cancel = request.getParameter("x_url_cancel");
			x_url_complete = request.getParameter("x_url_complete");
			
			// Non-mandatory fields
			x_customer_first_name = request.getParameter("x_customer_first_name");
			x_customer_last_name = request.getParameter("x_customer_last_name");
			x_customer_email = request.getParameter("x_customer_email");
			x_customer_billing_phone = request.getParameter("x_customer_billing_phone");
			
			
	}

}
	}
