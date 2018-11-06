package com.payplatform.utils;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

public class PayCommonUtil {
	//随机字符串生成  
    public static String getRandomString(int length) { //length表示生成字符串的长度      
         String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";         
         Random random = new Random();         
         StringBuffer sb = new StringBuffer();         
         for (int i = 0; i < length; i++) {         
             int number = random.nextInt(base.length());         
             sb.append(base.charAt(number));         
         }         
         return sb.toString();         
    }    
    /**
	 * IpUtils工具类方法
	 * 获取真实的ip地址
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
	    if(ip!=null && !"unKnown".equalsIgnoreCase(ip)){
	         //多次反向代理后会有多个ip值，第一个ip才是真实ip
	    	int index = ip.indexOf(",");
	        if(index != -1){
	            return ip.substring(0,index);
	        }else{
	            return ip;
	        }
	    }
	    ip = request.getHeader("X-Real-IP");
	    if(ip!=null && !"unKnown".equalsIgnoreCase(ip)){
	       return ip;
	    }
	    return request.getRemoteAddr();
	}
 
}
