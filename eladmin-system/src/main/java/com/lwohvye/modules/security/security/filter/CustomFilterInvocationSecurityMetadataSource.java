package com.lwohvye.modules.security.security.filter;

import com.lwohvye.constant.SecurityConstant;
import com.lwohvye.modules.system.service.IResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

import java.util.*;

/**
 * @author Hongyan Wang
 * @description 要实现动态配置权限，首先自定义一个类实现FilterInvocationSecurityMetadataSource接口，Spring Security通过接口中的getAttributes方法来确定请求需要哪些角色
 * <a href="https://docs.spring.io/spring-security/site/docs/4.2.4.RELEASE/reference/htmlsingle/#appendix-faq-dynamic-url-metadata">The first thing you should ask yourself is if you really need to do this.</a>
 * @date 2021/11/27 2:45 下午
 */
@RequiredArgsConstructor
public class CustomFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    AntPathMatcher antPathMatcher = new AntPathMatcher();  //用来实现ant风格的Url匹配

    private final IResourceService resourceService;

    /**
     * getAttributes 方法确定一个请求需要哪些角色
     *
     * @param object 是FilterInvocation对象，可以获取当前请求的Url
     * @return Collection<ConfigAttribute> 当前请求Url所需要的角色
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {

        var fi = (FilterInvocation) object;
        var url = fi.getRequestUrl(); // 获取当前请求的Url
        var httpMethod = fi.getRequest().getMethod(); // 请求方法GET、POST、PUT、DELETE
        List<ConfigAttribute> attributes;
        // Lookup your database (or other source) using this information and populate the
        // list of attributes
        var securityConfigs = resourceService.queryAllRes().stream() //获取数据库中的所有资源信息，即本案例中的resource以及对应的role
                .filter(resource -> antPathMatcher.match(resource.getPattern(), url) // URI匹配
                                    && !resource.getRoleCodes().isEmpty() // 有关联角色（需要特定角色权限）
                                    && (Objects.isNull(resource.getReqMethod()) || Objects.equals(resource.getReqMethod(), httpMethod))) // 请求方法类型匹配。资源未配置请求方法视为全部
                .flatMap(resource -> resource.getRoleCodes().stream()) // 将字符状态的角色名用逗号切开
                .distinct() // 排重
                .map(role -> new SecurityConfig("ROLE_" + role.trim())).toList();
        if (!securityConfigs.isEmpty())
            attributes = new ArrayList<>(securityConfigs); // 构建返回
        else attributes = Collections.singletonList(new SecurityConfig(SecurityConstant.ROLE_LOGIN)); //如果请求Url在资源表中不存在相应的模式，则该请求登陆后即可访问
        return attributes;
    }

    /**
     * @return 返回所有定义好的权限资源，Spring Security启动时会校验相关配置是否正确，如果不需要校验，直接返回null即可
     */
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    /**
     * 返回类对象是否支持校验
     *
     * @param clazz
     * @return
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
