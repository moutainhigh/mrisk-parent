package com.zoom.risk.platform.thirdparty.tencent.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaCreditScoreBriefGetRequest;
import com.alipay.api.response.ZhimaCreditScoreBriefGetResponse;
import com.zoom.risk.platform.thirdparty.common.utils.ThirdPartyConstants;
import com.zoom.risk.platform.thirdparty.tencent.service.TencentCreditEntryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangyulin
 *Oct 26, 2015
 */
@Service("tencentCreditEntryService")
public class TencentCreditEntryServiceImpl implements TencentCreditEntryService {
    private static final Logger logger = LogManager.getLogger(TencentCreditEntryServiceImpl.class);

    public Map<String, Object> invoke(String idCardNumber, String userName, Integer admittanceScore) {
        DefaultAlipayClient alipayClient = new DefaultAlipayClient(serverUrl, APP_ID, APP_PRIVATE_KEY, JSON, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);

        ZhimaCreditScoreBriefGetRequest request = new ZhimaCreditScoreBriefGetRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("transaction_id", System.currentTimeMillis()+"");
        map.put("product_code", PRODUCT_CODE);
        map.put("cert_type", CERT_TYPE);
        map.put("cert_no", idCardNumber);
        map.put("name", userName);
        map.put("admittance_score", admittanceScore);
        String bizContent = JSONObject.toJSONString(map);
        logger.info("Request for ZhiMa credit score， bizContent: {}", bizContent);
        request.setBizContent(bizContent);
        ZhimaCreditScoreBriefGetResponse response = null;
        Map<String, Object> resultMap = new HashMap<>();
        String result = "";
        try {
            response = alipayClient.execute(request);
            if ( response.isSuccess() ) {
                result = response.getIsAdmittance();
            }
        } catch (AlipayApiException e) {
            result =  "";
            logger.error("", e);
        }
        resultMap.put(ThirdPartyConstants.QUOTA_KEY,result);
        return resultMap;
    }
}
