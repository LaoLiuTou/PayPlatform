package com.payplatform.controller.wxpay;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payplatform.utils.HttpRequest;
import com.payplatform.utils.PayCommonUtil;
import com.payplatform.utils.RSAUtils;
import com.payplatform.utils.wxpay.IWxPayConfig;
import com.payplatform.utils.wxpay.WXPay;
import com.payplatform.utils.wxpay.WXPayUtil;
@Controller
public class WxpayController {
	 
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Logger logger = Logger.getLogger("PayPlatformLogger");
	 
	 /**
	  * 统一下单
	  * @return
	  */
	@SuppressWarnings({ "rawtypes" })
	@RequestMapping("/wxpay")
	@ResponseBody
	public Map wxpay(HttpServletRequest request){
		// 发起微信支付
		WXPay wxpay = null;
		Map<String, String> result = new HashMap<String, String>();
		try {
			//接受参数(金额)
			String total_fee = request.getParameter("total_fee");
			//接受参数(openid)
			String openid = request.getParameter("openid");
			String out_trade_no = request.getParameter("out_trade_no");
			
			String body = request.getParameter("body");
			String nonceStr=PayCommonUtil.getRandomString(32);
			IWxPayConfig iWxPayConfig = new IWxPayConfig();
		    wxpay = new WXPay(iWxPayConfig); // *** 注入自己实现的微信配置类, 创建WXPay核心类, WXPay 包括统一下单接口
		    String spbill_create_ip = PayCommonUtil.getIpAddr(request);
		    Map<String, String> data = new HashMap<String, String>();
	        data.put("body", body);
	        data.put("out_trade_no", out_trade_no);
	        data.put("device_info", "WEB");
	        data.put("fee_type", "CNY");
	        data.put("total_fee", total_fee);
	        data.put("spbill_create_ip",spbill_create_ip);
	        data.put("notify_url", iWxPayConfig.getNotify_url());
	        //data.put("trade_type", "NATIVE");  // 此处指定为扫码支付
	        data.put("trade_type", "JSAPI");  //  
	        data.put("product_id", "12");
	        data.put("openid", openid);
	        data.put("nonce_str", nonceStr);
	    

	        
		    logger.info("发起微信支付下单接口, request={}"+ data);
		    Map<String, String> response = wxpay.unifiedOrder(data); // 微信sdk集成方法, 统一下单接口unifiedOrder, 此处请求   MD5加密   加密方式
		    logger.info("微信支付下单成功, 返回值 response={}"+response);
		    String returnCode = response.get("return_code");
		    if (!"SUCCESS".equals(returnCode)) {
		        return null;
		    }
		    String resultCode = response.get("result_code");
		    if (!"SUCCESS".equals(resultCode)) {
		    	return null;
		    }
		    String prepay_id = response.get("prepay_id");
		    if (prepay_id == null) {
		        return null;
		    }

		    // ******************************************
		    //
		    //  前端调起微信支付必要参数
		    //
		    // ******************************************
		    String packages = "prepay_id=" + prepay_id;
		    Map<String, String> wxPayMap = new HashMap<String, String>();
		    wxPayMap.put("appId", iWxPayConfig.getAppID());
		    wxPayMap.put("timeStamp", (System.currentTimeMillis() / 1000)+"");
		    wxPayMap.put("nonceStr", nonceStr);
		    wxPayMap.put("package", packages);
		    wxPayMap.put("signType", "MD5"); 
		     
		    // 加密串中包括 appId timeStamp nonceStr package signType 5个参数, 通过sdk WXPayUtil类加密, 注意, 此处使用  MD5加密  方式
		    String paySign = WXPayUtil.generateSignature(wxPayMap, iWxPayConfig.getKey());

		    // ******************************************
		    //
		    //  返回给前端调起微信支付的必要参数
		    //
		    // ******************************************
		    result.put("prepay_id", prepay_id);
		    result.put("paySign", paySign);
		    result.putAll(wxPayMap);
		    return result;
		} catch (Exception e) {
			 e.printStackTrace();
			 logger.info("支付失败："+e.getMessage());
			 return null;
		}
 
	}
	
