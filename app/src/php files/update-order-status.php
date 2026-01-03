<?php
header("Content-Type: application/json");
include "db.php";
include "send-push-notification.php";

// Get POST data
$order_id = $_POST['order_id'] ?? null;
$status = $_POST['status'] ?? null;

if (!$order_id || !$status) {
    echo json_encode([
        "status" => "error",
        "message" => "order_id and status required"
    ]);
    exit;
}

// Validate status
$validStatuses = ['pending', 'confirmed', 'preparing', 'in_progress', 'ready', 'out_for_delivery', 'delivered', 'cancelled'];
if (!in_array($status, $validStatuses)) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid status"
    ]);
    exit;
}

// Get user_id from order before updating
$user_id = 0;
$order_query = mysqli_query($conn, "SELECT user_id FROM orders WHERE order_id = '$order_id'");
if ($order_query && mysqli_num_rows($order_query) > 0) {
    $order_row = mysqli_fetch_assoc($order_query);
    $user_id = intval($order_row['user_id']);
}

// Update order status
$result = mysqli_query($conn, "
    UPDATE orders SET status = '$status' WHERE order_id = '$order_id'
");

if ($result) {
    // 🔥 Send push notification to customer about status update
    if ($user_id > 0) {
        notifyCustomerOrderStatus($conn, $user_id, $order_id, $status);
    }
    
    echo json_encode([
        "status" => "success",
        "message" => "Order status updated to $status"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to update order status"
    ]);
}
?>
