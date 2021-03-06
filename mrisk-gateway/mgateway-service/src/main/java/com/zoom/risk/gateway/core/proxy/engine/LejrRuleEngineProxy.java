package com.zoom.risk.gateway.core.proxy.engine;

import com.zoom.risk.gateway.hitrule.framework.HitRuleFramework;
import com.zoom.risk.gateway.core.proxy.RuleEngineProxy;
import com.zoom.risk.gateway.core.service.RiskHelperService;
import com.zoom.risk.gateway.common.utils.GsonUtil;
import com.zoom.risk.gateway.common.utils.Utils;
import com.zoom.risk.gateway.fraud.utils.RiskConstant;
import com.zoom.risk.platform.engine.api.DecisionResponse;
import com.zoom.risk.platform.engine.api.DecisionRule;
import com.zoom.risk.platform.engine.api.RuleEngineApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by jiangyulin on 2015/12/10.
 */
@Service("zoomRuleEngine")
public class LejrRuleEngineProxy implements RuleEngineProxy {
    private static final Logger logger = LogManager.getLogger(LejrRuleEngineProxy.class);
    public static final String TAKINGTIME = "takingTime";

    @Resource(name="ruleEngineApi")
    private RuleEngineApi ruleEngineApi;

    @Resource(name="riskHelperService")
    private RiskHelperService riskHelperService;

    @Resource(name="hitRuleFramework")
    private HitRuleFramework hitRuleFramework;

    @Override
    public Map<String, Object> evaluate(Map<String, Object> riskData) {
        long time = System.currentTimeMillis();
        long ruleEngineTime = 0L;
        Map<String,Object> resultMap = new HashMap<>();
        DecisionResponse decisionResponse = ruleEngineApi.evaluate(riskData);
        ruleEngineTime = System.currentTimeMillis() - time;
        if ( decisionResponse.isOK() ){
            Integer decisionCode = decisionResponse.getHitPolicy().getFinalDecisionCode();
            //返回结果
            resultMap.put(RiskConstant.RESULT_DECISION_CODE, decisionCode);
            //请求参数
            riskData.put(RiskConstant.RESULT_DECISION_CODE, decisionCode +"");
            List<DecisionRule> hitRules = decisionResponse.getHitRules();
            //只有通过时才返回actionCode，返回所有命中规则的actionCode
            Set<String> actionCodeSet = new HashSet<>();
            for (DecisionRule rule : hitRules) {
                String actionCode = rule.getActionCode();
                if (Utils.isNotEmpty(actionCode)) {
                    actionCodeSet.add(actionCode);
                }
            }
            //规则结果处理
            hitRuleFramework.publishEvent(actionCodeSet,riskData);
            resultMap.put(RiskConstant.RESULT_ACTION_CODES, actionCodeSet);
            riskData.put(RiskConstant.RESULT_ACTION_CODES, actionCodeSet);
            riskData.put(RiskConstant.HIT_RULES, riskHelperService.convertHitRules(hitRules));
            riskData.put(TAKINGTIME,decisionResponse.getExtendedValue(TAKINGTIME));
        }
        String jsonInput = GsonUtil.getNoNullGson().toJson(riskData);
        if ( !decisionResponse.isOK() ){
            String errorMessage = "RuleEngineApi happens error, responseCode : " + decisionResponse.getResponseCode() + ", responseDesc : " + decisionResponse.getResponseDesc();
            throw new RuntimeException(errorMessage);
        }
        logger.info("RuleEngineApi takes {} ms, total time {} ms, jsonInput: {}", ruleEngineTime,  System.currentTimeMillis()-time, jsonInput );
        return resultMap;
    }
}
