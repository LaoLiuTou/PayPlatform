package com.payplatform.controller.wxpay;
import static com.payplatform.utils.wxpay.WXPayConstants.USER_AGENT;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.payplatform.utils.PayCommonUtil;
import com.payplatform.utils.wxpay.IWxPayConfig;
import com.payplatform.utils.wxpay.WXPayConstants;
import com.payplatform.utils.wxpay.WXPayUtil;
import com.payplatform.utils.wxpay.WXPayConstants.SignType;
@Controller
public class RSAController {
	 
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Logger logger = Logger.getLogger("PayPlatformLogger");
	 
    
    /**
	  * 获取RSA公钥
	  * @return
	  */
	@RequestMapping("/getRSA")
	@ResponseBody
	public String getRSA() {
		String resp = "";
		try {
			IWxPayConfig iWxPayConfig = new IWxPayConfig();
			String nonceStr = PayCommonUtil.getRandomString(32);
			Map<String, String> params = new HashMap<String, String>();
			params.put("mch_id", iWxPayConfig.getMchID());
			params.put("nonce_str", nonceStr);
			String refundSign = WXPayUtil.generateSignature(params,
					iWxPayConfig.getKey());
			params.put("sign", refundSign);

			// appid和secret是开发者分别是小程序ID和小程序密钥，开发者通过微信公众平台-》设置-》开发设置就可以直接获取，
			String url = "https://fraud.mch.weixin.qq.com/risk/getpublickey";
			String reqBody = WXPayUtil.mapToXml(params);
			resp=requestOnce(url,reqBody,true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("获取RSA公钥失败："+e.getMessage());
		}
		return resp;
	}
	
	private String requestOnce(String urlSuffix,  String data,  boolean useCert) throws Exception {
		IWxPayConfig iWxPayConfig = new IWxPayConfig();
		BasicHttpClientConnectionManager connManager;
        if (useCert) {
            // 证书
            char[] password = iWxPayConfig.getMchID().toCharArray();
            InputStream certStream = iWxPayConfig.getCertStream();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(certStream, password);

            // 实例化密钥库 & 初始化密钥工厂
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);

            // 创建 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1"},
                    null,
                    new DefaultHostnameVerifier());

            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslConnectionSocketFactory)
                            .build(),
                    null,
                    null,
                    null
            );
        }
        else {
            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", SSLConnectionSocketFactory.getSocketFactory())
                            .build(),
                    null,
                    null,
                    null
            );
        }

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        HttpPost httpPost = new HttpPost(urlSuffix);
        RequestConfig requestConfig = RequestConfig.custom().
        		setSocketTimeout(iWxPayConfig.getHttpConnectTimeoutMs()).
        		setConnectTimeout(iWxPayConfig.getHttpReadTimeoutMs()).build();
        httpPost.setConfig(requestConfig);

        StringEntity postEntity = new StringEntity(data, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.addHeader("User-Agent", USER_AGENT + " " + iWxPayConfig.getMchID());
        httpPost.setEntity(postEntity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");

    }
	 
 
}
