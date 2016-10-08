package com.protobuftest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class HttpClientRequest {

	public final static String RequestMethod_GET = "GET";
	public final static String RequestMethod_POST = "POST";
	public final static String RequestMethod_PUT = "PUT";
	public final static String RequestMethod_DELETE = "DELETE";
	
	public static ResponseResult request(String url, String requestMethod, 
			          Map<String, String> requestMap, byte[] byteArray) throws Exception{
		String domain = getDomain(url);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpRequestBase httprequest = null;
		HttpContext localContext = new BasicHttpContext();
		if(RequestMethod_GET.equalsIgnoreCase(requestMethod)){
			// if requestMap is not empty, add all the parameters into url
			if(requestMap!=null && !requestMap.isEmpty()){
				url = concatMapParam(url, requestMap);
			}
			httprequest = new HttpGet(url);
		}else if(RequestMethod_POST.equalsIgnoreCase(requestMethod)){
			HttpPost postrequest = new HttpPost(url);
			if(byteArray==null){
				if(requestMap!=null && !requestMap.isEmpty()){
					String cookieKey = null;
				    List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				    for(Map.Entry<String, String> entry : requestMap.entrySet()){
				    	cookieKey = getCookieKey(entry.getKey());
				    	if(cookieKey!=null){
				    	    addCookie(localContext, cookieKey, entry.getValue(), domain);
				    	}else {
				    		nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				    	}
				    }
				    postrequest.setEntity(new UrlEncodedFormEntity(nvps));
				}
			}else {
				url = concatMapParam(url, requestMap);
				postrequest.addHeader("Content-Type", "application/octet-stream;charset=utf-8");  
				postrequest.setEntity(new ByteArrayEntity(byteArray));   
			}
			
			httprequest = postrequest;
		}
		
		CloseableHttpResponse response = httpclient.execute(httprequest, localContext);
		ResponseResult resultObj = null;
		int statusCode = 0;
		String resultString = null;
		try {
		    HttpEntity entity_result = response.getEntity();
		    statusCode = response.getStatusLine().getStatusCode();
		    boolean isStream = entity_result.isStreaming();
		    if(isStream){
		    	// clone stream, because will close the stream and response in the end, and also the response stream should be small size
		    	InputStream resultStream = cloneInputStream(entity_result.getContent());
		    	resultObj = new ResponseResult(statusCode, null, resultStream, isStream, null);
		    }else {
		    	resultString = EntityUtils.toString(entity_result);
		    	resultObj = new ResponseResult(statusCode, resultString, null, isStream, null);
		    }
		    // do something useful with the response body
		    // and ensure it is fully consumed
		    EntityUtils.consume(entity_result);
		} catch(Exception e){
			if(statusCode==0){
				statusCode = 500;
			}
			String errorMessage = e.getMessage()+" | caused by: "+e.getCause().getMessage();
			resultObj = new ResponseResult(statusCode, resultString, null, false, errorMessage);
		}finally {
		    response.close();
		}
		
		return resultObj;
	}
	
	private static InputStream cloneInputStream(InputStream inputstream) throws IOException{
		if(inputstream==null){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while((length=inputstream.read(buffer))>-1){
			baos.write(buffer, 0, length);
		}
		baos.flush();
		InputStream resultStream = new ByteArrayInputStream(baos.toByteArray());
		return resultStream;
	}
	
	public static class ResponseResult{
		private int statuscode;
		private String resultString;
		private InputStream resultStream;
		private boolean isStream;
		private String errorMessage;
		
		public ResponseResult(int statuscode, String resultString, InputStream resultStream, boolean isStream, String errorMessage) {
			this.statuscode = statuscode;
			this.resultString = resultString;
			this.resultStream = resultStream;
			this.isStream = isStream;
			this.errorMessage = errorMessage;
		}

		public int getStatuscode() {
			return statuscode;
		}

		public String getResultString() {
			return resultString;
		}

		public InputStream getResultStream() {
			return resultStream;
		}

		public boolean isStream() {
			return isStream;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public boolean isSuccess() {
			return (statuscode==200)?true:false;
		}
		
	}
	
	private static String concatMapParam(String url, Map<String, String> requestMap){
		if(requestMap!=null && !requestMap.isEmpty()){
			StringBuffer buffer = new StringBuffer();
		    for(Map.Entry<String, String> entry : requestMap.entrySet()){
		    	buffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		    }
		    if(url.endsWith("?")||url.endsWith("&")){
		    	url = url + buffer.substring(0, buffer.length()-1);
		    }else {
		    	url = url + "?"+buffer.substring(0, buffer.length()-1);
		    }
		}
		return url;
	}
	
	public static String getDomain(String url){
		String domain = null;
		try {
			URL aURL = new URL(url);
			domain = aURL.getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		// or use regex
//		Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(us|com|cn|net|org|biz|info|tv)",Pattern.CASE_INSENSITIVE);
//		Matcher matcher = p.matcher(url);
//		if(matcher.find()){
//			domain = matcher.group();
//		}
		return domain;
	}
	
//	public static void main(String[] args){
//		System.out.println(getCookieKey("Cookie[name]"));
//	}
	
	// if has cookie format, then return key, if key is empty, then return null
	// the cookie parameter like this: cookie[key]=aabbcc
	private static String getCookieKey(String parameter){
		String key = null;
		Pattern p = Pattern.compile("(Cookie|cookie)\\[(.+)?\\]",Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(parameter);
		if(matcher.find()){
			key = matcher.group(2);
		}
		return key;
	}
	
	private static void addCookie(HttpContext localContext, String key, String value, String domain){
		if(key!=null && !"".equals(key)){
			BasicCookieStore cookieStore = (BasicCookieStore)localContext.getAttribute(HttpClientContext.COOKIE_STORE);
			if(cookieStore==null){
				cookieStore = new BasicCookieStore();
			}
			localContext.getAttribute(HttpClientContext.COOKIE_STORE);
		    BasicClientCookie cookie = new BasicClientCookie(key, value);
		    cookie.setDomain(domain); // e.g.: ".github.com"
		    cookie.setPath("/");
		    cookieStore.addCookie(cookie);
		    localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		}
	}
	
}
