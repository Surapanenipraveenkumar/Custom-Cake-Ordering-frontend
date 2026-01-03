<?php
// Prevent any output before JSON
ob_start();

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST");
header("Access-Control-Allow-Headers: Content-Type");

// Clear any output buffer
ob_end_clean();

include "db.php";

// Read JSON or GET/POST
$data = json_decode(file_get_contents("php://input"), true);
$order_id = $data['order_id'] ?? $_GET['order_id'] ?? $_POST['order_id'] ?? null;
$user_id = $data['user_id'] ?? $_GET['user_id'] ?? $_POST['user_id'] ?? null;

if (!$order_id) {
    echo json_encode([
        "status" => "error",
        "message" => "order_id required"
    ]);
    exit;
}

$order_id = intval($order_id);

// Verify the order exists and belongs to this user (if user_id provided)
$checkQuery = "SELECT order_id, user_id, status FROM orders WHERE order_id = $order_id";
$checkResult = mysqli_query($conn, $checkQuery);

if (!$checkResult || mysqli_num_rows($checkResult) == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Order not found"
    ]);
    exit;
}

$order = mysqli_fetch_assoc($checkResult);

// Check if order can be cancelled (not already delivered or cancelled)
$currentStatus = strtolower($order['status']);
if ($currentStatus == 'delivered') {
    echo json_encode([
        "status" => "error",
        "message" => "Cannot cancel a delivered order"
    ]);
    exit;
}

if ($currentStatus == 'cancelled') {
    echo json_encode([
        "status" => "error",
        "message" => "Order is already cancelled"
    ]);
    exit;
}

// Update order status to cancelled
$updateQuery = "UPDATE orders SET status = 'cancelled' WHERE order_id = $order_id";
$updateResult = mysqli_query($conn, $updateQuery);

if (!$updateResult) {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to cancel order: " . mysqli_error($conn)
    ]);
    exit;
}

echo json_encode([
    "status" => "success",
    "message" => "Order cancelled successfully",
    "order_id" => $order_id
]);
