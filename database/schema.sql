-- 创建数据库
CREATE DATABASE IF NOT EXISTS note_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE note_system;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER-普通用户, ADMIN-管理员',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除 0:否 1:是',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) COMMENT '标签颜色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 笔记表
CREATE TABLE IF NOT EXISTS note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category_id BIGINT COMMENT '分类ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    summary VARCHAR(500) COMMENT '摘要',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开 0:否 1:是',
    approval_status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '审批状态: DRAFT-草稿, PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除 0:否 1:是',
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_approval_status (approval_status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- 笔记标签关联表
CREATE TABLE IF NOT EXISTS note_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    INDEX idx_note_id (note_id),
    INDEX idx_tag_id (tag_id),
    UNIQUE KEY uk_note_tag (note_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记标签关联表';

-- 审批记录表
CREATE TABLE IF NOT EXISTS note_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审批记录ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '提交用户ID',
    approval_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态: PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝',
    admin_id BIGINT COMMENT '审批管理员ID',
    admin_remark VARCHAR(500) COMMENT '审批意见',
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    approved_at DATETIME COMMENT '审批时间',
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 收藏表
CREATE TABLE IF NOT EXISTS note_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_note_user (note_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 评论表
CREATE TABLE IF NOT EXISTS note_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID，用于回复功能',
    reply_to_user_id BIGINT DEFAULT NULL COMMENT '回复的用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态 0:隐藏 1:显示',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除 0:否 1:是',
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 修改审批表，添加管理员查看记录
ALTER TABLE note_approval ADD COLUMN viewed_by_admin TINYINT DEFAULT 0 COMMENT '管理员是否已查看 0:否 1:是';
ALTER TABLE note_approval ADD COLUMN viewed_at DATETIME DEFAULT NULL COMMENT '管理员查看时间';
ALTER TABLE note_approval ADD COLUMN viewed_admin_id BIGINT DEFAULT NULL COMMENT '查看的管理员ID';

-- 插入测试数据
-- 默认用户: admin / admin123 (管理员角色)
-- 密码使用BCrypt加密
INSERT INTO user (username, password, nickname, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '管理员', 'ADMIN');

-- 默认分类
INSERT INTO category (user_id, name, sort) VALUES 
(1, '学习笔记', 1),
(1, '课堂记录', 2),
(1, '知识点总结', 3);

-- 默认标签
INSERT INTO tag (user_id, name, color) VALUES 
(1, '重要', '#F53F3F'),
(1, '待复习', '#E6A23C'),
(1, '已掌握', '#67C23A'),
(1, '编程', '#409EFF'),
(1, '数学', '#722ED1'),
(1, '英语', '#00B42A');
