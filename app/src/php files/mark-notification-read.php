<?php
header("Content-Type: application/json");
include "db.php";

$data = json_decode(file_get_contents("php://input"), true);
if (!$data) $data = $_POST;

$notification_id = $data['notification_id'] ?? null;
$user_id = $data['user_id'] ?? null;
$mark_all = $data['mark_all'] ?? false;
$user_type = $data['user_type'] ?? null;

if ($mark_all && $user_id && $user_type) {
    // Mark all notifications as read for user
    $user_id = intval($user_id);
    $user_type = mysqli_real_escape_string($conn, $user_type);
    
    $result = mysqli_query($conn, "
        UPDATE notifications 
        SET is_read = 1 
        WHERE user_id = $user_id AND user_type = '$user_type'
    ");
    
    echo json_encode([
        "status" => "success",
        "message" => "All notifications marked as read"
    ]);
    exit;
}

if (!$notification_id) {
    echo json_encode([
        "status" => "error",
        "message" => "notification_id required"
    ]);
    exit;
}

$notification_id = intval($notification_id);

$result = mysqli_query($conn, "
    UPDATE notifications 
    SET is_read = 1 
    WHERE notification_id = $notification_id
");

if ($result) {
    echo json_encode([
        "status" => "success",
        "message" => "Notification marked as read"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to update notification"
    ]);
}
?>
