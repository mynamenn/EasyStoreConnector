package com.lawrence;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;



@WebServlet(urlPatterns = {"/EasyStore", "/Ecwid"})
public class Connector extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String secretKey = "TEST";
	// fdc049f852580e9af81687bad31081a465faa0ddea88126b4ffa3cfec2bd3b08
	private HashMap<String, String> data = new HashMap<>();
	
	private HashMap<String, String> fieldDatas = new HashMap<>();
	
    public Connector() {
    	data.put("testing", "value");
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
    
    // Parse request formData into fieldDatas
    private void parseData(HttpServletRequest request, String company) {
    	List<FileItem> items;
		try {
			items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			for (FileItem item : items) {
	        	
	        	// For company name
	            if (item.isFormField()) {
	                // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
	                String fieldName = item.getFieldName();
	                String fieldData = item.getString();	
	                fieldDatas.put(fieldName, fieldData);
	            } 
	        }
			fieldDatas.put("company", company);
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		HashMap<String, String> datas = new HashMap<>();
		String company = fieldDatas.get("company");
		ServletContext context = request.getSession().getServletContext();
		
		if(company.equals("EasyStore")) {
			datas.put("amount", fieldDatas.get("x_amount"));
			datas.put("name", fieldDatas.get("x_customer_first_name")+ " "+ fieldDatas.get("x_customer_last_name"));
			datas.put("phoneNumber", fieldDatas.get("x_customer_billing_phone"));
			datas.put("email", fieldDatas.get("x_customer_email"));
			datas.put("accountId", fieldDatas.get("x_account_id"));
			datas.put("currency", fieldDatas.get("x_currency"));
			datas.put("reference", fieldDatas.get("x_reference"));
			datas.put("storeCountry", fieldDatas.get("x_store_country"));
			datas.put("storeName", fieldDatas.get("x_store_name"));
			datas.put("cancelLink", fieldDatas.get("x_url_cancel"));
			datas.put("completeLink", fieldDatas.get("x_url_complete"));
			datas.put("signature", fieldDatas.get("x_signature"));
			datas.put("callback", fieldDatas.get("x_url_callback"));
			datas.put("test", fieldDatas.get("x_test"));
			datas.put("company", fieldDatas.get("company"));
			context.setAttribute(fieldDatas.get("x_reference"), datas);
			
			Object attribute = context.getAttribute(fieldDatas.get("x_reference"));
			System.out.println(attribute);
			
		}else if(company.equals("Ecwid")) {
			datas.put("employeeId", fieldDatas.get("employeeId"));
			datas.put("merchantId", fieldDatas.get("merchantId"));
			datas.put("callback", fieldDatas.get("callback"));
			datas.put("amount", fieldDatas.get("amount"));
			datas.put("email", fieldDatas.get("email"));
			datas.put("phoneNumber", fieldDatas.get("phoneNumber"));
			datas.put("paymentMode", fieldDatas.get("paymentMode"));
			datas.put("payMethod", fieldDatas.get("payMethod"));
			datas.put("recurringMode", fieldDatas.get("recurringMode"));
			datas.put("orderId", fieldDatas.get("orderId"));
			datas.put("name", fieldDatas.get("name"));
			datas.put("reference", fieldDatas.get("reference"));
			datas.put("company", fieldDatas.get("company"));
			
			context.setAttribute(fieldDatas.get("reference"), datas);
			
			Object attribute = context.getAttribute(fieldDatas.get("reference"));
			System.out.println(attribute);
		}
		
		PrintWriter out = response.getWriter();
		out.print(datas);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		String redirectUrl = "https://localhost/EasyStore/";
		PrintWriter out = response.getWriter();
		
		// Check if it's Ecwid or EasyStore
		// Returns /EasyStore/EasyStore or /EasyStore/Ecwid
		String path = request.getRequestURI().split("/")[2];
	
		if(path.equals("EasyStore")) {
			out.println("EasyStore datas received!");
			// Put into fieldDatas hash map
			parseData(request, "EasyStore");
			
			// Redirect to index.html if true
			if(generateSignature( data, secretKey, fieldDatas.get("x_signature"))) {
				try {
					Desktop.getDesktop().browse(new URI(redirectUrl));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}
			
		}else if(path.equals("Ecwid")) {
			out.println("Ecwid datas received!");
			parseData(request, "Ecwid");
			try {
				Desktop.getDesktop().browse(new URI(redirectUrl));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
	}
}

