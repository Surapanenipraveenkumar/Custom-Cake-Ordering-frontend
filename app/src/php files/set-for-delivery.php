<?php
// set-for-delivery.php
// Baker marks an order as ready for delivery pickup

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

error_reporting(0);

$host = "localhost";
$username = "root";
$password = "";
$database = "custom-cake";

$conn = new mysqli($host, $username, $password, $database);

if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

// Get input
$data = json_decode(file_get_contents("php://input"), true);
$order_id = isset($data['order_id']) ? intval($data['order_id']) : 0;
$baker_id = isset($data['baker_id']) ? intval($data['baker_id']) : 0;

if ($order_id <= 0 || $baker_id <= 0) {
    echo json_encode(["status" => "error", "message" => "Missing order_id or baker_id"]);
    exit;
}

// Verify the order belongs to this baker
$check_sql = "SELECT order_id, status FROM orders WHERE order_id = ? AND baker_id = ?";
$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("ii", $order_id, $baker_id);
$check_stmt->execute();
$result = $check_stmt->get_result();

if ($result->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Order not found or not authorized"]);
    exit;
}

$order = $result->fetch_assoc();

// Ensure delivery columns exist
$conn->query("ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_status VARCHAR(50) DEFAULT 'pending'");
$conn->query("ALTER TABLE orders ADD COLUMN IF NOT EXISTS ready_for_delivery TINYINT(1) DEFAULT 0");
$conn->query("ALTER TABLE orders ADD COLUMN IF NOT EXISTS ready_for_delivery_at DATETIME DEFAULT NULL");

// Update order to be ready for delivery
$update_sql = "UPDATE orders SET 
    status = 'ready_for_pickup',
    delivery_status = 'pending',
    ready_for_delivery = 1,
    ready_for_delivery_at = NOW()
    WHERE order_id = ?";

$update_stmt = $conn->prepare($update_sql);
$update_stmt->bind_param("i", $order_id);

if ($update_stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Order is now available for delivery partners"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to update order: " . $conn->error
    ]);
}

$conn->close();
?>
