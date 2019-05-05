package com.payplatform.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpRequest {
	/**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求Map参数，请求参数应该是 {"name1":"value1","name2":"value2"}的形式。
     * @param charset         
     *             发送和接收的格式
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, Map<String,Object> map,String charset){
          StringBuffer sb=new StringBuffer();
          //构建请求参数
          if(map!=null&&map.size()>0){
              Iterator it=map.entrySet().iterator(); //定义迭代器
              while(it.hasNext()){
                 Map.Entry  er= (Entry) it.next();
                 sb.append(er.getKey());
                 sb.append("=");
                 sb.append(er.getValue());
                 sb.append("&");
             }
          }
       return  sendGet(url,sb.toString(), charset);
    }


    /**
     * 向指定URL发送POST方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求Map参数，请求参数应该是 {"name1":"value1","name2":"value2"}的形式。
     * @param charset         
     *             发送和接收的格式
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String,Object> map,String charset){
          StringBuffer sb=new StringBuffer();
          //构建请求参数
          if(map!=null&&map.size()>0){
                for (Entry<String, Object> e : map.entrySet()) {  
                    sb.append(e.getKey());  
                    sb.append("=");  
                    sb.append(e.getValue());  
                    sb.append("&");  
                }  
          }
       return  sendPost(url,sb.toString(),charset);
    }


    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param charset         
     *             发送和接收的格式
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param,String charset) {
        String result = "";
        String line;
        StringBuffer sb=new StringBuffer();
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性 设置请求格式
            conn.setRequestProperty("contentType", charset); 
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            //设置超时时间
            conn.setConnectTimeout(60);
            conn.setReadTimeout(60);
            // 建立实际的连接
            conn.connect();
            // 定义 BufferedReader输入流来读取URL的响应,设置接收格式
            in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(),charset));
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result=sb.toString();
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param charset         
     *             发送和接收的格式       
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param,String charset) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String line;
        StringBuffer sb=new StringBuffer();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接 
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性 设置请求格式
            conn.setRequestProperty("contentType", charset);  
            //conn.setRequestProperty("content-type", "application/x-www-form-urlencoded"); 
            conn.setRequestProperty("source", "xcx"); 
            //设置超时时间
            conn.setConnectTimeout(60);
            conn.setReadTimeout(60);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
          
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应    设置接收格式
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),charset));
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result=sb.toString();
        } catch (Exception e) {
            System.out.println("发送 POST请求出现异常!"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }   
    


    @SuppressWarnings({ "rawtypes", "unchecked" })
   	public static String postMap(String url,Map<String,String> headerMap,Map<String, String> contentMap) {
       String result = null;
       CloseableHttpClient httpClient = HttpClients.createDefault();
       HttpPost post = new HttpPost(url);
       List<NameValuePair> content = new ArrayList<NameValuePair>();
       if(contentMap!=null&&contentMap.size()>0){
	    Iterator iterator = contentMap.entrySet().iterator();           //将content生成entity
	    while(iterator.hasNext()){  
	        Entry<String,String> elem = (Entry<String, String>) iterator.next();  
	        content.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));  
	    }
       }
       CloseableHttpResponse response = null;
       try {
       	if(headerMap!=null&&headerMap.size()>0){
       		 Iterator headerIterator = headerMap.entrySet().iterator();          //循环增加header
                while(headerIterator.hasNext()){  
                    Entry<String,String> elem = (Entry<String, String>) headerIterator.next();  
                    post.addHeader(elem.getKey(),elem.getValue());
                }
       	}
          
           if(content.size() > 0){  
               UrlEncodedFormEntity entity = new UrlEncodedFormEntity(content,"UTF-8");  
               post.setEntity(entity);
           }
           response = httpClient.execute(post);            //发送请求并接收返回数据
           if(response != null && response.getStatusLine().getStatusCode() == 200)
           {
               HttpEntity entity = response.getEntity();       //获取response的body部分
               result = EntityUtils.toString(entity);          //读取reponse的body部分并转化成字符串
           }
           return result;
       } catch (UnsupportedEncodingException e) {
           e.printStackTrace();
       } catch (ClientProtocolException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }finally {
           try {
               httpClient.close();
               if(response != null)
               {
                   response.close();
               }
           } catch (IOException e) {
               e.printStackTrace();
           }

       }
       return null;
   } 
    public static void main(String[] args) {
        String url="http://192.168.1.144/DeerShop/updatePayByPayId";
        
        Map<String,String> header=new HashMap<String,String>();
        header.put("source", "xcx");
        Map<String,String> map=new HashMap<String,String>();
        map.put("pay_id", "2018110716203612332630688"); 
        map.put("status", "1");
         
        System.out.println("Post请求:"+HttpRequest.postMap(url, header,map));
    }
 
}
