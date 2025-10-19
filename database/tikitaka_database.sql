-- =====================================================
-- TIKITAKA PSM - BASE DE DATOS
-- Script SQL para crear la estructura completa
-- =====================================================

-- Crear base de datos
CREATE DATABASE tikitaka_db;
USE tikitaka_db;

-- =====================================================
-- TABLA: SELECCIONES DE FÚTBOL
-- =====================================================
CREATE TABLE football_teams (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    confederation VARCHAR(50) NOT NULL,
    flag_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar selecciones de fútbol
INSERT INTO football_teams (name, confederation, flag_url) VALUES
-- CONMEBOL
('Argentina', 'CONMEBOL', 'https://flagcdn.com/ar.svg'),
('Brasil', 'CONMEBOL', 'https://flagcdn.com/br.svg'),
('Uruguay', 'CONMEBOL', 'https://flagcdn.com/uy.svg'),
('Colombia', 'CONMEBOL', 'https://flagcdn.com/co.svg'),
('Chile', 'CONMEBOL', 'https://flagcdn.com/cl.svg'),
('Perú', 'CONMEBOL', 'https://flagcdn.com/pe.svg'),
('Ecuador', 'CONMEBOL', 'https://flagcdn.com/ec.svg'),
('Venezuela', 'CONMEBOL', 'https://flagcdn.com/ve.svg'),
('Bolivia', 'CONMEBOL', 'https://flagcdn.com/bo.svg'),
('Paraguay', 'CONMEBOL', 'https://flagcdn.com/py.svg'),

-- UEFA
('España', 'UEFA', 'https://flagcdn.com/es.svg'),
('Francia', 'UEFA', 'https://flagcdn.com/fr.svg'),
('Alemania', 'UEFA', 'https://flagcdn.com/de.svg'),
('Italia', 'UEFA', 'https://flagcdn.com/it.svg'),
('Inglaterra', 'UEFA', 'https://flagcdn.com/gb-eng.svg'),
('Portugal', 'UEFA', 'https://flagcdn.com/pt.svg'),
('Países Bajos', 'UEFA', 'https://flagcdn.com/nl.svg'),
('Bélgica', 'UEFA', 'https://flagcdn.com/be.svg'),
('Croacia', 'UEFA', 'https://flagcdn.com/hr.svg'),

-- CONCACAF
('México', 'CONCACAF', 'https://flagcdn.com/mx.svg'),
('Estados Unidos', 'CONCACAF', 'https://flagcdn.com/us.svg'),
('Canadá', 'CONCACAF', 'https://flagcdn.com/ca.svg'),
('Costa Rica', 'CONCACAF', 'https://flagcdn.com/cr.svg'),

-- AFC
('Japón', 'AFC', 'https://flagcdn.com/jp.svg'),
('Corea del Sur', 'AFC', 'https://flagcdn.com/kr.svg'),
('Australia', 'AFC', 'https://flagcdn.com/au.svg'),
('Arabia Saudí', 'AFC', 'https://flagcdn.com/sa.svg'),

-- CAF
('Marruecos', 'CAF', 'https://flagcdn.com/ma.svg'),
('Túnez', 'CAF', 'https://flagcdn.com/tn.svg'),
('Egipto', 'CAF', 'https://flagcdn.com/eg.svg'),
('Nigeria', 'CAF', 'https://flagcdn.com/ng.svg'),
('Ghana', 'CAF', 'https://flagcdn.com/gh.svg'),
('Senegal', 'CAF', 'https://flagcdn.com/sn.svg'),
('Camerún', 'CAF', 'https://flagcdn.com/cm.svg');

-- =====================================================
-- TABLA: USUARIOS
-- =====================================================
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(255),
    favorite_team_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (favorite_team_id) REFERENCES football_teams(id) ON DELETE SET NULL,
    INDEX idx_email (email),
    INDEX idx_favorite_team (favorite_team_id)
);

