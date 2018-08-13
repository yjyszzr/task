package com.dl.task.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Http工具类
 */
public class HttpUtil {
	private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	 public static String sendMsg(String postData, String postUrl,boolean isJson) {
		 logger.info("postUrl={},postData={}",postUrl,postData);
		 OutputStreamWriter out = null;
		 HttpURLConnection conn = null;
	        try {
	            //发送POST请求
	            URL url = new URL(postUrl);
	            conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("POST");
	            if(isJson) {
	            	conn.setRequestProperty("Content-Type", "application/json");
	            }else {
	            	conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	            }
	            conn.setRequestProperty("Connection", "Keep-Alive");
	            conn.setUseCaches(false);
	            conn.setDoOutput(true);
	            if(postData != null && postData.length() > 0) {
	            	conn.setRequestProperty("Content-Length", "" + postData.length());
		            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		            out.write(postData);
		            out.flush();
	            }
	            int code = conn.getResponseCode();
	            //获取响应状态
	            if (code != HttpURLConnection.HTTP_OK) {
	                return null;
	            }
	            //获取响应内容体
	            String line, result = "";
	            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
	            while ((line = in.readLine()) != null) {
	                result += line + "\n";
	            }
	            in.close();
	            logger.info("HttpUtil.sendMsg response={}",result);
	            return result;
	        } catch (IOException e) {
	            logger.error("",e);
	            return null;
	        }finally {
	        	if(out != null) {
	        		try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        }
	    }
	 
}
