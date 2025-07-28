-- Multi-tenant RBAC System Database Initialization Script
-- Database: rbac_system
-- User: rbac_user
-- Password: rbac123456

-- Create tenant table
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(50) UNIQUE NOT NULL,
    tenant_name VARCHAR(100) NOT NULL,
    description TEXT,
    contact_person VARCHAR(50),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    address TEXT,
    domain VARCHAR(100),
    logo_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    max_users INTEGER DEFAULT 100,
    current_users INTEGER DEFAULT 0,
    subscription_plan VARCHAR(20) DEFAULT 'BASIC' CHECK (subscription_plan IN ('BASIC', 'PREMIUM', 'ENTERPRISE')),
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create tenant config table
CREATE TABLE IF NOT EXISTS tenant_configs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(20) DEFAULT 'STRING' CHECK (config_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, config_key)
);

-- Create user table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    avatar_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    is_super_admin BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, username),
    UNIQUE(tenant_id, email)
);

-- Create role table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, role_code)
);

-- Create permission table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    permission_type VARCHAR(20) DEFAULT 'MENU' CHECK (permission_type IN ('MENU', 'BUTTON', 'API')),
    parent_id BIGINT,
    path VARCHAR(255),
    component VARCHAR(255),
    icon VARCHAR(100),
    sort_order INTEGER DEFAULT 0,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, permission_code)
);

-- Create user role association table
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_id)
);

-- Create role permission association table
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE(role_id, permission_id)
);

-- Create indexes
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_roles_tenant_id ON roles(tenant_id);
CREATE INDEX idx_roles_role_code ON roles(role_code);
CREATE INDEX idx_permissions_tenant_id ON permissions(tenant_id);
CREATE INDEX idx_permissions_parent_id ON permissions(parent_id);
CREATE INDEX idx_permissions_permission_code ON permissions(permission_code);
CREATE INDEX idx_user_roles_tenant_id ON user_roles(tenant_id);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_role_permissions_tenant_id ON role_permissions(tenant_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);