	/**
	 * 回调
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/payCallback")
	@ResponseBody
	public String payCallback(HttpServletRequest request, HttpServletResponse response) {
	    logger.info("进入微信支付异步通知");
	    String resXml="";
	    try{
	        //
	        InputStream is = request.getInputStream();
	        //将InputStream转换成String
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder();
	        String line = null;
	        try {
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        resXml=sb.toString();
	        logger.info("微信支付异步通知请求包: {}"+ resXml);
	        
	        // ------------------------------
            // 处理业务完毕
            // ------------------------------
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();

 
	        return payBack(resXml);
	    }catch (Exception e){
	    	 e.printStackTrace();
	        logger.error("微信支付回调通知失败",e);
	        String result = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
	        return result;
	    }
	}
 
	@SuppressWarnings("unchecked")
	public String payBack(String notifyData) {
	    logger.info("payBack() start, notifyData={}"+ notifyData);
	    String xmlBack="";
	    Map<String, String> notifyMap = null;
	    try {
	    	IWxPayConfig iWxPayConfig = new IWxPayConfig();
	        WXPay wxpay = new WXPay(iWxPayConfig);

	        notifyMap = WXPayUtil.xmlToMap(notifyData);         // 转换成map
	        if (wxpay.isPayResultNotifySignatureValid(notifyMap)) {
	            // 签名正确
	            // 进行处理。
	            // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
	            String return_code = notifyMap.get("return_code");//状态
	            String out_trade_no = notifyMap.get("out_trade_no");//订单号

	            if (out_trade_no == null) {
	                logger.info("微信支付回调失败订单号: {}"+ notifyMap);
	                xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
	                return xmlBack;
	            }

	            
	            logger.info("微信支付回调成功订单号: {}"+notifyMap);
	            if(return_code.equals("SUCCESS")){
	            	// 业务逻辑处理 ****************************
	            	Properties properties = new Properties();
	        		String base = IWxPayConfig.class.getResource("/")
	        				.getPath();
	        		 
	        		properties.load(new FileInputStream(base
	        					+ "config/config.properties"));
	            	String url=properties.getProperty("service_url").trim();
	                Map<String,String> header=new HashMap<String,String>();
	                header.put("source", "xcx");
	                Map<String,String> map=new HashMap<String,String>();
	                map.put("pay_id", out_trade_no); 
	                map.put("status", "1");
	                String updateResult=HttpRequest.postMap(url, header,map);
	                ObjectMapper objectMapper = new ObjectMapper();
	    			Map<String,String> updateMap = (Map<String,String>)objectMapper.readValue(updateResult, Map.class);
	    			if(updateMap.get("status").equals("0")){
	    				xmlBack = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
	    			}
	    			else{
	    				xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[业务处理失败]]></return_msg>" + "</xml> ";
	    			}
	                logger.info("Post请求:"+updateResult);
	            	
	            }
	            //xmlBack = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
	            return xmlBack;
	        } else {
	            logger.error("微信支付回调通知签名错误");
	            xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
	            return xmlBack;
	        }
	    } catch (Exception e) {
	    	 e.printStackTrace();
	        logger.error("微信支付回调通知失败",e);
	        xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
	    }
	    return xmlBack;
	}

	/**
	  * 查询订单
	  * @return
	  */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/orderQuery")
	@ResponseBody
	public Map orderQuery(HttpServletRequest request){
		Map<String, String> resp = new HashMap<String, String>();
		try {
			String out_trade_no = request.getParameter("out_trade_no");
			IWxPayConfig iWxPayConfig = new IWxPayConfig();
			WXPay wxpay = new WXPay(iWxPayConfig);
	
	        Map<String, String> data = new HashMap<String, String>();
	        data.put("out_trade_no", out_trade_no);
	        resp = wxpay.orderQuery(data);
             
        } catch (Exception e) {
            e.printStackTrace();
        }
		return resp;

	}
	
