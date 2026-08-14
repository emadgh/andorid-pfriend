# PFriend PHP API

A small framework-free PHP + MySQL backend for the PFriend native Android app.

## Requirements
- PHP 8.1+
- PDO MySQL
- MySQL 5.7+ / MariaDB 10.4+
- HTTPS

## Install
1. Upload all files from this branch to a folder on your PHP host.
2. Create an empty MySQL database and user in your hosting panel.
3. Visit `https://your-domain.example/pfriend/install.php`.
4. Enter database credentials and a statistics access key.
5. The installer creates the tables, writes `config.php`, and creates `.installed` to prevent rerunning installation.
6. In the Android app, enter the folder URL, e.g. `https://your-domain.example/pfriend/`.

## Visibility model
PFriend intentionally has no per-entry privacy controls. Every authenticated account can view every user's tracker entries and daily totals. Registration requires explicit acceptance of this rule. Email addresses and password hashes are not public through the API.

## Statistics page
Open `stats.php?key=YOUR_STATS_KEY`. This page is read-only; there is no administration CRUD UI.
