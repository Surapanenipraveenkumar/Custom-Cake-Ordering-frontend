<?php
/**
 * Firebase Cloud Messaging Helper
 * 
 * This file provides functions to send push notifications via FCM.
 * Include this file in other PHP scripts that need to send notifications.
 * 
 * IMPORTANT: Replace FIREBASE_SERVER_KEY with your actual Firebase Server Key
 * Get it from: Firebase Console > Project Settings > Cloud Messaging > Server Key
 */

// ⚠️ REPLACE THIS WITH YOUR ACTUAL FIREBASE SERVER KEY
define('FIREBASE_SERVER_KEY', 'YOUR_FIREBASE_SERVER_KEY_HERE');

/**
 * Send a push notification to a single device
 * 
 * @param string $fcm_token The device's FCM token
 * @param string $title Notification title
 * @param string $body Notification body text
 * @param array $data Optional data payload
 * @return array Result with success status and message
 */
function sendPushNotification($fcm_token, $title, $body, $data = array()) {
    if (empty($fcm_token)) {
        return array('success' => false, 'message' => 'No FCM token provided');
    }
    
    $url = 'https://fcm.googleapis.com/fcm/send';
    
    // Build the notification payload
    $notification = array(
        'title' => $title,
        'body' => $body,
        'sound' => 'default',
        'click_action' => 'FLUTTER_NOTIFICATION_CLICK'
    );
    
    // Merge data with notification type
    $data['title'] = $title;
    $data['body'] = $body;
    
    $fields = array(
        'to' => $fcm_token,
        'notification' => $notification,
        'data' => $data,
        'priority' => 'high'
    );
    
    $headers = array(
        'Authorization: key=' . FIREBASE_SERVER_KEY,
        'Content-Type: application/json'
    );
    
    // Initialize cURL
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
    
    $result = curl_exec($ch);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($error) {
        return array('success' => false, 'message' => 'cURL error: ' . $error);
    }
    
    $response = json_decode($result, true);
    
    if (isset($response['success']) && $response['success'] == 1) {
        return array('success' => true, 'message' => 'Notification sent successfully');
    } else {
        return array('success' => false, 'message' => 'FCM error: ' . $result);
    }
}

/**
 * Send notification to multiple devices
 * 
 * @param array $fcm_tokens Array of FCM tokens
 * @param string $title Notification title
 * @param string $body Notification body text
 * @param array $data Optional data payload
 * @return array Result with success status and message
 */
function sendPushNotificationToMultiple($fcm_tokens, $title, $body, $data = array()) {
    if (empty($fcm_tokens)) {
        return array('success' => false, 'message' => 'No FCM tokens provided');
    }
    
    $url = 'https://fcm.googleapis.com/fcm/send';
    
    $notification = array(
        'title' => $title,
        'body' => $body,
        'sound' => 'default'
    );
    
    $data['title'] = $title;
    $data['body'] = $body;
    
    $fields = array(
        'registration_ids' => $fcm_tokens,
        'notification' => $notification,
        'data' => $data,
        'priority' => 'high'
    );
    
    $headers = array(
        'Authorization: key=' . FIREBASE_SERVER_KEY,
        'Content-Type: application/json'
    );
    
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
    
    $result = curl_exec($ch);
    curl_close($ch);
    
    $response = json_decode($result, true);
    
    return array(
        'success' => isset($response['success']) && $response['success'] > 0,
        'message' => $result,
        'sent' => $response['success'] ?? 0,
        'failed' => $response['failure'] ?? 0
    );
}

/**
 * Get FCM token for a user
 * 
 * @param mysqli $conn Database connection
 * @param string $user_type Type of user (customer, baker, delivery)
 * @param int $user_id User's ID
 * @return string|null FCM token or null if not found
 */
function getFcmToken($conn, $user_type, $user_id) {
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
            return null;
    }
    
    $sql = "SELECT fcm_token FROM $table WHERE $id_column = " . intval($user_id);
    $result = mysqli_query($conn, $sql);
    
    if ($result && mysqli_num_rows($result) > 0) {
        $row = mysqli_fetch_assoc($result);
        return $row['fcm_token'];
    }
    
    return null;
}

/**
 * Notify baker about new order
 */
function notifyBakerNewOrder($conn, $baker_id, $order_id, $customer_name) {
    $fcm_token = getFcmToken($conn, 'baker', $baker_id);
    if ($fcm_token) {
        return sendPushNotification(
            $fcm_token,
            "🎂 New Order Received!",
            "Order #$order_id from $customer_name",
            array(
                'type' => 'new_order',
                'order_id' => strval($order_id)
            )
        );
    }
    return array('success' => false, 'message' => 'Baker FCM token not found');
}

/**
 * Notify customer about order status change
 */
function notifyCustomerOrderStatus($conn, $user_id, $order_id, $status) {
    $fcm_token = getFcmToken($conn, 'customer', $user_id);
    
    $status_messages = array(
        'confirmed' => 'Your order has been confirmed by the baker!',
        'preparing' => 'Your order is now being prepared! 🍰',
        'ready' => 'Your order is ready for pickup/delivery!',
        'out_for_delivery' => 'Your order is out for delivery! 🚚',
        'delivered' => 'Your order has been delivered! Enjoy! ❤️',
        'cancelled' => 'Your order has been cancelled.'
    );
    
    $message = $status_messages[$status] ?? "Order status updated to: $status";
    
    if ($fcm_token) {
        return sendPushNotification(
            $fcm_token,
            "Order #$order_id Update",
            $message,
            array(
                'type' => 'order_status',
                'order_id' => strval($order_id),
                'status' => $status
            )
        );
    }
    return array('success' => false, 'message' => 'Customer FCM token not found');
}

/**
 * Notify delivery partner about new delivery assignment
 */
function notifyDeliveryNewAssignment($conn, $delivery_id, $order_id) {
    $fcm_token = getFcmToken($conn, 'delivery', $delivery_id);
    if ($fcm_token) {
        return sendPushNotification(
            $fcm_token,
            "🚚 New Delivery Assignment!",
            "Order #$order_id is ready for pickup",
            array(
                'type' => 'delivery_assigned',
                'order_id' => strval($order_id)
            )
        );
    }
    return array('success' => false, 'message' => 'Delivery partner FCM token not found');
}
?>
