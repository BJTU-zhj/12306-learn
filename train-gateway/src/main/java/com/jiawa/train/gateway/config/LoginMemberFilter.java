package com.jiawa.train.gateway.config;


import com.jiawa.train.gateway.util.JwtUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoginMemberFilter implements GlobalFilter, Ordered {

    private static final Logger LOG= LoggerFactory.getLogger(LoginMemberFilter.class);

    @Resource
    private JwtUtil jwtUtil;

//exchange 包含了当前 HTTP 请求的所有信息。chain 代表过滤器链。
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path=exchange.getRequest().getURI().getPath();
        //过滤不需要验证jwt的地址
        if(path.contains("admin")
        || path.contains("hello")
        || path.contains("member/member/send-code")
        || path.contains("member/member/login")){
            LOG.info("不需要验证JWT，{}", path);
            return chain.filter(exchange);
        }else{
            LOG.info("需要验证JWT，{}", path);
            //验证JWT
            String token = exchange.getRequest().getHeaders().getFirst("token");
            LOG.info("开始验证jwt,token:{}", token);
            if(token==null||token.isEmpty() ){
                LOG.info("jwt验证失败，token为空");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }else {
                boolean verifyResult=jwtUtil.verify(token);
                if(verifyResult){
                    LOG.info("jwt验证成功");
                    return chain.filter(exchange);
                }else{
                    LOG.info("jwt验证失败,token无效");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
