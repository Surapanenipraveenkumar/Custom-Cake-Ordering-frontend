<?php
header("Content-Type: application/json");
include "db.php";

// Create notifications table if not exists
mysqli_query($conn, "
    CREATE TABLE IF NOT EXISTS notifications (
        notification_id INT AUTO_INCREMENT PRIMARY KEY,
        user_type ENUM('customer', 'baker', 'delivery') NOT NULL,
        user_id INT NOT NULL,
        type VARCHAR(50) NOT NULL,
        title VARCHAR(255) NOT NULL,
        message TEXT,
        order_id INT DEFAULT NULL,
        is_read TINYINT(1) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_user (user_type, user_id),
        INDEX idx_read (is_read)
    )
");

$user_type = $_GET['user_type'] ?? null;
$user_id = $_GET['user_id'] ?? null;

if (!$user_type || !$user_id) {
    echo json_encode([
        "status" => "error",
        "message" => "user_type and user_id required"
    ]);
    exit;
}

$user_type = mysqli_real_escape_string($conn, $user_type);
$user_id = intval($user_id);

// Get notifications for user
$query = mysqli_query($conn, "
    SELECT * FROM notifications 
    WHERE user_type = '$user_type' AND user_id = $user_id
    ORDER BY created_at DESC
    LIMIT 50
");

$notifications = [];
$unread_count = 0;

while ($row = mysqli_fetch_assoc($query)) {
    $notifications[] = [
        "notification_id" => (int)$row['notification_id'],
        "type" => $row['type'],
        "title" => $row['title'],
        "message" => $row['message'],
        "order_id" => $row['order_id'] ? (int)$row['order_id'] : null,
        "is_read" => (bool)$row['is_read'],
        "created_at" => $row['created_at'],
        "time_ago" => getTimeAgo($row['created_at'])
    ];
    
    if (!$row['is_read']) {
        $unread_count++;
    }
}

function getTimeAgo($datetime) {
    $time = strtotime($datetime);
    $diff = time() - $time;
    
    if ($diff < 60) return "Just now";
    if ($diff < 3600) return floor($diff / 60) . " min ago";
    if ($diff < 86400) return floor($diff / 3600) . " hr ago";
    if ($diff < 604800) return floor($diff / 86400) . " days ago";
    return date('M d', $time);
}

echo json_encode([
    "status" => "success",
    "unread_count" => $unread_count,
    "notifications" => $notifications
]);
?>
