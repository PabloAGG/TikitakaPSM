CREATE DATABASE tikitaka_db;
USE tikitaka_db;

-- TABLA: SELECCIONES DE FÚTBOL (teams)

CREATE TABLE teams (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    confederation VARCHAR(50) NOT NULL,
    flag_url VARCHAR(255),
    logo_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar selecciones de fútbol
INSERT INTO teams (name, confederation, flag_url, logo_url) VALUES
-- CONMEBOL
('Argentina', 'CONMEBOL', 'https://flagcdn.com/ar.svg', 'https://flagcdn.com/ar.svg'),
('Brasil', 'CONMEBOL', 'https://flagcdn.com/br.svg', 'https://flagcdn.com/br.svg'),
('Uruguay', 'CONMEBOL', 'https://flagcdn.com/uy.svg', 'https://flagcdn.com/uy.svg'),
('Colombia', 'CONMEBOL', 'https://flagcdn.com/co.svg', 'https://flagcdn.com/co.svg'),
('Chile', 'CONMEBOL', 'https://flagcdn.com/cl.svg', 'https://flagcdn.com/cl.svg'),
('Perú', 'CONMEBOL', 'https://flagcdn.com/pe.svg', 'https://flagcdn.com/pe.svg'),
('Ecuador', 'CONMEBOL', 'https://flagcdn.com/ec.svg', 'https://flagcdn.com/ec.svg'),
('Venezuela', 'CONMEBOL', 'https://flagcdn.com/ve.svg', 'https://flagcdn.com/ve.svg'),
('Bolivia', 'CONMEBOL', 'https://flagcdn.com/bo.svg', 'https://flagcdn.com/bo.svg'),
('Paraguay', 'CONMEBOL', 'https://flagcdn.com/py.svg', 'https://flagcdn.com/py.svg'),

-- UEFA
('España', 'UEFA', 'https://flagcdn.com/es.svg', 'https://flagcdn.com/es.svg'),
('Francia', 'UEFA', 'https://flagcdn.com/fr.svg', 'https://flagcdn.com/fr.svg'),
('Alemania', 'UEFA', 'https://flagcdn.com/de.svg', 'https://flagcdn.com/de.svg'),
('Italia', 'UEFA', 'https://flagcdn.com/it.svg', 'https://flagcdn.com/it.svg'),
('Inglaterra', 'UEFA', 'https://flagcdn.com/gb-eng.svg', 'https://flagcdn.com/gb-eng.svg'),
('Portugal', 'UEFA', 'https://flagcdn.com/pt.svg', 'https://flagcdn.com/pt.svg'),
('Países Bajos', 'UEFA', 'https://flagcdn.com/nl.svg', 'https://flagcdn.com/nl.svg'),
('Bélgica', 'UEFA', 'https://flagcdn.com/be.svg', 'https://flagcdn.com/be.svg'),
('Croacia', 'UEFA', 'https://flagcdn.com/hr.svg', 'https://flagcdn.com/hr.svg'),

-- CONCACAF
('México', 'CONCACAF', 'https://flagcdn.com/mx.svg', 'https://flagcdn.com/mx.svg'),
('Estados Unidos', 'CONCACAF', 'https://flagcdn.com/us.svg', 'https://flagcdn.com/us.svg'),
('Canadá', 'CONCACAF', 'https://flagcdn.com/ca.svg', 'https://flagcdn.com/ca.svg'),
('Costa Rica', 'CONCACAF', 'https://flagcdn.com/cr.svg', 'https://flagcdn.com/cr.svg'),

-- AFC
('Japón', 'AFC', 'https://flagcdn.com/jp.svg', 'https://flagcdn.com/jp.svg'),
('Corea del Sur', 'AFC', 'https://flagcdn.com/kr.svg', 'https://flagcdn.com/kr.svg'),
('Australia', 'AFC', 'https://flagcdn.com/au.svg', 'https://flagcdn.com/au.svg'),
('Arabia Saudí', 'AFC', 'https://flagcdn.com/sa.svg', 'https://flagcdn.com/sa.svg'),

-- CAF
('Marruecos', 'CAF', 'https://flagcdn.com/ma.svg', 'https://flagcdn.com/ma.svg'),
('Túnez', 'CAF', 'https://flagcdn.com/tn.svg', 'https://flagcdn.com/tn.svg'),
('Egipto', 'CAF', 'https://flagcdn.com/eg.svg', 'https://flagcdn.com/eg.svg'),
('Nigeria', 'CAF', 'https://flagcdn.com/ng.svg', 'https://flagcdn.com/ng.svg'),
('Ghana', 'CAF', 'https://flagcdn.com/gh.svg', 'https://flagcdn.com/gh.svg'),
('Senegal', 'CAF', 'https://flagcdn.com/sn.svg', 'https://flagcdn.com/sn.svg'),
('Camerún', 'CAF', 'https://flagcdn.com/cm.svg', 'https://flagcdn.com/cm.svg');


-- TABLA: USUARIOS

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    full_name VARCHAR(200) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    profile_image VARCHAR(255),
    team_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL,
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_team_id (team_id)
);

