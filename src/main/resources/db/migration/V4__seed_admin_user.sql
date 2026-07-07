-- Usuario ADMIN semilla para poder gestionar espacios y usuarios desde el
-- primer arranque. El endpoint de registro (/api/auth/register) siempre crea
-- usuarios con rol USER (AuthService.register), y crear un ADMIN vía
-- POST /api/users exige ya estar autenticado como ADMIN — sin este seed no
-- habría forma de arrancar el sistema.
--
-- Password: Admin123! (hash BCrypt generado con BCryptPasswordEncoder, el
-- mismo encoder usado por AuthService para el resto de usuarios).
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES (
    'Administrador',
    'admin@coworking.com',
    '$2a$10$nNmJnyvXNc/513asyWdJcOPjEB8XWowZQ/sP9tijY/iv8bCK7AQxW',
    'ADMIN',
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;
