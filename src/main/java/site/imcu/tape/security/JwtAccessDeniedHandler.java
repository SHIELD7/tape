package site.imcu.tape.security;

import com.alibaba.fastjson.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import site.imcu.tape.pojo.ResponseData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author mengh
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

   @Override
   public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
      ResponseData responseData = ResponseData.builder().message(accessDeniedException.getMessage()).code(-10).build();
      response.getWriter().write(JSONObject.toJSONString(responseData));
      response.setContentType("application/json;charset=UTF-8");

   }
}
