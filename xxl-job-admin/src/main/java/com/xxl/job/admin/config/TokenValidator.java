package com.xxl.job.admin.config;

import com.xxl.job.admin.controller.JobInfoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.xxl.job.admin.utils.RequestHeaderCheckUtils.checkRequestHeaderAccess;
import static com.xxl.job.admin.utils.RequestHeaderCheckUtils.checkRequestHeaderContainTokenKey;

@Component
public class TokenValidator {

    private static final Map<String, String> TOKEN_MAP = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(TokenValidator.class);

    public TokenValidator(
            @Value("${apn.access_token.key}") String apnAccessTokenKey,
            @Value("${apn.access_token.value}") String apnAccessTokenValue,
            @Value("${crm.access_token.key}") String crmAccessTokenKey,
            @Value("${crm.access_token.value}") String crmAccessTokenValue,
            @Value("${uoffer.access_token.key}") String uofferAccessTokenKey,
            @Value("${uoffer.access_token.value}") String uofferAccessTokenValue,
            @Value("${hr.access_token.key}") String hrAccessTokenKey,
            @Value("${hr.access_token.value}") String hrAccessTokenValue) {
        TOKEN_MAP.put(apnAccessTokenKey, apnAccessTokenValue);
        TOKEN_MAP.put(crmAccessTokenKey, crmAccessTokenValue);
        TOKEN_MAP.put(uofferAccessTokenKey, uofferAccessTokenValue);
        TOKEN_MAP.put(hrAccessTokenKey, hrAccessTokenValue);
    }

    public boolean validateTokens(HttpServletRequest request) {
        // 先检查请求头中是否包含所有令牌键
        if (!checkRequestHeaderContainTokenKey(request, TOKEN_MAP.keySet().toArray(new String[0]))) {
            logger.info("access token doesn't exist!");
            return false;
        }
        // 再检查每个令牌键对应的值是否匹配
        for (Map.Entry<String, String> entry : TOKEN_MAP.entrySet()) {
            String tokenKey = entry.getKey();
            String expectedTokenValue = entry.getValue();
            if (!checkRequestHeaderAccess(request, tokenKey, expectedTokenValue)) {
                logger.info("access token is wrong!");
                return false;
            }
        }
        return true;
    }

}