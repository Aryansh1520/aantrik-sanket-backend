package com.aantriksanket.backend.util.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);
    private final PermissionChecker permissionChecker;

    public PermissionAspect(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            logger.error("PermissionAspect: RequestContextHolder attributes are null");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpServletRequest request = attributes.getRequest();
        String httpMethod = request.getMethod();
        String apiCategory = requiresPermission.category();

        logger.debug("PermissionAspect: Checking permission for category={}, method={}, endpoint={}",
                apiCategory, httpMethod, request.getRequestURI());

        // OPTIONS is always allowed
        if ("OPTIONS".equalsIgnoreCase(httpMethod)) {
            logger.debug("PermissionAspect: OPTIONS request, allowing");
            return joinPoint.proceed();
        }

        // Check permission based on HTTP method (GET -> read, POST -> create, PUT/PATCH -> update, DELETE -> delete)
        boolean hasPermission = permissionChecker.hasPermission(apiCategory, httpMethod);
        logger.debug("PermissionAspect: Permission check result={} for category={}, method={}",
                hasPermission, apiCategory, httpMethod);

        if (!hasPermission) {
            logger.warn("PermissionAspect: Access denied for category={}, method={}, endpoint={}",
                    apiCategory, httpMethod, request.getRequestURI());
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", "Insufficient permissions");
            errorData.put("message", "You do not have permission to perform this action");
            errorData.put("category", apiCategory);
            errorData.put("method", httpMethod);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.aantriksanket.backend.util.ApiResponse(false, errorData));
        }

        logger.debug("PermissionAspect: Permission granted, proceeding");
        return joinPoint.proceed();
    }
}