	/**
	  * 退款
	  * @return
	  */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/orderRefund")
	@ResponseBody
	public Map orderRefund(HttpServletRequest request){
		Map<String, String> resp = new HashMap<String, String>();
		try {
			String out_trade_no = request.getParameter("out_trade_no");
			String refund_fee = request.getParameter("refund_fee");
			if(out_trade_no!=null&&refund_fee!=null){
				IWxPayConfig iWxPayConfig = new IWxPayConfig();
				WXPay wxpay = new WXPay(iWxPayConfig);
				
				Map<String, String> data = new HashMap<String, String>();
		        data.put("out_trade_no", out_trade_no);
		        Map<String, String> respsearch = wxpay.orderQuery(data);
		        String result_code = respsearch.get("result_code");
			    if ("SUCCESS".equals(result_code)) {
			    	String nonceStr=PayCommonUtil.getRandomString(32);
			    	String total_fee = respsearch.get("total_fee");
			    	String transaction_id = respsearch.get("transaction_id");
			    	
			    	Map<String, String> wxPayMap = new HashMap<String, String>();
				    wxPayMap.put("appid", iWxPayConfig.getMchAppID());
				    wxPayMap.put("mch_id", iWxPayConfig.getMchID());
				    wxPayMap.put("nonce_str", nonceStr);
				    wxPayMap.put("out_refund_no", (int)((Math.random()*9+1)*100000)+out_trade_no);
				    wxPayMap.put("out_trade_no", out_trade_no);
				    wxPayMap.put("refund_fee", (int)(Float.parseFloat(refund_fee)*100)+"");
				    wxPayMap.put("total_fee", total_fee);
				    wxPayMap.put("transaction_id", transaction_id);
				    //wxPayMap.put("signType", "MD5");
				    String refundSign = WXPayUtil.generateSignature(wxPayMap, iWxPayConfig.getKey());
				    wxPayMap.put("sign", refundSign);
			        resp = wxpay.refund(wxPayMap);
			         
			    }
			   
			}
			else{
				resp.put("result_code", "FAIL");
				resp.put("err_code", "PARAMISNULL");
			}
			 
       } catch (Exception e) {
           e.printStackTrace();
           logger.info("退款失败："+e.getMessage());
       }
		return resp;

	}
	