-- TABLA: PUBLICACIONES
CREATE TABLE posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    team_id INT,
    image_url VARCHAR(255),
    likes_count INT DEFAULT 0,
    is_draft BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_team_id (team_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_draft (is_draft),
    INDEX idx_is_active (is_active)
);

-- TABLA: LIKES DE PUBLICACIONES
CREATE TABLE post_likes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_post (user_id, post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id)
);

-- TABLA: FAVORITOS DE USUARIOS
CREATE TABLE user_favorites (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_favorite (user_id, post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id)
);

-- TABLA: SESIONES DE USUARIO (para autenticación)
CREATE TABLE user_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
);

-- TRIGGERS 

-- Trigger para incrementar likes_count cuando se agrega un like
DELIMITER //
CREATE TRIGGER increment_likes_count 
AFTER INSERT ON post_likes
FOR EACH ROW
BEGIN
    UPDATE posts 
    SET likes_count = likes_count + 1 
    WHERE id = NEW.post_id;
END//

-- Trigger para decrementar likes_count cuando se quita un like
CREATE TRIGGER decrement_likes_count 
AFTER DELETE ON post_likes
FOR EACH ROW
BEGIN
    UPDATE posts 
    SET likes_count = likes_count - 1 
    WHERE id = OLD.post_id;
END//
DELIMITER ;

-- VISTAS 
-- Vista para obtener posts completos con información del usuario y equipo
CREATE VIEW posts_complete AS
SELECT 
    p.id,
    p.title,
    p.content,
    p.image_url,
    p.likes_count,
    p.is_draft,
    p.created_at,
    p.updated_at,
    u.first_name,
    u.last_name,
    u.profile_image as user_profile_image,
    ft.name as team_name,
    ft.flag_url as team_flag
FROM posts p
JOIN users u ON p.user_id = u.id
LEFT JOIN teams ft ON p.team_id = ft.id
WHERE p.is_active = TRUE;

-- Vista para estadísticas de usuarios
CREATE VIEW user_stats AS
SELECT 
    u.id,
    u.first_name,
    u.last_name,
    u.email,
    COUNT(DISTINCT p.id) as total_posts,
    COUNT(DISTINCT CASE WHEN p.is_draft = FALSE THEN p.id END) as published_posts,
    COUNT(DISTINCT CASE WHEN p.is_draft = TRUE THEN p.id END) as draft_posts,
    SUM(p.likes_count) as total_likes_received,
    ft.name as favorite_team
FROM users u
LEFT JOIN posts p ON u.id = p.user_id AND p.is_active = TRUE
LEFT JOIN teams ft ON u.team_id = ft.id
WHERE u.is_active = TRUE
GROUP BY u.id;

-- PROCEDIMIENTOS ALMACENADOS 

-- Procedimiento para obtener feed personalizado por selección favorita
DELIMITER //
CREATE PROCEDURE GetPersonalizedFeed(IN user_id INT, IN limit_posts INT)
BEGIN
    DECLARE user_favorite_team INT;
    
    -- Obtener el equipo favorito del usuario
    SELECT team_id INTO user_favorite_team 
    FROM users WHERE id = user_id;
    
    -- Obtener posts ordenados por relevancia (equipo favorito primero)
    SELECT 
        pc.*,
        CASE WHEN uf.post_id IS NOT NULL THEN TRUE ELSE FALSE END as is_favorited,
        CASE WHEN pl.post_id IS NOT NULL THEN TRUE ELSE FALSE END as is_liked
    FROM posts_complete pc
    LEFT JOIN user_favorites uf ON pc.id = uf.post_id AND uf.user_id = user_id
    LEFT JOIN post_likes pl ON pc.id = pl.post_id AND pl.user_id = user_id
    WHERE pc.is_draft = FALSE
    ORDER BY 
        CASE WHEN pc.team_name = (SELECT name FROM teams WHERE id = user_favorite_team) THEN 0 ELSE 1 END,
        pc.created_at DESC
    LIMIT limit_posts;
END //
DELIMITER ;

-- ÍNDICES
CREATE INDEX idx_posts_team_created ON posts(team_id, created_at DESC);
CREATE INDEX idx_posts_user_draft ON posts(user_id, is_draft, created_at DESC);
CREATE INDEX idx_users_team_active ON users(team_id, is_active);

-- TABLA: COMENTARIOS EN POSTS
CREATE TABLE comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- TABLA: LIKES EN COMENTARIOS
CREATE TABLE comment_likes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    comment_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_comment_like (comment_id, user_id)
);

-- TABLA: NOTIFICACIONES PUSH
CREATE TABLE push_tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    device_type ENUM('android', 'ios', 'web') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- TABLA: NOTIFICACIONES
CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type ENUM('like', 'comment', 'favorite', 'follow', 'mention') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSON,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ÍNDICES PARA COMENTARIOS Y NOTIFICACIONES
CREATE INDEX idx_comments_post ON comments(post_id, created_at DESC);
CREATE INDEX idx_comments_user ON comments(user_id, created_at DESC);
CREATE INDEX idx_comment_likes_comment ON comment_likes(comment_id);
CREATE INDEX idx_comment_likes_user ON comment_likes(user_id);
CREATE INDEX idx_push_tokens_user ON push_tokens(user_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read, created_at DESC);
