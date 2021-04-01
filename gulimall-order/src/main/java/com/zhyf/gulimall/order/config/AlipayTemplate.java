package com.zhyf.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zhyf.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000117630699";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCIEpO9Y/zmixWsvTBu+54vy7etbkmRnIlPVcEOaNQYr/zH4m8lwMNHIkVXAtxTZJ4ikwP5/YmqAL+Gzdu/XDRma1+vbxUWNQaWq/sjBg00ZYoWweGCEDUCL6tlyYCubdyfpJTWjb6Hol5IYBSbLFOQJgRBYP/RWmQNPoVNpqF50i2l3ByXTjmU4mANadO6cPbqRcM+AIou0DD2R6KFJl6L9XyuRvCg+CP8jahqj8Nv95kOwapZ6z8DCaB6kGAxyX2uLHavnQ/GTotqnOcLoH8TCf4iLI5oRoDCYM0F2flll6RNqlAsRhaIZJrFO9KiXWoYSR2Za2gWs/Pwkww6ZPozAgMBAAECggEAWObaQ8jJ+M4yMRPCSyB174BH1zXRNyNwyraaVZHD0QVaQDX+rEnOy/DmIL0rughpEOnAlVpC0SGAANEKm3HggnHwqCwJOOMYFOT0RaO0iOE2YTv23AU+vNjsQKpwOoZouJzM6YjeTLuWoe60R9WkGu/bo+N+AAfwin6olV0i7vWfEWFQKzFTjW4E8kbpEeIVKzS/hjWya+IgyVhMhvZ98WmBggdWxb4C/FQR8tesqKfDHv3TcQ1alVGOhW0W4jMkt7yBSzTR3z9hxCVDU5GdEcwWCCeB0vlUxH7G1Jpie6XvLCP+o3Bfk/24amLRzaftq68+B8p6VfsR+g+W0embkQKBgQDUfOc0+SpDOdj9bmjLv0WAANvpHSkjvfx2yO7bTvtliGmLM804T6JTNIw18KuCA7JiypyMhvY4TgyrA3YOSAYVt59Sqd5Vx14E7g+2hUz80j2vNaENygsoFHlT038p+yFi0kCPrc0tjBteMRnwAG65o5vnlg7Bb+pgHzZQcT9hPQKBgQCj788fGRFHteSZ+JRE1BwCTj2IapaocvnkUZ6fim1IuUT4MH0ilA3KZ+Vty5aO4djIKw1j2rmNJGJZI0hOl9qJ3prcoyp+bDly19FQqWd2Uq4CQCGYHKP6EmkS5CszWGErbo08xcsTmaPiekK+XMJj9BRJf3FZS434Fl9AVqygLwKBgQDFOFjwlgNf+iUotH2X/qPnNuTl7SbuAbx+E+l+rEQx7PbpON9VULR9PdyVhBfryLqdbyj2VZ3ajLa9U60TfE4JgjDibL/sJeth8AOtiPP4BLs0EDa3sbvBK5+g6kCxGmHWcwdxVAOILu6H8lL8q6oZq31c6a4wZEvJg67J4xLMkQKBgGuG3cm9/XGVws2jGE1U+tIgU4foscznLTUuu+ZVO30Z2D0aQNmvVqROJVQw1x380N3qLywxyMbk+IUS+VhjjfokQls5wqONhNjo6egIVU5syKm9Osu07XEXJkg31IYnEAUWzBjye1Lt0Wq5WpHV8cLx02Sp7MTvI713nx5qO2wNAoGBAISIVEhr3FxHnaSX6BnUMMlrXkzth5DwKsZvqF4jghkH8/agQx4/nDN5E5ep3SZhHGHharsm94gXrEMO0l+0NMcEFB3N9Rzna0+tP8aIPIRClFySMZegg76HfLxdH+XsWWhwPgPx7KtF4xMx0wxHRVbDjRSx2nWRgAXs27PPGo21";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkPKG94Vow2Ix99J2iWOLqkKFhMnQqULNOhlNMCWhAc0E5A7JGOwwHsmwIA6XAZDacCnyw76SKTB4MnE9M6On3V5lG8JOnlNLElSCEDc03bo8cYfvyOFuNBzFWQe5JTn/5o5AC6ha2pG1qk71/JVZsElclv8NyE9VtnA7REX373P9yTXoOxDJwd04KQYBn0s9m6nk7tA+jrMUxQaKEyHsy7plmd1kmhAFk0abyLZJp4fA5oSJ6dEYJ81YVpfaMyB5G32HyyT6WHD+2zzGJHu9H/mKmcErhuZZh0T/kG+Rfx8zOhBitP38kvMk+G5p2IDs0xNa2MZ14W1qV86x5hLOswIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://ncfd5img18.52http.net/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"1m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }
}
