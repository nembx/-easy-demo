package com.example.pay_demo.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lian
 */

@RestController
@Slf4j
@RequestMapping("/order")
public class PayController {

    @Resource
    private Config config;


    @GetMapping("/pay")
    public String pay() throws Exception {
        Factory.setOptions(config);
        AlipayTradePagePayResponse pay = Factory.Payment.Page().pay("mac笔记本", "111262626", "19999", "http://localhost:8080/order/notify");
        return pay.getBody();
    }

    @GetMapping("/qrPay")
    public void pay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Factory.setOptions(config);
        // 调用支付宝的接口
        AlipayTradePrecreateResponse payResponse = Factory.Payment.FaceToFace().preCreate("mac笔记本", "262626", "19999");
        String httpBody = payResponse.getHttpBody();
        JSONObject entries = JSONUtil.parseObj(httpBody);
        String qrUrl = entries.getJSONObject("alipay_trade_precreate_response").get("qr_code").toString();
        QrCodeUtil.generate(qrUrl, 300, 300, "png", response.getOutputStream());
    }


    @PostMapping("/notify")
    public void notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = new HashMap<>();
        //获取支付宝POST过来反馈信息，将异步通知中收到的待验证所有参数都存放到map中
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String name : parameterMap.keySet()) {
            String[] values = parameterMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        //验签
        Boolean signResult = Factory.Payment.Common().verifyNotify(params);
        if (signResult) {
            log.info("收到支付宝发送的支付结果通知");
            String out_trade_no = request.getParameter("out_trade_no");
            log.info("交易流水号：{}", out_trade_no);
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
            //交易成功
            switch (trade_status) {
                case "TRADE_SUCCESS":
                    //支付成功的业务逻辑，比如落库，开vip权限等
                    log.info("订单：{} 交易成功", out_trade_no);
                    break;
                case "TRADE_FINISHED":
                    log.info("交易结束，不可退款");
                    //其余业务逻辑
                    break;
                case "TRADE_CLOSED":
                    log.info("超时未支付，交易已关闭，或支付完成后全额退款");
                    //其余业务逻辑
                    break;
                case "WAIT_BUYER_PAY":
                    log.info("交易创建，等待买家付款");
                    //其余业务逻辑
                    break;
            }
            response.getWriter().write("success");   //返回success给支付宝，表示消息我已收到，不用重调

        } else {
            response.getWriter().write("fail");   ///返回fail给支付宝，表示消息我没收到，请重试
        }
    }


    @GetMapping("/query")
    public String query() throws Exception {
        Factory.setOptions(config);
        AlipayTradeQueryResponse result = Factory.Payment.Common().query("262626");
        return result.getHttpBody();
    }

    @GetMapping("/refund")
    public String refund() throws Exception {
        Factory.setOptions(config);
        AlipayTradeRefundResponse refundResponse = Factory.Payment.Common().refund("262626", "19999");
        return refundResponse.getHttpBody();
    }
}