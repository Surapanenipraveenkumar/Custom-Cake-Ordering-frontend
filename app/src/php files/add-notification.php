<?php
header("Content-Type: application/json");
include "db.php";

$data = json_decode(file_get_contents("php://input"), true);
if (!$data) $data = $_POST;

$user_type = $data['user_type'] ?? null;
$user_id = $data['user_id'] ?? null;
$type = $data['type'] ?? null;
$title = $data['title'] ?? null;
$message = $data['message'] ?? null;
$order_id = $data['order_id'] ?? null;

if (!$user_type || !$user_id || !$type || !$title) {
    echo json_encode([
        "status" => "error",
        "message" => "user_type, user_id, type, and title are required"
    ]);
    exit;
}

$user_type = mysqli_real_escape_string($conn, $user_type);
$user_id = intval($user_id);
$type = mysqli_real_escape_string($conn, $type);
$title = mysqli_real_escape_string($conn, $title);
$message = $message ? mysqli_real_escape_string($conn, $message) : "";
$order_id = $order_id ? intval($order_id) : "NULL";

$result = mysqli_query($conn, "
    INSERT INTO notifications (user_type, user_id, type, title, message, order_id)
    VALUES ('$user_type', $user_id, '$type', '$title', '$message', $order_id)
");

if ($result) {
    echo json_encode([
        "status" => "success",
        "message" => "Notification created",
        "notification_id" => mysqli_insert_id($conn)
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to create notification: " . mysqli_error($conn)
    ]);
}
?>
