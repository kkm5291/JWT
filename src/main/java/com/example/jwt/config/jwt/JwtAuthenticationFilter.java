package com.example.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.jwt.config.oauth.PrincipalDetails;
import com.example.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;


// 스프링 시큐리티에서 UsernamePasswordAuthenticationFilter 가 있음.
// /login이라고 요청하면 Username, password 전송하면 (post로)
// 이 필터가 동작을 함
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    
    //login 요청을 하면 로그인 시도를 위해서 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter : 로그인 시도 중");

        // 1. username, password 받아서
        try {
//            BufferedReader br = request.getReader();
//
//            String input = null;
//            while ((input = br.readLine()) != null) {
//                System.out.println(input);
//            }
            ObjectMapper om = new ObjectMapper();
            User user = om.readValue(request.getInputStream(), User.class);
            System.out.println(user);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

            //principalDetailsService의 loadUserByUsername() 함수가 실행된 후 정상이면 authentication이 리턴됨.
            // DB에 있는 username과 password가 일치한다.
            Authentication authentication =
                    authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            System.out.println("로그인 완료 됨 : " + principalDetails.getUser().getUsername()); // 로그인 정상적으로 됐다는 것
            // authentication 객체가 session 영역에 저장을 해야하고 그 방법이 return 해주면 됨.
            // 리턴의 이유는 권한 관리를 security가 대신 해주기 때문에 편하려고 하는 것.
            // 굳이 JWT 토큰을 사용하면서 세션을 만들 이유가 없음.
            // 단지 권한 처리 때문에 session에 넣어줌

            //JWT 토큰을 만들어줄 것임.
            return authentication;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. 정상인지 로그인 시도를 해보는 것. authenticationManager로 로그인 시도를 하면!! PrincipalDetailsService가 호출이 됨
        //    loadUserByUsername() 함수가 실행이 됨

        // 3. PrincipalDetails를 세션에 담고 (권한 관리를 위해서 진행하는 것)

        // 4. JWT토큰을 만들어서 응답해주면 됨.

        return null;
    }

    // attemptAuthentication 실행 후 인증이 정상적으로 되었으면 successfulAuthentication 함수가 실행 됨
    // JWT 토큰을 만들어서 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication 실행 됨 : 인증이 완료됨");
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        //RSA방식은 아니구 Hash 암호방식
        String jwtToken = JWT.create()
                .withSubject("cos토큰")
                .withExpiresAt(new Date(System.currentTimeMillis() + (60000) * 10))
                .withClaim("id", principalDetails.getUser().getId()) // withClaim은 비공개 claim으로 내가 넣고싶은 키 밸류값 넣으면 됨
                .withClaim("username", principalDetails.getUser().getUsername())
                .sign(Algorithm.HMAC512("cos"));

        response.addHeader("Authorization", "Bearer "+jwtToken);
    }
}
