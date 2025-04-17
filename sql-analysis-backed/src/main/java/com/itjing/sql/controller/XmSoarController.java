package com.itjing.sql.controller;

import cn.hutool.core.util.StrUtil;
import com.itjing.sql.response.AjaxResult;
import com.itjing.sql.service.XmSoarWebService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * 小米Soar相关控制器
 *
 * @author lijing
 * @date 2025-04-16
 */
@RestController
@Slf4j
@RequestMapping("/xm/soar")
public class XmSoarController {

    @Resource
    private XmSoarWebService xmSoarWebService;

    @Value("${soar.web.port:3000}")
    private int soarWebPort;

    private final String SOAR_BASE_URL = "http://localhost:";

    @GetMapping("/health")
    public AjaxResult<?> checkHealth() {
        try {
            URL url = new URL(SOAR_BASE_URL + soarWebPort + "/webui");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            return AjaxResult.success(responseCode);
        } catch (Exception e) {
            log.error("检查服务健康状态失败 {}", e.getMessage());
            throw new RuntimeException(StrUtil.format("检查服务健康状态失败 {}", e.getMessage()));
        }
    }

}