	/**
	  * 获取openid
	  * @return
	  */
	@RequestMapping("/getOpenId")
	@ResponseBody
	public String getOpenId(HttpServletRequest request){
		BufferedReader in = null;  
        try {  
        	IWxPayConfig iWxPayConfig = new IWxPayConfig();
    		String appid=iWxPayConfig.getAppID();
    		//String appid="wx2f8a44b4736bbe20";
    		String secret=iWxPayConfig.getSecret();
    		String code = request.getParameter("code");
    		System.out.println("code:"+code);
    		
            //appid和secret是开发者分别是小程序ID和小程序密钥，开发者通过微信公众平台-》设置-》开发设置就可以直接获取，
            String url="https://api.weixin.qq.com/sns/jscode2session?appid="
            +appid+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code";
            
			URL weChatUrl = new URL(url);  
            // 打开和URL之间的连接  
            URLConnection connection = weChatUrl.openConnection();  
            // 设置通用的请求属性  
            connection.setConnectTimeout(5000);  
            connection.setReadTimeout(5000);  
            // 建立实际的连接  
            connection.connect();  
            // 定义 BufferedReader输入流来读取URL的响应  
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));  
            StringBuffer sb = new StringBuffer();  
            String line;  
            while ((line = in.readLine()) != null) {  
                sb.append(line);  
            }  
            return sb.toString();  
        } catch (Exception e) {  
        	throw new RuntimeException(e);
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
	} 
	
	
	/**
	  * 企业付款到零钱
	  * @return
	  */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/payToChange")
	@ResponseBody
	public Map payToChange(HttpServletRequest request){
		Map<String, String> resp = new HashMap<String, String>();
		try {
			IWxPayConfig iWxPayConfig = new IWxPayConfig();
			WXPay wxpay = new WXPay(iWxPayConfig);
			String nonceStr=PayCommonUtil.getRandomString(32);
			String openid = request.getParameter("openid");
			String partnertradeno = request.getParameter("partnertradeno");
			String amount = request.getParameter("amount");
			String spbill_create_ip = PayCommonUtil.getIpAddr(request);
			
			Map<String, String> params = new HashMap<String, String>();
	        params.put("mch_appid", iWxPayConfig.getMchAppID());
	        params.put("mchid", iWxPayConfig.getMchID());
	        params.put("nonce_str", nonceStr);
	        params.put("partner_trade_no", partnertradeno);
	        params.put("openid", openid);
	        params.put("check_name", "NO_CHECK");
	        params.put("amount", amount);
	        params.put("desc", "链鹿平台转账");
	        params.put("spbill_create_ip", spbill_create_ip);
		    String refundSign = WXPayUtil.generateSignature(params, iWxPayConfig.getKey());
		    params.put("sign", refundSign);
		    
		    
	        resp = wxpay.payToChange(params);
       } catch (Exception e) {
           e.printStackTrace();
           logger.info("企业付款到零钱："+e.getMessage());
       }
		return resp;

	}
	/**
	  * 企业付款到银行卡
	  * @return
	  */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/payToBank")
	@ResponseBody
	public Map payToBank(HttpServletRequest request){
		Map<String, String> resp = new HashMap<String, String>();
		try {
			IWxPayConfig iWxPayConfig = new IWxPayConfig();
			String PUBLIC_KEY =iWxPayConfig.getPkcs8();
			WXPay wxpay = new WXPay(iWxPayConfig);
			String nonceStr=PayCommonUtil.getRandomString(32);
			String cardNum = request.getParameter("cardnum");
			String owner = request.getParameter("owner");
			String bank = request.getParameter("bank");
			String partnertradeno = request.getParameter("partnertradeno");
			String amount = request.getParameter("amount");
			Map<String, String> params = new HashMap<String, String>();
			params.put("mch_id", iWxPayConfig.getMchID());
            params.put("partner_trade_no", partnertradeno);
            params.put("nonce_str", nonceStr);
            params.put("enc_bank_no", RSAUtils.encryptByPublicKeyByWx(cardNum, PUBLIC_KEY));//收款方银行卡号
            params.put("enc_true_name", RSAUtils.encryptByPublicKeyByWx(owner, PUBLIC_KEY));//收款方用户名    
            params.put("bank_code", iWxPayConfig.getBankCode().get(bank));//收款方开户行        
            params.put("amount", amount);
            params.put("desc", "链鹿平台转账");
            String refundSign = WXPayUtil.generateSignature(params, iWxPayConfig.getKey());
		    params.put("sign", refundSign);
	        
	        resp = wxpay.payToBank(params);
	      
      } catch (Exception e) {
          e.printStackTrace();
          logger.info("企业付款到银行卡："+e.getMessage());
      }
		return resp;

	}
	
	
	/**
     * 查询企业付款到银行
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping("/queryBank")
	@ResponseBody
	public Map queryBank(HttpServletRequest request) {
    	Map<String, String> resp = new HashMap<String, String>();
        try {
        	IWxPayConfig iWxPayConfig = new IWxPayConfig();
			WXPay wxpay = new WXPay(iWxPayConfig);
			String partnertradeno = request.getParameter("partnertradeno");
			String nonceStr=PayCommonUtil.getRandomString(32);
            Map<String, String> params = new HashMap<String, String>();
            params.put("mch_id", iWxPayConfig.getMchID());
            params.put("nonce_str", nonceStr);
            params.put("partner_trade_no", partnertradeno);
            String refundSign = WXPayUtil.generateSignature(params, iWxPayConfig.getKey());
		    params.put("sign", refundSign);
		    
		    resp = wxpay.queryBank(params);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("查询企业付款到银行卡失败："+e.getMessage());
        }
        return resp;
    }
    
    
   
}
