<?php
declare(strict_types=1);

const PFRIEND_ROOT = __DIR__;

function json_response(array $data, int $status = 200): never {
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('X-Content-Type-Options: nosniff');
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function fail(string $message, int $status = 400): never {
    json_response(['ok' => false, 'error' => $message], $status);
}

function config(): array {
    $path = PFRIEND_ROOT . '/config.php';
    if (!is_file($path)) fail('PFriend is not installed. Run install.php first.', 503);
    $cfg = require $path;
    if (!is_array($cfg)) fail('Invalid server configuration.', 500);
    return $cfg;
}

function db(): PDO {
    static $pdo = null;
    if ($pdo instanceof PDO) return $pdo;
    $c = config()['db'];
    $dsn = sprintf('mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4', $c['host'], $c['port'], $c['name']);
    $pdo = new PDO($dsn, $c['user'], $c['pass'], [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
    return $pdo;
}

function input_json(): array {
    $raw = file_get_contents('php://input') ?: '';
    if ($raw === '') return [];
    try { return json_decode($raw, true, 64, JSON_THROW_ON_ERROR); }
    catch (Throwable) { fail('Invalid JSON body.'); }
}

function bearer_token(): ?string {
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if (preg_match('/^Bearer\s+(.+)$/i', trim($header), $m)) return trim($m[1]);
    return null;
}

function current_user(bool $required = true): ?array {
    $token = bearer_token();
    if (!$token) {
        if ($required) fail('Authentication required.', 401);
        return null;
    }
    $hash = hash('sha256', $token);
    $stmt = db()->prepare('SELECT u.* FROM auth_tokens t JOIN users u ON u.id=t.user_id WHERE t.token_hash=? AND t.expires_at > NOW() LIMIT 1');
    $stmt->execute([$hash]);
    $user = $stmt->fetch();
    if (!$user && $required) fail('Session expired or invalid.', 401);
    return $user ?: null;
}

function issue_token(int $userId): string {
    $token = bin2hex(random_bytes(32));
    $hash = hash('sha256', $token);
    $stmt = db()->prepare('INSERT INTO auth_tokens (user_id, token_hash, expires_at, created_at) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 90 DAY), NOW())');
    $stmt->execute([$userId, $hash]);
    return $token;
}

function public_user(array $u, bool $includeEmail = false, bool $following = false): array {
    $out = [
        'id' => (int)$u['id'],
        'username' => $u['username'],
        'display_name' => $u['display_name'],
        'is_following' => $following,
    ];
    if ($includeEmail) $out['email'] = $u['email'];
    return $out;
}

function require_method(string $method): void {
    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== $method) fail('Method not allowed.', 405);
}
