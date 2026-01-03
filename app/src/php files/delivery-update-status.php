<?php
header("Content-Type: application/json");
include "db.php";

// Accept JSON or form data
$data = json_decode(file_get_contents("php://input"), true);
if (!$data) {
    $data = $_POST;
}

$delivery_id = $data['delivery_id'] ?? null;
$order_id = $data['order_id'] ?? null;
$action = $data['action'] ?? null; // accept, pickup, deliver, reject

if (!$delivery_id || !$order_id || !$action) {
    echo json_encode([
        "status" => "error",
        "message" => "delivery_id, order_id and action are required"
    ]);
    exit;
}

$delivery_id = mysqli_real_escape_string($conn, $delivery_id);
$order_id = mysqli_real_escape_string($conn, $order_id);
$action = mysqli_real_escape_string($conn, $action);

// Check if order exists
$orderCheck = mysqli_query($conn, "SELECT * FROM orders WHERE order_id = '$order_id'");
if (mysqli_num_rows($orderCheck) == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Order not found"
    ]);
    exit;
}

$order = mysqli_fetch_assoc($orderCheck);

// Perform action
switch ($action) {
    case 'accept':
        // Assign order to this delivery person
        $result = mysqli_query($conn, "
            UPDATE orders 
            SET delivery_id = '$delivery_id', 
                delivery_status = 'assigned'
            WHERE order_id = '$order_id'
        ");
        $message = "Order accepted successfully";
        break;
        
    case 'pickup':
        // Mark as picked up and update main status for customer tracking
        $result = mysqli_query($conn, "
            UPDATE orders 
            SET delivery_status = 'picked_up',
                picked_up_at = NOW(),
                status = 'out_for_delivery'
            WHERE order_id = '$order_id' 
            AND delivery_id = '$delivery_id'
        ");
        $message = "Order picked up - Customer notified";
        break;
        
    case 'deliver':
        // Mark as delivered
        $result = mysqli_query($conn, "
            UPDATE orders 
            SET delivery_status = 'delivered',
                delivered_at = NOW(),
                status = 'delivered'
            WHERE order_id = '$order_id' 
            AND delivery_id = '$delivery_id'
        ");
        $message = "Order delivered successfully";
        break;
        
    case 'reject':
        // Remove assignment
        $result = mysqli_query($conn, "
            UPDATE orders 
            SET delivery_id = NULL,
                delivery_status = 'pending'
            WHERE order_id = '$order_id' 
            AND delivery_id = '$delivery_id'
        ");
        $message = "Order rejected";
        break;
        
    default:
        echo json_encode([
            "status" => "error",
            "message" => "Invalid action"
        ]);
        exit;
}

if ($result) {
    echo json_encode([
        "status" => "success",
        "message" => $message
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Update failed: " . mysqli_error($conn)
    ]);
}
?>
