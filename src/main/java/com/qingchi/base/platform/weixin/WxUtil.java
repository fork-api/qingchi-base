package com.qingchi.base.platform.weixin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingchi.base.constant.PlatformType;
import com.qingchi.base.constant.WxErrCode;
import com.qingchi.base.model.notify.NotifyDO;
import com.qingchi.base.platform.PushMessageUtils;
import com.qingchi.base.platform.PushMsgDTO;
import com.qingchi.base.platform.TokenDTO;
import com.qingchi.base.platform.qq.QQPayResult;
import com.qingchi.base.model.account.AccountDO;
import com.qingchi.base.repository.user.AccountRepository;
import com.qingchi.base.utils.QingLogger;
import com.qingchi.base.utils.TokenUtils;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author qinkaiyuan
 * @date 2019-10-24 13:30
 */
@Component
public class WxUtil {
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;
    private static AccountRepository accountRepository;

    @Resource
    public void setObjectMapper(ObjectMapper objectMapper) {
        WxUtil.objectMapper = objectMapper;
    }

    @Resource
    public void setAccountRepository(AccountRepository accountRepository) {
        WxUtil.accountRepository = accountRepository;
    }

    @Resource
    public void setRestTemplate(RestTemplate restTemplate) {
        WxUtil.restTemplate = restTemplate;
    }

    private static String tokenUrl;

    @Value("${config.tokenUrl}")
    public void setTokenUrl(String tokenUrl) {
        WxUtil.tokenUrl = tokenUrl;
    }


    /**
     * 获取微信token
     *
     * @return
     */
    public static String getAccessToken() {
        ResponseEntity<TokenDTO> responseEntity = restTemplate.getForEntity(tokenUrl + "getWxSession", TokenDTO.class);
        return Objects.requireNonNull(responseEntity.getBody()).getAccessToken();
    }

    public static String refreshAccessToken() {
        ResponseEntity<TokenDTO> responseEntity = restTemplate.getForEntity(tokenUrl + "refreshWxSession", TokenDTO.class);
        return Objects.requireNonNull(responseEntity.getBody()).getAccessToken();
    }

    /**
     * 校验内容是否违规
     *
     * @param content
     */
    public static HttpResult checkContentWxSec(String content) {
        HttpResult result = checkContentWxSecPost(content);
        assert result != null;
        if (result.hasError()) {
            if (WxErrCode.token_invalid.equals(result.getErrcode())) {
                WxUtil.refreshAccessToken();
                result = checkContentWxSecPost(content);
            }
        }
        return result;
    }

    private static HttpResult checkContentWxSecPost(String content) {
        HashMap<String, Object> postData = new HashMap<>();
        postData.put("content", content);
        String url = WxConst.wx_msg_sec_check_url + WxUtil.getAccessToken();
        return restTemplate.postForEntity(url, postData, HttpResult.class).getBody();
    }


    public static void wxPushMsgCommon(String openId, String platform, PushMsgDTO pushMsgDTO, NotifyDO notify) {
        String accessToken = WxUtil.getAccessToken();
        pushMsgDTO.setAccess_token(accessToken);
        pushMsgDTO.setTouser(openId);
        //如果评论
        String url = WxConst.push_msg_url + accessToken;
        HttpResult result = restTemplate.postForEntity(url, pushMsgDTO, HttpResult.class).getBody();
        PushMessageUtils.savePushMsg(notify, pushMsgDTO, result, platform);
    }

    private static String wx_mp_id;
    private static String wx_app_id;

    @Value("${config.wx.mp.wx_mp_id}")
    public void setWx_mp_id(String wx_mp_id) {
        WxUtil.wx_mp_id = wx_mp_id;
    }

    @Value("${config.wx.app.wx_app_id}")
    public void setWx_app_id(String wx_app_id) {
        WxUtil.wx_app_id = wx_app_id;
    }

    private static String wx_merchant_id;
    private static String wx_merchant_key;

    @Value("${config.wx.merchant.wx_merchant_id}")
    public void setWx_merchant_id(String wx_merchant_id) {
        WxUtil.wx_merchant_id = wx_merchant_id;
    }

    @Value("${config.wx.merchant.wx_merchant_key}")
    public void setWx_merchant_key(String wx_merchant_key) {
        WxUtil.wx_merchant_key = wx_merchant_key;
    }

