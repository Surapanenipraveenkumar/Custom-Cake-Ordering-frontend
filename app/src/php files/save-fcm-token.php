<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

include 'db.php';

// Get JSON input
$json = file_get_contents("php://input");
$data = json_decode($json, true);

if (!$data) {
    echo json_encode(array("status" => "error", "message" => "Invalid JSON"));
    exit;
}

$user_type = $data['user_type'] ?? '';
$user_id = intval($data['user_id'] ?? 0);
$fcm_token = $data['fcm_token'] ?? '';

// Validate inputs
if (empty($user_type) || $user_id <= 0 || empty($fcm_token)) {
    echo json_encode(array("status" => "error", "message" => "Missing required fields"));
    exit;
}

// Determine which table to update based on user type
switch ($user_type) {
    case 'customer':
        $table = 'users';
        $id_column = 'id';
        break;
    case 'baker':
        $table = 'bakers';
        $id_column = 'id';
        break;
    case 'delivery':
        $table = 'delivery_persons';
        $id_column = 'delivery_id';
        break;
    default:
        echo json_encode(array("status" => "error", "message" => "Invalid user type"));
        exit;
}

// Sanitize token
$fcm_token = mysqli_real_escape_string($conn, $fcm_token);

// Update the FCM token
$sql = "UPDATE $table SET fcm_token = '$fcm_token' WHERE $id_column = $user_id";

if (mysqli_query($conn, $sql)) {
    if (mysqli_affected_rows($conn) > 0) {
        echo json_encode(array(
            "status" => "success",
            "message" => "FCM token saved successfully"
        ));
    } else {
        echo json_encode(array(
            "status" => "success",
            "message" => "No changes made (token may already be the same)"
        ));
    }
} else {
    echo json_encode(array(
        "status" => "error",
        "message" => "Failed to save FCM token: " . mysqli_error($conn)
    ));
}

mysqli_close($conn);
?>
