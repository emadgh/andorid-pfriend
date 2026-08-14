<?php
declare(strict_types=1);
require __DIR__ . '/bootstrap.php';
$cfg = config();
$key = (string)($_GET['key'] ?? '');
if ($key === '' || !password_verify($key, (string)$cfg['stats_key_hash'])) {
    http_response_code(403); exit('Forbidden');
}
$pdo = db();
$stats = [
    'Users' => (int)$pdo->query('SELECT COUNT(*) FROM users')->fetchColumn(),
    'Entries' => (int)$pdo->query('SELECT COUNT(*) FROM entries WHERE deleted_at IS NULL')->fetchColumn(),
    'Water logs today' => (int)$pdo->query("SELECT COUNT(*) FROM entries WHERE type='water' AND deleted_at IS NULL AND occurred_at>=CURDATE()")->fetchColumn(),
    'Food logs today' => (int)$pdo->query("SELECT COUNT(*) FROM entries WHERE type='food' AND deleted_at IS NULL AND occurred_at>=CURDATE()")->fetchColumn(),
    'Urination logs today' => (int)$pdo->query("SELECT COUNT(*) FROM entries WHERE type='urine' AND deleted_at IS NULL AND occurred_at>=CURDATE()")->fetchColumn(),
    'Bowel logs today' => (int)$pdo->query("SELECT COUNT(*) FROM entries WHERE type='bowel' AND deleted_at IS NULL AND occurred_at>=CURDATE()")->fetchColumn(),
    'Circles' => (int)$pdo->query('SELECT COUNT(*) FROM circles')->fetchColumn(),
    'Follows' => (int)$pdo->query('SELECT COUNT(*) FROM follows')->fetchColumn(),
];
?><!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>PFriend Stats</title><style>body{font-family:system-ui;margin:0;background:#f5f5f5;color:#171717}.wrap{max-width:900px;margin:6vh auto;padding:20px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:14px}.card{background:#fff;padding:20px;border-radius:16px;box-shadow:0 5px 25px #0001}.n{font-size:34px;font-weight:750}.l{color:#666;margin-top:6px}</style></head><body><main class="wrap"><h1><?=htmlspecialchars((string)($cfg['app_name']??'PFriend'))?> — Stats</h1><div class="grid"><?php foreach($stats as $label=>$value): ?><div class="card"><div class="n"><?=$value?></div><div class="l"><?=htmlspecialchars($label)?></div></div><?php endforeach; ?></div></main></body></html>
