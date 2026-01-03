<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

// Enable error logging for debugging
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);

include "db.php";
include "send-push-notification.php";

// Check connection
if (!$conn) {
    echo json_encode(["status"=>"error","message"=>"DB connection failed"]);
    exit;
}

// Read JSON input
$raw = file_get_contents("php://input");
$data = json_decode($raw, true);

// Log received data for debugging
error_log("place-order.php: Received data: " . $raw);

if (!$data || !isset($data['user_id'])) {
    echo json_encode(["status"=>"error","message"=>"user_id required"]);
    exit;
}

$user_id = intval($data['user_id']);
$delivery_fee = intval($data['delivery_fee'] ?? 0);

// Get additional fields from request
$delivery_address = mysqli_real_escape_string($conn, $data['delivery_address'] ?? '');
$delivery_date = mysqli_real_escape_string($conn, $data['delivery_date'] ?? '');
$delivery_time = mysqli_real_escape_string($conn, $data['delivery_time'] ?? '');
$payment_method = mysqli_real_escape_string($conn, $data['payment_method'] ?? '');

error_log("place-order.php: user_id=$user_id, delivery_fee=$delivery_fee, address=$delivery_address, date=$delivery_date, time=$delivery_time, payment=$payment_method");

// Get cart items
$cartQ = mysqli_query($conn, "
    SELECT cart.cart_id, cart.cake_id, cart.quantity,
           cakes.cake_name, cakes.price, cakes.baker_id, cakes.image
    FROM cart
    JOIN cakes ON cart.cake_id = cakes.cake_id
    WHERE cart.user_id = $user_id
");

if (!$cartQ) {
    error_log("place-order.php: Cart query failed: " . mysqli_error($conn));
    echo json_encode(["status"=>"error","message"=>"Cart query failed: " . mysqli_error($conn)]);
    exit;
}

if (mysqli_num_rows($cartQ) == 0) {
    error_log("place-order.php: Cart is empty for user_id=$user_id");
    echo json_encode(["status"=>"error","message"=>"Cart is empty"]);
    exit;
}

$subtotal = 0;
$baker_id = 0;
$items = [];

while ($row = mysqli_fetch_assoc($cartQ)) {
    $item_total = floatval($row['price']) * intval($row['quantity']);
    $subtotal += $item_total;
    $baker_id = intval($row['baker_id']);
    
    $items[] = [
        "cake_id" => intval($row['cake_id']),
        "cake_name" => $row['cake_name'],
        "price" => floatval($row['price']),
        "quantity" => intval($row['quantity']),
        "image" => $row['image'],
        "item_total" => $item_total
    ];
}

$total = $subtotal + $delivery_fee;

// Insert order into orders table with all fields
$sql = "INSERT INTO orders (user_id, baker_id, total_amount, status, delivery_address, delivery_date, delivery_time, payment_method) 
        VALUES ($user_id, $baker_id, $total, 'pending', '$delivery_address', '$delivery_date', '$delivery_time', '$payment_method')";

error_log("place-order.php: SQL query: " . $sql);

$insert = mysqli_query($conn, $sql);

if (!$insert) {
    error_log("place-order.php: Order insert failed: " . mysqli_error($conn));
    echo json_encode(["status"=>"error","message"=>"Order insert failed: ".mysqli_error($conn)]);
    exit;
}

$order_id = mysqli_insert_id($conn);
$order_id_str = "ORD" . str_pad($order_id, 8, "0", STR_PAD_LEFT);

error_log("place-order.php: Order created successfully: order_id=$order_id, order_id_str=$order_id_str");

// Try to insert order_items (ignore if table doesn't exist)
$table_check = mysqli_query($conn, "SHOW TABLES LIKE 'order_items'");
if ($table_check && mysqli_num_rows($table_check) > 0) {
    foreach ($items as $item) {
        mysqli_query($conn, "INSERT INTO order_items (order_id, cake_id, quantity, price) VALUES ($order_id, {$item['cake_id']}, {$item['quantity']}, {$item['price']})");
    }
}

// Clear cart
mysqli_query($conn, "DELETE FROM cart WHERE user_id = $user_id");

// 🔥 Send push notification to baker about new order
$customer_name = "Customer";
$customer_query = mysqli_query($conn, "SELECT name FROM users WHERE id = $user_id");
if ($customer_query && mysqli_num_rows($customer_query) > 0) {
    $customer_row = mysqli_fetch_assoc($customer_query);
    $customer_name = $customer_row['name'];
}
notifyBakerNewOrder($conn, $baker_id, $order_id, $customer_name);

// Return success
echo json_encode([
    "status" => "success",
    "message" => "Order placed",
    "order_id" => $order_id,
    "order_id_str" => $order_id_str,
    "subtotal" => $subtotal,
    "delivery_fee" => $delivery_fee,
    "total_amount" => $total,
    "items" => $items
]);
?>
