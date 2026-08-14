<?php
declare(strict_types=1);
require __DIR__ . '/bootstrap.php';

$action = (string)($_GET['action'] ?? '');

try {
    switch ($action) {
        case 'server_info':
            $cfg = config();
            json_response(['ok' => true, 'name' => $cfg['app_name'] ?? 'PFriend', 'api_version' => 1]);

        case 'register':
            require_method('POST');
            $b = input_json();
            if (empty($b['accept_visibility'])) fail('You must accept the global visibility rule.');
            $username = strtolower(trim((string)($b['username'] ?? '')));
            $email = strtolower(trim((string)($b['email'] ?? '')));
            $display = trim((string)($b['display_name'] ?? ''));
            $password = (string)($b['password'] ?? '');
            if (!preg_match('/^[a-z0-9_.-]{3,40}$/', $username)) fail('Username must be 3-40 characters using letters, numbers, dot, underscore or dash.');
            if (!filter_var($email, FILTER_VALIDATE_EMAIL)) fail('Invalid email address.');
            if ($display === '' || mb_strlen($display) > 100) fail('Display name is required and must be under 100 characters.');
            if (strlen($password) < 8) fail('Password must be at least 8 characters.');
            $stmt = db()->prepare('INSERT INTO users (username,email,password_hash,display_name,accepted_visibility_at) VALUES (?,?,?,?,NOW())');
            try { $stmt->execute([$username, $email, password_hash($password, PASSWORD_DEFAULT), $display]); }
            catch (PDOException $e) {
                if ((string)$e->getCode() === '23000') fail('Username or email is already in use.', 409);
                throw $e;
            }
            $id = (int)db()->lastInsertId();
            $user = ['id'=>$id,'username'=>$username,'email'=>$email,'display_name'=>$display];
            json_response(['ok'=>true,'token'=>issue_token($id),'user'=>public_user($user,true)], 201);

        case 'login':
            require_method('POST');
            $b = input_json();
            $identity = strtolower(trim((string)($b['identity'] ?? '')));
            $password = (string)($b['password'] ?? '');
            $stmt = db()->prepare('SELECT * FROM users WHERE username=? OR email=? LIMIT 1');
            $stmt->execute([$identity,$identity]);
            $u = $stmt->fetch();
            if (!$u || !password_verify($password, $u['password_hash'])) fail('Invalid username/email or password.', 401);
            json_response(['ok'=>true,'token'=>issue_token((int)$u['id']),'user'=>public_user($u,true)]);

        case 'me':
            $u = current_user();
            json_response(['ok'=>true,'user'=>public_user($u,true)]);

        case 'users':
            $me = current_user();
            $stmt = db()->prepare('SELECT u.id,u.username,u.display_name, CASE WHEN f.following_id IS NULL THEN 0 ELSE 1 END is_following FROM users u LEFT JOIN follows f ON f.following_id=u.id AND f.follower_id=? ORDER BY u.display_name,u.username LIMIT 500');
            $stmt->execute([(int)$me['id']]);
            $users = array_map(fn($u)=>[
                'id'=>(int)$u['id'],'username'=>$u['username'],'display_name'=>$u['display_name'],'is_following'=>(bool)$u['is_following']
            ], $stmt->fetchAll());
            json_response(['ok'=>true,'users'=>$users]);

        case 'user':
            $me = current_user();
            $id = (int)($_GET['id'] ?? 0);
            $stmt = db()->prepare('SELECT u.id,u.username,u.display_name, CASE WHEN f.following_id IS NULL THEN 0 ELSE 1 END is_following FROM users u LEFT JOIN follows f ON f.following_id=u.id AND f.follower_id=? WHERE u.id=? LIMIT 1');
            $stmt->execute([(int)$me['id'],$id]);
            $u = $stmt->fetch();
            if (!$u) fail('User not found.',404);
            json_response(['ok'=>true,'user'=>['id'=>(int)$u['id'],'username'=>$u['username'],'display_name'=>$u['display_name'],'is_following'=>(bool)$u['is_following']]]);

        case 'entry_add':
            require_method('POST');
            $me = current_user();
            $b = input_json();
            $type = strtolower(trim((string)($b['type'] ?? '')));
            if (!in_array($type,['water','food','urine','bowel'],true)) fail('Invalid entry type.');
            $amount = array_key_exists('amount',$b) && $b['amount'] !== null ? (float)$b['amount'] : null;
            if ($amount !== null && ($amount < 0 || $amount > 100000)) fail('Invalid amount.');
            $unit = isset($b['unit']) ? mb_substr(trim((string)$b['unit']),0,16) : null;
            $label = isset($b['label']) ? mb_substr(trim((string)$b['label']),0,120) : null;
            $note = isset($b['note']) ? mb_substr(trim((string)$b['note']),0,500) : null;
            $uuid = sprintf('%04x%04x-%04x-%04x-%04x-%04x%04x%04x', random_int(0,65535),random_int(0,65535),random_int(0,65535),random_int(0,0x0fff)|0x4000,random_int(0,0x3fff)|0x8000,random_int(0,65535),random_int(0,65535),random_int(0,65535));
            $stmt = db()->prepare('INSERT INTO entries (uuid,user_id,type,amount_value,unit,label,note,occurred_at) VALUES (?,?,?,?,?,?,?,NOW())');
            $stmt->execute([$uuid,(int)$me['id'],$type,$amount,$unit,$label,$note]);
            json_response(['ok'=>true,'id'=>(int)db()->lastInsertId()],201);

        case 'entries':
            current_user();
            $limit = max(1,min(100,(int)($_GET['limit'] ?? 30)));
            $userId = (int)($_GET['user_id'] ?? 0);
            $sql = 'SELECT e.id,e.user_id,e.type,e.amount_value,e.unit,e.label,e.note,e.occurred_at,u.username,u.display_name FROM entries e JOIN users u ON u.id=e.user_id WHERE e.deleted_at IS NULL';
            $args = [];
            if ($userId > 0) { $sql .= ' AND e.user_id=?'; $args[]=$userId; }
            $sql .= ' ORDER BY e.occurred_at DESC,e.id DESC LIMIT ' . $limit;
            $stmt = db()->prepare($sql); $stmt->execute($args);
            $rows = array_map(function($r){
                $r['id']=(int)$r['id']; $r['user_id']=(int)$r['user_id'];
                $r['amount_value']=$r['amount_value']===null?null:(float)$r['amount_value']; return $r;
            },$stmt->fetchAll());
            json_response(['ok'=>true,'entries'=>$rows]);

        case 'daily':
            current_user();
            $userId = (int)($_GET['user_id'] ?? 0);
            if ($userId <= 0) $userId = (int)current_user()['id'];
            $stmt = db()->prepare("SELECT COALESCE(SUM(CASE WHEN type='water' THEN amount_value ELSE 0 END),0) water_ml, COALESCE(SUM(CASE WHEN type='food' THEN amount_value ELSE 0 END),0) food_g, SUM(CASE WHEN type='urine' THEN 1 ELSE 0 END) urine_count, COALESCE(SUM(CASE WHEN type='urine' THEN amount_value ELSE 0 END),0) urine_ml, SUM(CASE WHEN type='bowel' THEN 1 ELSE 0 END) bowel_count FROM entries WHERE user_id=? AND deleted_at IS NULL AND occurred_at>=CURDATE() AND occurred_at<DATE_ADD(CURDATE(),INTERVAL 1 DAY)");
            $stmt->execute([$userId]); $s=$stmt->fetch();
            json_response(['ok'=>true,'summary'=>['water_ml'=>(float)$s['water_ml'],'food_g'=>(float)$s['food_g'],'urine_count'=>(int)$s['urine_count'],'urine_ml'=>(float)$s['urine_ml'],'bowel_count'=>(int)$s['bowel_count']]]);

        case 'compare':
            $me = current_user();
            $circleId = (int)($_GET['circle_id'] ?? 0);
            $args = [];
            $join = '';
            if ($circleId > 0) {
                $membership = db()->prepare('SELECT 1 FROM circle_members WHERE circle_id=? AND user_id=?');
                $membership->execute([$circleId, (int)$me['id']]);
                if (!$membership->fetchColumn()) fail('Circle not found or you are not a member.', 404);
                $join = ' JOIN circle_members cm ON cm.user_id=u.id AND cm.circle_id=? ';
                $args[] = $circleId;
            }
            $sql = "SELECT u.id,u.username,u.display_name,
                COALESCE(SUM(CASE WHEN e.type='water' THEN e.amount_value ELSE 0 END),0) water_ml,
                COALESCE(SUM(CASE WHEN e.type='food' THEN e.amount_value ELSE 0 END),0) food_g,
                SUM(CASE WHEN e.type='urine' THEN 1 ELSE 0 END) urine_count,
                COALESCE(SUM(CASE WHEN e.type='urine' THEN e.amount_value ELSE 0 END),0) urine_ml,
                SUM(CASE WHEN e.type='bowel' THEN 1 ELSE 0 END) bowel_count
                FROM users u $join
                LEFT JOIN entries e ON e.user_id=u.id AND e.deleted_at IS NULL
                    AND e.occurred_at>=CURDATE() AND e.occurred_at<DATE_ADD(CURDATE(),INTERVAL 1 DAY)
                GROUP BY u.id,u.username,u.display_name
                ORDER BY u.display_name,u.username LIMIT 500";
            $stmt = db()->prepare($sql); $stmt->execute($args);
            $rows = array_map(fn($r)=>[
                'id'=>(int)$r['id'],'username'=>$r['username'],'display_name'=>$r['display_name'],
                'water_ml'=>(float)$r['water_ml'],'food_g'=>(float)$r['food_g'],
                'urine_count'=>(int)$r['urine_count'],'urine_ml'=>(float)$r['urine_ml'],
                'bowel_count'=>(int)$r['bowel_count']
            ], $stmt->fetchAll());
            json_response(['ok'=>true,'comparison'=>$rows]);

        case 'follow_toggle':
            require_method('POST');
            $me=current_user(); $b=input_json(); $target=(int)($b['user_id']??0);
            if ($target<=0 || $target===(int)$me['id']) fail('Invalid user.');
            $check=db()->prepare('SELECT 1 FROM users WHERE id=?'); $check->execute([$target]); if(!$check->fetchColumn()) fail('User not found.',404);
            $stmt=db()->prepare('SELECT 1 FROM follows WHERE follower_id=? AND following_id=?'); $stmt->execute([(int)$me['id'],$target]);
            if($stmt->fetchColumn()) { db()->prepare('DELETE FROM follows WHERE follower_id=? AND following_id=?')->execute([(int)$me['id'],$target]); $following=false; }
            else { db()->prepare('INSERT INTO follows (follower_id,following_id) VALUES (?,?)')->execute([(int)$me['id'],$target]); $following=true; }
            json_response(['ok'=>true,'following'=>$following]);

        case 'circles':
            $me=current_user();
            $stmt=db()->prepare('SELECT c.id,c.owner_id,c.name,COUNT(cm2.user_id) member_count FROM circles c JOIN circle_members mine ON mine.circle_id=c.id AND mine.user_id=? LEFT JOIN circle_members cm2 ON cm2.circle_id=c.id GROUP BY c.id,c.owner_id,c.name ORDER BY c.created_at DESC');
            $stmt->execute([(int)$me['id']]);
            $rows=array_map(fn($r)=>['id'=>(int)$r['id'],'owner_id'=>(int)$r['owner_id'],'name'=>$r['name'],'member_count'=>(int)$r['member_count']],$stmt->fetchAll());
            json_response(['ok'=>true,'circles'=>$rows]);

        case 'circle_create':
            require_method('POST'); $me=current_user(); $b=input_json(); $name=trim((string)($b['name']??''));
            if($name===''||mb_strlen($name)>100) fail('Circle name is required and must be under 100 characters.');
            db()->beginTransaction();
            try { db()->prepare('INSERT INTO circles (owner_id,name) VALUES (?,?)')->execute([(int)$me['id'],$name]); $id=(int)db()->lastInsertId(); db()->prepare("INSERT INTO circle_members (circle_id,user_id,role) VALUES (?,?,'owner')")->execute([$id,(int)$me['id']]); db()->commit(); }
            catch(Throwable $e){ db()->rollBack(); throw $e; }
            json_response(['ok'=>true,'circle'=>['id'=>$id,'owner_id'=>(int)$me['id'],'name'=>$name,'member_count'=>1]],201);

        case 'circle':
            $me=current_user(); $id=(int)($_GET['id']??0);
            $stmt=db()->prepare('SELECT c.id,c.owner_id,c.name,(SELECT COUNT(*) FROM circle_members x WHERE x.circle_id=c.id) member_count FROM circles c JOIN circle_members mine ON mine.circle_id=c.id AND mine.user_id=? WHERE c.id=? LIMIT 1');
            $stmt->execute([(int)$me['id'],$id]); $c=$stmt->fetch(); if(!$c) fail('Circle not found or you are not a member.',404);
            $m=db()->prepare('SELECT u.id,u.username,u.display_name FROM circle_members cm JOIN users u ON u.id=cm.user_id WHERE cm.circle_id=? ORDER BY cm.role DESC,u.display_name'); $m->execute([$id]);
            $members=array_map(fn($u)=>['id'=>(int)$u['id'],'username'=>$u['username'],'display_name'=>$u['display_name']],$m->fetchAll());
            json_response(['ok'=>true,'circle'=>['id'=>(int)$c['id'],'owner_id'=>(int)$c['owner_id'],'name'=>$c['name'],'member_count'=>(int)$c['member_count']],'members'=>$members]);

        case 'circle_add_member':
            require_method('POST'); $me=current_user(); $b=input_json(); $circleId=(int)($b['circle_id']??0); $username=strtolower(trim((string)($b['username']??'')));
            $own=db()->prepare('SELECT 1 FROM circles WHERE id=? AND owner_id=?'); $own->execute([$circleId,(int)$me['id']]); if(!$own->fetchColumn()) fail('Only the circle owner can add members.',403);
            $u=db()->prepare('SELECT id FROM users WHERE username=?'); $u->execute([$username]); $uid=(int)$u->fetchColumn(); if($uid<=0) fail('User not found.',404);
            db()->prepare("INSERT IGNORE INTO circle_members (circle_id,user_id,role) VALUES (?,?,'member')")->execute([$circleId,$uid]);
            json_response(['ok'=>true]);

        default:
            fail('Unknown API action.',404);
    }
} catch (PDOException $e) {
    error_log('PFriend DB error: '.$e->getMessage());
    fail('Database error.',500);
} catch (Throwable $e) {
    error_log('PFriend error: '.$e->getMessage());
    fail($e->getMessage() ?: 'Server error.',500);
}
