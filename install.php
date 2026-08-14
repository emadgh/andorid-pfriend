<?php
declare(strict_types=1);

$lock = __DIR__ . '/.installed';
if (is_file($lock)) {
    http_response_code(403);
    exit('PFriend is already installed. Delete .installed manually only if you intentionally want to reinstall.');
}

$error = null;
$success = false;
if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'POST') {
    $host = trim((string)($_POST['db_host'] ?? 'localhost'));
    $port = (int)($_POST['db_port'] ?? 3306);
    $name = trim((string)($_POST['db_name'] ?? ''));
    $user = trim((string)($_POST['db_user'] ?? ''));
    $pass = (string)($_POST['db_pass'] ?? '');
    $appName = trim((string)($_POST['app_name'] ?? 'PFriend')) ?: 'PFriend';
    $statsKey = (string)($_POST['stats_key'] ?? '');

    try {
        if ($name === '' || $user === '') throw new RuntimeException('Database name and user are required.');
        if (strlen($statsKey) < 10) throw new RuntimeException('Statistics key must be at least 10 characters.');
        $dsn = sprintf('mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4', $host, $port, $name);
        $pdo = new PDO($dsn, $user, $pass, [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]);

        $schema = [
            "CREATE TABLE IF NOT EXISTS users (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, username VARCHAR(40) NOT NULL UNIQUE, email VARCHAR(190) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, display_name VARCHAR(100) NOT NULL, accepted_visibility_at DATETIME NOT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS auth_tokens (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, user_id BIGINT UNSIGNED NOT NULL, token_hash CHAR(64) NOT NULL UNIQUE, expires_at DATETIME NOT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX(user_id), INDEX(expires_at), CONSTRAINT fk_tokens_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS entries (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, uuid CHAR(36) NOT NULL UNIQUE, user_id BIGINT UNSIGNED NOT NULL, type VARCHAR(20) NOT NULL, amount_value DECIMAL(12,2) NULL, unit VARCHAR(16) NULL, label VARCHAR(120) NULL, note VARCHAR(500) NULL, occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at DATETIME NULL, INDEX(user_id, occurred_at), INDEX(type, occurred_at), CONSTRAINT fk_entries_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS follows (follower_id BIGINT UNSIGNED NOT NULL, following_id BIGINT UNSIGNED NOT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(follower_id, following_id), CONSTRAINT fk_follows_a FOREIGN KEY(follower_id) REFERENCES users(id) ON DELETE CASCADE, CONSTRAINT fk_follows_b FOREIGN KEY(following_id) REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS circles (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, owner_id BIGINT UNSIGNED NOT NULL, name VARCHAR(100) NOT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX(owner_id), CONSTRAINT fk_circles_owner FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS circle_members (circle_id BIGINT UNSIGNED NOT NULL, user_id BIGINT UNSIGNED NOT NULL, role VARCHAR(20) NOT NULL DEFAULT 'member', joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(circle_id, user_id), CONSTRAINT fk_cm_circle FOREIGN KEY(circle_id) REFERENCES circles(id) ON DELETE CASCADE, CONSTRAINT fk_cm_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        ];
        foreach ($schema as $sql) $pdo->exec($sql);

        $cfg = "<?php\nreturn " . var_export([
            'app_name' => $appName,
            'db' => ['host' => $host, 'port' => $port, 'name' => $name, 'user' => $user, 'pass' => $pass],
            'stats_key_hash' => password_hash($statsKey, PASSWORD_DEFAULT),
        ], true) . ";\n";
        if (file_put_contents(__DIR__ . '/config.php', $cfg, LOCK_EX) === false) throw new RuntimeException('Could not write config.php. Check directory permissions.');
        file_put_contents($lock, date(DATE_ATOM) . "\n", LOCK_EX);
        $success = true;
    } catch (Throwable $e) {
        $error = $e->getMessage();
    }
}
?><!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Install PFriend</title><style>body{font-family:system-ui;background:#f5f5f5;margin:0}.box{max-width:640px;margin:5vh auto;background:#fff;padding:28px;border-radius:18px;box-shadow:0 8px 40px #0001}label{display:block;margin-top:14px;font-weight:600}input{width:100%;box-sizing:border-box;padding:11px;margin-top:6px;border:1px solid #bbb;border-radius:10px}button{margin-top:20px;padding:12px 18px;border:0;border-radius:10px;background:#111;color:#fff}.err{color:#a00}.ok{color:#075}</style></head><body><div class="box"><h1>PFriend installer</h1><?php if($success): ?><p class="ok">Installation complete. You can now connect the Android app to this folder URL. For statistics, open <code>stats.php?key=YOUR_KEY</code>.</p><?php else: ?><?php if($error): ?><p class="err"><?=htmlspecialchars($error)?></p><?php endif; ?><form method="post"><label>App name<input name="app_name" value="PFriend"></label><label>DB host<input name="db_host" value="localhost" required></label><label>DB port<input name="db_port" value="3306" required></label><label>DB name<input name="db_name" required></label><label>DB user<input name="db_user" required></label><label>DB password<input name="db_pass" type="password"></label><label>Statistics access key<input name="stats_key" type="password" minlength="10" required></label><button>Install</button></form><?php endif; ?></div></body></html>