-- =====================================================
-- TABLA: PUBLICACIONES
-- =====================================================
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
    FOREIGN KEY (team_id) REFERENCES football_teams(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_team_id (team_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_draft (is_draft),
    INDEX idx_is_active (is_active)
);

-- =====================================================
-- TABLA: LIKES DE PUBLICACIONES
-- =====================================================
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

-- =====================================================
-- TABLA: FAVORITOS DE USUARIOS
-- =====================================================
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

-- =====================================================
-- TABLA: SESIONES DE USUARIO (para autenticación)
-- =====================================================
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

-- =====================================================
-- TRIGGERS PARA AUTOMATIZAR CONTADORES
-- =====================================================

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

-- =====================================================
-- VISTAS ÚTILES
-- =====================================================

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
    u.profile_image_url as user_profile_image,
    ft.name as team_name,
    ft.flag_url as team_flag
FROM posts p
JOIN users u ON p.user_id = u.id
LEFT JOIN football_teams ft ON p.team_id = ft.id
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
LEFT JOIN football_teams ft ON u.favorite_team_id = ft.id
WHERE u.is_active = TRUE
GROUP BY u.id;

-- =====================================================
-- DATOS DE PRUEBA
-- =====================================================

-- Insertar usuarios de prueba
INSERT INTO users (first_name, last_name, email, phone, password_hash, favorite_team_id) VALUES
('Pablo', 'García', 'pablo@tikitaka.com', '8199191919', '$2b$10$example_hash_1', 1), -- Argentina
('María', 'López', 'maria@tikitaka.com', '8199191920', '$2b$10$example_hash_2', 2), -- Brasil
('Carlos', 'Rodríguez', 'carlos@tikitaka.com', '8199191921', '$2b$10$example_hash_3', 20), -- México
('Ana', 'Martínez', 'ana@tikitaka.com', '8199191922', '$2b$10$example_hash_4', 11); -- España

-- Insertar publicaciones de prueba
INSERT INTO posts (user_id, title, content, team_id, is_draft) VALUES
(1, 'Victoria de Argentina', 'Increíble partido de la Albiceleste! Messi estuvo espectacular.', 1, FALSE),
(2, 'Brasil en cuartos', 'La Canarinha sigue avanzando con buen fútbol.', 2, FALSE),
(3, 'El Tri se prepara', 'México se alista para el próximo mundial.', 20, FALSE),
(1, 'Borrador sobre táctica', 'Análisis táctico pendiente de completar...', 1, TRUE),
(4, 'La Roja Española', 'España vuelve a brillar con su toque característico.', 11, FALSE);

-- Insertar algunos likes y favoritos de prueba
INSERT INTO post_likes (user_id, post_id) VALUES
(2, 1), (3, 1), (4, 1), -- 3 likes para el post de Argentina
(1, 2), (3, 2), -- 2 likes para el post de Brasil
(1, 5), (2, 5); -- 2 likes para el post de España

INSERT INTO user_favorites (user_id, post_id) VALUES
(2, 1), -- María marca como favorito el post de Argentina
(1, 2), -- Pablo marca como favorito el post de Brasil
(3, 5); -- Carlos marca como favorito el post de España

-- =====================================================
-- PROCEDIMIENTOS ALMACENADOS ÚTILES
-- =====================================================

-- Procedimiento para obtener feed personalizado por selección favorita
DELIMITER //
CREATE PROCEDURE GetPersonalizedFeed(IN user_id INT, IN limit_posts INT)
BEGIN
    DECLARE user_favorite_team INT;
    
    -- Obtener el equipo favorito del usuario
    SELECT favorite_team_id INTO user_favorite_team 
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
        CASE WHEN pc.team_name = (SELECT name FROM football_teams WHERE id = user_favorite_team) THEN 0 ELSE 1 END,
        pc.created_at DESC
    LIMIT limit_posts;
END //
DELIMITER ;

-- =====================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- =====================================================
CREATE INDEX idx_posts_team_created ON posts(team_id, created_at DESC);
CREATE INDEX idx_posts_user_draft ON posts(user_id, is_draft, created_at DESC);
CREATE INDEX idx_users_team_active ON users(favorite_team_id, is_active);

-- =====================================================
-- SCRIPT COMPLETADO
-- Base de datos lista para Tikitaka PSM
-- =====================================================