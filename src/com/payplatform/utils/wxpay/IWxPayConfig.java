package com.payplatform.utils.wxpay;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.payplatform.utils.wxpay.IWXPayDomain;
import com.payplatform.utils.wxpay.WXPayConfig;
import com.payplatform.utils.wxpay.WXPayConstants;
public class IWxPayConfig extends WXPayConfig { // 继承sdk WXPayConfig 实现sdk中部分抽象方法

    private byte[] certData;

    private String app_id;

    private String wx_pay_key;

    private String wx_pay_mch_id;
    
    private String wx_pay_mch_app_id;
    
    private String wx_pay_cert_path;

    private String app_secret;
    
    private String notify_url;
    
    private Map<String,String> bankMap;
    
    private String pkcs8;
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public IWxPayConfig() throws Exception { // 构造方法读取证书, 通过getCertStream 可以使sdk获取到证书
        //String certPath = "/data/config/chidori/apiclient_cert.p12";
    	
    	Properties properties = new Properties();
		String base = IWxPayConfig.class.getResource("/")
				.getPath();
		 
		properties.load(new FileInputStream(base
					+ "config/config.properties"));
		app_id = properties.getProperty("wx.pay.app_id").trim();  
		wx_pay_key = properties.getProperty("wx.pay.key").trim();  
		wx_pay_mch_id = properties.getProperty("wx.pay.mch_id").trim();  
		wx_pay_mch_app_id = properties.getProperty("wx.pay.mch_appid").trim();  
		wx_pay_cert_path = properties.getProperty("wx.pay.cert").trim();  
		app_secret = properties.getProperty("app_secret").trim();  
		notify_url = properties.getProperty("notify_url").trim();  
		pkcs8 = properties.getProperty("wx.pay.pkcs8").trim();  
        File file = new File(wx_pay_cert_path);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
        Map temp = new HashMap();
        temp.put("工商银行","1002");
        temp.put("农业银行","1005");
        temp.put("中国银行","1026");
        temp.put("建设银行","1003");
        temp.put("招商银行","1001");
        temp.put("邮储银行","1066");
        temp.put("交通银行","1020");
        temp.put("浦发银行","1004");
        temp.put("民生银行","1006");
        temp.put("兴业银行","1009");
        temp.put("平安银行","1010");
        temp.put("中信银行","1021");
        temp.put("华夏银行","1025");
        temp.put("广发银行","1027");
        temp.put("光大银行","1022");
        temp.put("北京银行","1032");
        temp.put("宁波银行","1056");
        bankMap=temp;
    }

    @Override
    public String getAppID() {
        return app_id;
    }

    @Override
    public String getMchID() {
        return wx_pay_mch_id;
    }
    @Override
    public String getMchAppID() {
    	return wx_pay_mch_app_id;
    }

    @Override
    public String getKey() {
        return wx_pay_key;
    }

    @Override
    public String getSecret() {
        return app_secret;
    }
    @Override
    public String getNotify_url() {
        return notify_url;
    }
    @Override
    public String getPkcs8() {
    	return pkcs8;
    }
    @Override
    public Map<String,String> getBankCode() {
    	return bankMap;
    }
    @Override
    public InputStream getCertStream() {
        return new ByteArrayInputStream(this.certData);
    }

    @Override
    public IWXPayDomain getWXPayDomain() { // 这个方法需要这样实现, 否则无法正常初始化WXPay
        IWXPayDomain iwxPayDomain = new IWXPayDomain() {
            public void report(String domain, long elapsedTimeMillis, Exception ex) {

            }
            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API, true);
            }
        };
        return iwxPayDomain;
    }
}