    //发起支付
    public static String postPayUrl(String platform, String deviceIp, String orderNo, String total_feeStr, Integer userId) throws IOException {
        Optional<AccountDO> accountDOOptional = accountRepository.findOneByUserId(userId);
        AccountDO accountDO = accountDOOptional.get();

        String trade_type = WxConst.mp_pay_trade_type;
        String appId = wx_mp_id;

        Map<String, String> map = new HashMap<>();

        //不为小程序改为app
        if (!PlatformType.mp.equals(platform)) {
            trade_type = WxConst.app_pay_trade_type;
            appId = wx_app_id;
        }
        map.put("appid", appId);
        //指定微信
        String openId = accountDO.getWxMpOpenId();
        //只有为小程序才有这行
        if (PlatformType.mp.equals(platform)) {
            map.put("openid", openId);
        }
        String bodystr = "qingchiapp";
        map.put("body", bodystr);
        map.put("mch_id", wx_merchant_id);
        String nonce_strstr = TokenUtils.getUUID();
        map.put("nonce_str", nonce_strstr);
        map.put("notify_url", WxConst.wx_pay_result_notify_url);

        map.put("out_trade_no", orderNo);
        map.put("spbill_create_ip", deviceIp);
        //10元
        map.put("total_fee", total_feeStr);
        map.put("trade_type", trade_type);

        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.setContentType(MediaType.APPLICATION_XML);
        StringBuilder xmlString = new StringBuilder();

        String appIdStr = "<appid>" + appId + "</appid>";
        String openIdXml = "";
        if (PlatformType.mp.equals(platform)) {
            openIdXml = "<openid>" + openId + "</openid>";
        }
        String body = "<body>" + bodystr + "</body>";
        String mch_id = "<mch_id>" + wx_merchant_id + "</mch_id>";
        String nonce_str = "<nonce_str>" + nonce_strstr + "</nonce_str>";

        String sign = getSignToken(map);
        String signStr = "<sign>" + sign + "</sign>";

        String notify = "<notify_url>" + WxConst.wx_pay_result_notify_url + "</notify_url>";
        String out_trade_no_xml = "<out_trade_no>" + orderNo + "</out_trade_no>";
        String spbill_create_ip = "<spbill_create_ip>" + deviceIp + "</spbill_create_ip>";
        String total_fee = "<total_fee>" + total_feeStr + "</total_fee>";
        String trade_typeStr = "<trade_type>" + trade_type + "</trade_type>";

        xmlString.append("<xml>")
                .append(appIdStr);
        //只有小程序才有openid
        if (PlatformType.mp.equals(platform)) {
            xmlString.append(openIdXml);
        }
        xmlString.append(body)
                .append(mch_id)
                .append(nonce_str)
                .append(notify)
                .append(out_trade_no_xml)
                .append(spbill_create_ip)
                .append(total_fee)
                .append(trade_typeStr)
                .append(signStr)
                .append("</xml>");

        // 创建 HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(xmlString.toString(), requestHeader);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(WxConst.wx_pay_url, requestEntity, String.class);
        String xmlStr = responseEntity.getBody();
        XStream xstream = new XStream();
        xstream.alias("xml", QQPayResult.class);
        Object qqPayResult = xstream.fromXML(xmlStr);
        String result = objectMapper.writeValueAsString(qqPayResult);
        QingLogger.logger.info(result);
        QQPayResult result1 = objectMapper.readValue(result, QQPayResult.class);

        return result1.getPrepay_id();
    }

    //前端使用
    /*public static String getFrontPaySign(Map<String, String> map) {
        map.put("appId", WxConst.mp_app_id);
        map.put("signType", "MD5");
        return getSignToken(map);
    }*/

    public static String getFrontPaySign(String platform, String prepayid, String packageStr, String nonceStr, String timeStamp) {
        Map<String, String> map = new HashMap<>();
        if (PlatformType.mp.equals(platform)) {
            map.put("appId", wx_mp_id);
            map.put("signType", "MD5");
            map.put("timeStamp", timeStamp);
            map.put("nonceStr", nonceStr);
        } else {
            map.put("appid", wx_app_id);
            map.put("partnerid", wx_merchant_id);
            map.put("prepayid", prepayid);
            map.put("noncestr", nonceStr);
            map.put("timestamp", timeStamp);
        }
        map.put("package", packageStr);
        return getSignToken(map);
    }

    /**
     * 生成签名
     *
     * @param map
     * @return
     */
    public static String getSignToken(Map<String, String> map) {
        List<Map.Entry<String, String>> infoIds = new ArrayList<>(map.entrySet());
        // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
        infoIds.sort(Map.Entry.comparingByKey());
        // 构造签名键值对的格式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> item : infoIds) {
            String itemKey = item.getKey();
            String itemVal = item.getValue();
            if (StringUtils.isNotEmpty(itemKey) && StringUtils.isNotEmpty(itemVal)) {
                sb.append(itemKey).append("=").append(itemVal).append("&");
            }
        }
        //key为密钥
        String result = sb.toString() + "key=" + wx_merchant_key;
        //进行MD5加密
        result = DigestUtils.md5Hex(result).toUpperCase();
        return result;
    }
}
