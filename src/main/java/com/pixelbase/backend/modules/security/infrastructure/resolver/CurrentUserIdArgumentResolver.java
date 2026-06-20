package com.pixelbase.backend.modules.security.infrastructure.resolver;

import com.pixelbase.backend.common.security.annotation.CurrentUserId;
import com.pixelbase.backend.modules.security.domain.UserDetailsImpl;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // El resolver solo actúa si el parámetro tiene nuestra anotación y es de tipo Long
        return parameter.hasParameterAnnotation(CurrentUserId.class)
            && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            return null; // Caso GUEST (Invitado): Retorna null de forma limpia
        }

        // Caso CUSTOMER: Al estar dentro del módulo security, el casting es 100% legal
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return principal.user().id();
    }
}
