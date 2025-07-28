package com.hoxkar.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtTokenUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    
    /**
     * 生成JWT Token
     */
    public String generateToken(UserDetails userDetails, String tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId);
        return createToken(claims, userDetails.getUsername(), expiration);
    }
    
    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(UserDetails userDetails, String tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId);
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }
    
    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * 从Token中提取租户ID
     */
    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", String.class));
    }
    
    /**
     * 从Token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * 从Token中提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从Token中提取所有声明
     */
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查Token是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Token过期检查失败: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * 验证Token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查是否为刷新Token
     */
    public Boolean isRefreshToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "refresh".equals(type);
        } catch (Exception e) {
            log.warn("Token类型检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 解析Token（不验证签名，用于调试）
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 验证Token（简化版本，不需要UserDetails）
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查Token是否在黑名单中
     */
    public Boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("检查Token黑名单失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 将Token加入黑名单
     */
    public void addToBlacklist(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            Date expiration = extractExpiration(token);
            long ttl = Math.max(0, expiration.getTime() - System.currentTimeMillis());
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttl, TimeUnit.MILLISECONDS);
                log.debug("Token已加入黑名单，TTL: {}ms", ttl);
            }
        } catch (Exception e) {
            log.error("将Token加入黑名单失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从黑名单中移除Token
     */
    public void removeFromBlacklist(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.delete(blacklistKey);
            log.debug("Token已从黑名单中移除");
        } catch (Exception e) {
            log.error("从黑名单中移除Token失败: {}", e.getMessage());
        }
    }
    
    /**
     * 存储刷新Token到Redis
     */
    public void storeRefreshToken(String username, String refreshToken) {
        try {
            String refreshKey = REFRESH_TOKEN_PREFIX + username;
            Date expiration = extractExpiration(refreshToken);
            long ttl = Math.max(0, expiration.getTime() - System.currentTimeMillis());
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set(refreshKey, refreshToken, ttl, TimeUnit.MILLISECONDS);
                log.debug("刷新Token已存储，TTL: {}ms", ttl);
            }
        } catch (Exception e) {
            log.error("存储刷新Token失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从Redis获取刷新Token
     */
    public String getRefreshToken(String username) {
        try {
            String refreshKey = REFRESH_TOKEN_PREFIX + username;
            Object value = redisTemplate.opsForValue().get(refreshKey);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("获取刷新Token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 删除刷新Token
     */
    public void deleteRefreshToken(String username) {
        try {
            String refreshKey = REFRESH_TOKEN_PREFIX + username;
            redisTemplate.delete(refreshKey);
            log.debug("刷新Token已删除");
        } catch (Exception e) {
            log.error("删除刷新Token失败: {}", e.getMessage());
        }
    }
    
    /**
     * 验证刷新Token
     */
    public Boolean validateRefreshToken(String username, String refreshToken) {
        try {
            String storedToken = getRefreshToken(username);
            if (storedToken == null) {
                return false;
            }
            
            // 检查存储的Token是否匹配
            if (!storedToken.equals(refreshToken)) {
                return false;
            }
            
            // 检查Token是否过期
            return !isTokenExpired(refreshToken);
        } catch (Exception e) {
            log.error("验证刷新Token失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从Token中获取租户ID（别名方法）
     */
    public String getTenantIdFromToken(String token) {
        return extractTenantId(token);
    }
    
    /**
     * 从Token中获取用户ID（从subject中提取）
     */
    public String getUserIdFromToken(String token) {
        return extractUsername(token);
    }
    
    /**
     * 从Token中获取用户名（别名方法）
     */
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }
    
    /**
     * 获取Token剩余有效时间（毫秒）
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("获取Token剩余时间失败: {}", e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 清理过期的黑名单Token
     */
    public void cleanupExpiredBlacklistTokens() {
        try {
            // Redis会自动清理过期的key，这里可以添加额外的清理逻辑
            log.debug("黑名单Token清理完成");
        } catch (Exception e) {
            log.error("清理过期黑名单Token失败: {}", e.getMessage());
        }
    }
} 