-- Insert initial tenant data
INSERT INTO tenants (tenant_code, tenant_name, description, contact_person, contact_email, contact_phone, domain, status, max_users, subscription_plan, subscription_start_date, subscription_end_date) VALUES
('default', 'Default Tenant', 'System default tenant', 'System Admin', 'admin@default.com', '13800138000', 'default.local', 'ACTIVE', 1000, 'ENTERPRISE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 year'),
('demo', 'Demo Tenant', 'Demo tenant for testing', 'Demo User', 'demo@demo.com', '13800138001', 'demo.local', 'ACTIVE', 100, 'BASIC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '6 months');

-- Insert tenant config data
INSERT INTO tenant_configs (tenant_id, config_key, config_value, config_type, description, is_system) VALUES
(1, 'theme', 'default', 'STRING', 'System theme', true),
(1, 'language', 'zh-CN', 'STRING', 'System language', true),
(1, 'timezone', 'Asia/Shanghai', 'STRING', 'Timezone setting', true),
(2, 'theme', 'blue', 'STRING', 'System theme', false),
(2, 'language', 'zh-CN', 'STRING', 'System language', true),
(2, 'timezone', 'Asia/Shanghai', 'STRING', 'Timezone setting', true);

-- Insert initial user data (password: 123456, BCrypt encrypted)
INSERT INTO users (tenant_id, username, password, email, real_name, status, is_super_admin) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@default.com', 'System Administrator', 'ACTIVE', true),
(1, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'user1@default.com', 'Normal User 1', 'ACTIVE', false),
(2, 'demo_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@demo.com', 'Demo Administrator', 'ACTIVE', true),
(2, 'demo_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'user@demo.com', 'Demo User', 'ACTIVE', false);

-- Insert initial role data
INSERT INTO roles (tenant_id, role_name, role_code, description, is_system) VALUES
(1, 'Super Administrator', 'SUPER_ADMIN', 'System super administrator with all permissions', true),
(1, 'Administrator', 'ADMIN', 'Tenant administrator with all tenant permissions', true),
(1, 'Normal User', 'USER', 'Normal user with basic permissions', true),
(2, 'Super Administrator', 'SUPER_ADMIN', 'System super administrator with all permissions', true),
(2, 'Administrator', 'ADMIN', 'Tenant administrator with all tenant permissions', true),
(2, 'Normal User', 'USER', 'Normal user with basic permissions', true);

-- Insert initial permission data
INSERT INTO permissions (tenant_id, permission_name, permission_code, permission_type, parent_id, path, component, icon, sort_order, description, is_system) VALUES
-- System management permissions
(1, 'System Management', 'system:manage', 'MENU', NULL, '/system', 'SystemLayout', 'setting', 1, 'System management module', true),
(1, 'User Management', 'user:manage', 'MENU', 1, '/system/user', 'UserList', 'user', 1, 'User management', true),
(1, 'Role Management', 'role:manage', 'MENU', 1, '/system/role', 'RoleList', 'team', 2, 'Role management', true),
(1, 'Permission Management', 'permission:manage', 'MENU', 1, '/system/permission', 'PermissionList', 'safety', 3, 'Permission management', true),
(1, 'Tenant Management', 'tenant:manage', 'MENU', 1, '/system/tenant', 'TenantList', 'apartment', 4, 'Tenant management', true),

-- User management permissions
(1, 'User Query', 'user:query', 'API', 2, '/api/users', NULL, NULL, 1, 'Query user list', true),
(1, 'User Create', 'user:create', 'API', 2, '/api/users', NULL, NULL, 2, 'Create user', true),
(1, 'User Update', 'user:update', 'API', 2, '/api/users/{id}', NULL, NULL, 3, 'Update user info', true),
(1, 'User Delete', 'user:delete', 'API', 2, '/api/users/{id}', NULL, NULL, 4, 'Delete user', true),
(1, 'User Enable', 'user:enable', 'API', 2, '/api/users/{id}/enable', NULL, NULL, 5, 'Enable user', true),
(1, 'User Disable', 'user:disable', 'API', 2, '/api/users/{id}/disable', NULL, NULL, 6, 'Disable user', true),

-- Role management permissions
(1, 'Role Query', 'role:query', 'API', 3, '/api/roles', NULL, NULL, 1, 'Query role list', true),
(1, 'Role Create', 'role:create', 'API', 3, '/api/roles', NULL, NULL, 2, 'Create role', true),
(1, 'Role Update', 'role:update', 'API', 3, '/api/roles/{id}', NULL, NULL, 3, 'Update role info', true),
(1, 'Role Delete', 'role:delete', 'API', 3, '/api/roles/{id}', NULL, NULL, 4, 'Delete role', true),
(1, 'Role Permission Assign', 'role:assign', 'API', 3, '/api/roles/{id}/permissions', NULL, NULL, 5, 'Assign role permissions', true),

-- Permission management permissions
(1, 'Permission Query', 'permission:query', 'API', 4, '/api/permissions', NULL, NULL, 1, 'Query permission list', true),
(1, 'Permission Create', 'permission:create', 'API', 4, '/api/permissions', NULL, NULL, 2, 'Create permission', true),
(1, 'Permission Update', 'permission:update', 'API', 4, '/api/permissions/{id}', NULL, NULL, 3, 'Update permission info', true),
(1, 'Permission Delete', 'permission:delete', 'API', 4, '/api/permissions/{id}', NULL, NULL, 4, 'Delete permission', true),

-- Tenant management permissions
(1, 'Tenant Query', 'tenant:query', 'API', 5, '/api/tenants', NULL, NULL, 1, 'Query tenant list', true),
(1, 'Tenant Create', 'tenant:create', 'API', 5, '/api/tenants', NULL, NULL, 2, 'Create tenant', true),
(1, 'Tenant Update', 'tenant:update', 'API', 5, '/api/tenants/{id}', NULL, NULL, 3, 'Update tenant info', true),
(1, 'Tenant Delete', 'tenant:delete', 'API', 5, '/api/tenants/{id}', NULL, NULL, 4, 'Delete tenant', true),

-- Demo tenant permissions (simplified)
(2, 'System Management', 'system:manage', 'MENU', NULL, '/system', 'SystemLayout', 'setting', 1, 'System management module', true),
(2, 'User Management', 'user:manage', 'MENU', 20, '/system/user', 'UserList', 'user', 1, 'User management', true),
(2, 'User Query', 'user:query', 'API', 21, '/api/users', NULL, NULL, 1, 'Query user list', true),
(2, 'User Create', 'user:create', 'API', 21, '/api/users', NULL, NULL, 2, 'Create user', true);

-- Insert user role association data
INSERT INTO user_roles (tenant_id, user_id, role_id) VALUES
(1, 1, 1), -- admin -> Super Administrator
(1, 2, 3), -- user1 -> Normal User
(2, 3, 4), -- demo_admin -> Super Administrator
(2, 4, 6); -- demo_user -> Normal User

-- Insert role permission association data (super admin has all permissions)
INSERT INTO role_permissions (tenant_id, role_id, permission_id) VALUES
-- Default tenant super admin permissions
(1, 1, 1), (1, 1, 2), (1, 1, 3), (1, 1, 4), (1, 1, 5), (1, 1, 6), (1, 1, 7), (1, 1, 8), (1, 1, 9), (1, 1, 10), (1, 1, 11), (1, 1, 12), (1, 1, 13), (1, 1, 14), (1, 1, 15), (1, 1, 16), (1, 1, 17), (1, 1, 18), (1, 1, 19),
-- Default tenant admin permissions
(1, 2, 1), (1, 2, 2), (1, 2, 3), (1, 2, 4), (1, 2, 6), (1, 2, 7), (1, 2, 8), (1, 2, 9), (1, 2, 10), (1, 2, 11), (1, 2, 12), (1, 2, 13), (1, 2, 14), (1, 2, 15), (1, 2, 16), (1, 2, 17), (1, 2, 18), (1, 2, 19),
-- Default tenant normal user permissions
(1, 3, 6), (1, 3, 11), (1, 3, 15),
-- Demo tenant super admin permissions
(2, 4, 20), (2, 4, 21), (2, 4, 22), (2, 4, 23),
-- Demo tenant normal user permissions
(2, 6, 22);

-- Update tenant current user count
UPDATE tenants SET current_users = (SELECT COUNT(*) FROM users WHERE tenant_id = tenants.id);

-- Create trigger function to automatically update updated_at field
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for related tables
CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tenant_configs_updated_at BEFORE UPDATE ON tenant_configs FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_permissions_updated_at BEFORE UPDATE ON permissions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create user count statistics function
CREATE OR REPLACE FUNCTION update_tenant_user_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tenants SET current_users = current_users + 1 WHERE id = NEW.tenant_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE tenants SET current_users = current_users - 1 WHERE id = OLD.tenant_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ language 'plpgsql';

-- Create trigger for user table
CREATE TRIGGER update_tenant_user_count_trigger AFTER INSERT OR DELETE ON users FOR EACH ROW EXECUTE FUNCTION update_tenant_user_count();

-- Commit transaction
COMMIT; 