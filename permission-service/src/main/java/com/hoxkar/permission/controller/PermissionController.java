package com.hoxkar.permission.controller;

import com.hoxkar.permission.pojo.dto.*;
import com.hoxkar.permission.pojo.vo.*;
import com.hoxkar.permission.service.PermissionService;
import com.hoxkar.common.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import com.hoxkar.common.pojo.dto.PageRequestDTO;
import com.hoxkar.common.pojo.vo.PageResponseVO;
import com.hoxkar.common.util.PageUtils;
import java.util.List;

/**
 * 权限管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限管理相关接口")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    // 角色管理接口
    @PostMapping("/roles")
    @Operation(summary = "创建角色", description = "创建新角色")
    public ApiResponse<RoleVO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("创建角色请求: {}", request.getRoleName());
        return permissionService.createRole(request);
    }
    
    @PutMapping("/roles/{roleId}")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public ApiResponse<RoleVO> updateRole(@PathVariable Long roleId, @Valid @RequestBody UpdateRoleRequest request) {
        log.info("更新角色请求: {}", roleId);
        return permissionService.updateRole(roleId, request);
    }
    
    @DeleteMapping("/roles/{roleId}")
    @Operation(summary = "删除角色", description = "删除指定角色")
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
        log.info("删除角色请求: {}", roleId);
        return permissionService.deleteRole(roleId);
    }
    
    @GetMapping("/roles/{roleId}")
    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色详细信息")
    public ApiResponse<RoleVO> getRoleById(@PathVariable Long roleId) {
        log.info("获取角色详情请求: {}", roleId);
        return permissionService.getRoleById(roleId);
    }
    
    @GetMapping("/roles/code/{roleCode}")
    @Operation(summary = "根据角色代码获取角色", description = "根据角色代码获取角色信息")
    public ApiResponse<RoleVO> getRoleByCode(@PathVariable String roleCode) {
        log.info("根据角色代码获取角色请求: {}", roleCode);
        return permissionService.getRoleByCode(roleCode);
    }
    
    @GetMapping("/roles")
    @Operation(summary = "获取角色列表", description = "获取所有角色列表")
    public ApiResponse<List<RoleVO>> getAllRoles() {
        log.info("获取角色列表请求");
        return permissionService.getAllRoles();
    }
    
   @PostMapping("/roles/page")
    @Operation(summary = "分页获取角色", description = "分页获取角色列表")
    public ApiResponse<PageResponseVO<RoleVO>> getRolesByPage(@Valid @RequestBody PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageUtils.toPageable(pageRequestDTO);
        Page<RoleVO> page = permissionService.getRolesByPage(pageable).getData();
        PageResponseVO<RoleVO> vo = PageUtils.toPageResponseVO(page);
        return ApiResponse.success(vo);
    }
    
    @PostMapping("/page")
    @Operation(summary = "分页获取权限", description = "分页获取权限列表")
    public ApiResponse<PageResponseVO<PermissionVO>> getPermissionsByPage(@Valid @RequestBody PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageUtils.toPageable(pageRequestDTO);
        Page<PermissionVO> page = permissionService.getPermissionsByPage(pageable).getData();
        PageResponseVO<PermissionVO> vo = PageUtils.toPageResponseVO(page);
        return ApiResponse.success(vo);
    }
    
    @GetMapping("/roles/status/{status}")
    @Operation(summary = "根据状态获取角色", description = "根据角色状态获取角色列表")
    public ApiResponse<List<RoleVO>> getRolesByStatus(@PathVariable String status) {
        log.info("根据状态获取角色请求: {}", status);
        return permissionService.getRolesByStatus(status);
    }
    
    @GetMapping("/roles/search")
    @Operation(summary = "搜索角色", description = "根据角色名称搜索角色")
    public ApiResponse<List<RoleVO>> searchRoles(@RequestParam String roleName) {
        log.info("搜索角色请求: {}", roleName);
        return permissionService.searchRoles(roleName);
    }
    
    @PostMapping("/roles/{roleId}/enable")
    @Operation(summary = "启用角色", description = "启用指定角色")
    public ApiResponse<Void> enableRole(@PathVariable Long roleId) {
        log.info("启用角色请求: {}", roleId);
        return permissionService.enableRole(roleId);
    }
    
    @PostMapping("/roles/{roleId}/disable")
    @Operation(summary = "禁用角色", description = "禁用指定角色")
    public ApiResponse<Void> disableRole(@PathVariable Long roleId) {
        log.info("禁用角色请求: {}", roleId);
        return permissionService.disableRole(roleId);
    }
    
    // 权限管理接口
    @PostMapping
    @Operation(summary = "创建权限", description = "创建新权限")
    public ApiResponse<PermissionVO> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        log.info("创建权限请求: {}", request.getPermissionName());
        return permissionService.createPermission(request);
    }
    
    @PutMapping("/{permissionId}")
    @Operation(summary = "更新权限", description = "更新权限信息")
    public ApiResponse<PermissionVO> updatePermission(@PathVariable Long permissionId, @Valid @RequestBody UpdatePermissionRequest request) {
        log.info("更新权限请求: {}", permissionId);
        return permissionService.updatePermission(permissionId, request);
    }
    
    @DeleteMapping("/{permissionId}")
    @Operation(summary = "删除权限", description = "删除指定权限")
    public ApiResponse<Void> deletePermission(@PathVariable Long permissionId) {
        log.info("删除权限请求: {}", permissionId);
        return permissionService.deletePermission(permissionId);
    }
    
    @GetMapping("/{permissionId}")
    @Operation(summary = "获取权限详情", description = "根据权限ID获取权限详细信息")
    public ApiResponse<PermissionVO> getPermissionById(@PathVariable Long permissionId) {
        log.info("获取权限详情请求: {}", permissionId);
        return permissionService.getPermissionById(permissionId);
    }
    
    @GetMapping("/code/{permissionCode}")
    @Operation(summary = "根据权限代码获取权限", description = "根据权限代码获取权限信息")
    public ApiResponse<PermissionVO> getPermissionByCode(@PathVariable String permissionCode) {
        log.info("根据权限代码获取权限请求: {}", permissionCode);
        return permissionService.getPermissionByCode(permissionCode);
    }
    
    @GetMapping
    @Operation(summary = "获取权限列表", description = "获取所有权限列表")
    public ApiResponse<List<PermissionVO>> getAllPermissions() {
        log.info("获取权限列表请求");
        return permissionService.getAllPermissions();
    }

    
    @GetMapping("/type/{permissionType}")
    @Operation(summary = "根据权限类型获取权限", description = "根据权限类型获取权限列表")
    public ApiResponse<List<PermissionVO>> getPermissionsByType(@PathVariable String permissionType) {
        log.info("根据权限类型获取权限请求: {}", permissionType);
        return permissionService.getPermissionsByType(permissionType);
    }
    
    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取权限树结构")
    public ApiResponse<List<PermissionVO>> getPermissionTree() {
        log.info("获取权限树请求");
        return permissionService.getPermissionTree();
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取权限", description = "根据权限状态获取权限列表")
    public ApiResponse<List<PermissionVO>> getPermissionsByStatus(@PathVariable String status) {
        log.info("根据状态获取权限请求: {}", status);
        return permissionService.getPermissionsByStatus(status);
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索权限", description = "根据权限名称搜索权限")
    public ApiResponse<List<PermissionVO>> searchPermissions(@RequestParam String permissionName) {
        log.info("搜索权限请求: {}", permissionName);
        return permissionService.searchPermissions(permissionName);
    }
    
    @PostMapping("/{permissionId}/enable")
    @Operation(summary = "启用权限", description = "启用指定权限")
    public ApiResponse<Void> enablePermission(@PathVariable Long permissionId) {
        log.info("启用权限请求: {}", permissionId);
        return permissionService.enablePermission(permissionId);
    }
    
    @PostMapping("/{permissionId}/disable")
    @Operation(summary = "禁用权限", description = "禁用指定权限")
    public ApiResponse<Void> disablePermission(@PathVariable Long permissionId) {
        log.info("禁用权限请求: {}", permissionId);
        return permissionService.disablePermission(permissionId);
    }
    
    // 角色权限分配接口
    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "为角色分配权限", description = "为指定角色分配权限")
    public ApiResponse<Void> assignPermissionsToRole(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        log.info("为角色分配权限请求: roleId={}, permissionIds={}", roleId, permissionIds);
        return permissionService.assignPermissionsToRole(roleId, permissionIds);
    }
    
    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "获取角色的权限", description = "获取指定角色的权限列表")
    public ApiResponse<List<PermissionVO>> getRolePermissions(@PathVariable Long roleId) {
        log.info("获取角色的权限请求: {}", roleId);
        return permissionService.getRolePermissions(roleId);
    }
    
    @PostMapping("/users/roles")
    @Operation(summary = "为用户分配角色", description = "为指定用户分配角色")
    public ApiResponse<Void> assignRolesToUser(@Valid @RequestBody AssignRoleRequest request) {
        log.info("为用户分配角色: userId={}, roleIds={}", request.getUserId(), request.getRoleIds());
        return permissionService.assignRolesToUser(request);
    }
    
    @GetMapping("/users/{userId}/roles")
    @Operation(summary = "获取用户的角色", description = "获取指定用户的角色列表")
    public ApiResponse<List<RoleVO>> getUserRoles(@PathVariable Long userId) {
        log.info("获取用户的角色请求: {}", userId);
        return permissionService.getUserRoles(userId);
    }
    
    @GetMapping("/users/{userId}/permissions")
    @Operation(summary = "获取用户的权限", description = "获取指定用户的权限列表")
    public ApiResponse<List<PermissionVO>> getUserPermissions(@PathVariable Long userId) {
        log.info("获取用户的权限请求: {}", userId);
        return permissionService.getUserPermissions(userId);
    }
    
    @GetMapping("/users/{userId}/check-permission/{permissionCode}")
    @Operation(summary = "检查用户权限", description = "检查用户是否有指定权限")
    public ApiResponse<Boolean> checkUserPermission(@PathVariable Long userId, @PathVariable String permissionCode) {
        log.info("检查用户权限请求: userId={}, permissionCode={}", userId, permissionCode);
        return permissionService.checkUserPermission(userId, permissionCode);
    }
    
    @GetMapping("/users/{userId}/check-role/{roleCode}")
    @Operation(summary = "检查用户角色", description = "检查用户是否有指定角色")
    public ApiResponse<Boolean> checkUserRole(@PathVariable Long userId, @PathVariable String roleCode) {
        log.info("检查用户角色请求: userId={}, roleCode={}", userId, roleCode);
        return permissionService.checkUserRole(userId, roleCode);
    }
    
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "权限服务健康检查")
    public ApiResponse<String> health() {
        return ApiResponse.success("权限服务运行正常");
    }
} 