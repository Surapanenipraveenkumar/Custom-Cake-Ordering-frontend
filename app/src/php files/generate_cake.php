<?php
header("Content-Type: application/json");

/* =====================================
   CONFIGURATION - OPTIMIZED FOR SPEED
===================================== */

// $HF_TOKEN = "#";

// Using SDXL Turbo - Updated API URL (2024)
// $API_URL = "https://api-inference.huggingface.co/models/stabilityai/sdxl-turbo";

/* =====================================
   READ PROMPT
===================================== */

$rawInput = file_get_contents("php://input");
$data = json_decode($rawInput, true);

if (is_array($data) && isset($data['prompt'])) {
    $customerPrompt = $data['prompt'];
} elseif (isset($_POST['prompt'])) {
    $customerPrompt = $_POST['prompt'];
} else {
    $customerPrompt = trim($rawInput);
}

$customerPrompt = trim((string)$customerPrompt);

if ($customerPrompt === "") {
    echo json_encode([
        "status" => "error",
        "message" => "Please enter any cake idea"
    ]);
    exit;
}

/* =====================================
   PROMPT (SHORTER = FASTER)
===================================== */

$finalPrompt = "Professional bakery photo of " . $customerPrompt . 
", realistic cake, studio lighting, delicious, 4k";

$negativePrompt = "cartoon, blurry, low quality, ugly, watermark, text";

/* =====================================
   ENSURE IMAGE FOLDER
===================================== */

if (!is_dir("generated")) {
    mkdir("generated", 0777, true);
}

/* =====================================
   PARALLEL GENERATION USING CURL MULTI
   (Generate 4 images at the same time!)
===================================== */

$imageUrls = [];
$curlHandles = [];
$mh = curl_multi_init();

// Create 4 parallel requests
for ($i = 0; $i < 4; $i++) {
    $seed = random_int(1, 999999999);
    
    $payload = json_encode([
        "inputs" => $finalPrompt,
        "parameters" => [
            "negative_prompt" => $negativePrompt,
            "width" => 512,
            "height" => 512,
            "num_inference_steps" => 4,      // SDXL Turbo only needs 4 steps!
            "guidance_scale" => 0.0,          // SDXL Turbo uses 0 guidance
            "seed" => $seed
        ]
    ]);
    
    $ch = curl_init($API_URL);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_HTTPHEADER => [
            "Authorization: Bearer $HF_TOKEN",
            "Content-Type: application/json",
            "Accept: image/png"
        ],
        CURLOPT_POSTFIELDS => $payload,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 60
    ]);
    
    $curlHandles[$i] = $ch;
    curl_multi_add_handle($mh, $ch);
}

// Execute all requests in parallel
$running = null;
do {
    curl_multi_exec($mh, $running);
    curl_multi_select($mh);
} while ($running > 0);

// Collect results
$baseUrl = "";
$protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = $_SERVER['HTTP_HOST'];
$scriptDir = dirname($_SERVER['SCRIPT_NAME']);
$baseUrl = $protocol . "://" . $host . $scriptDir . "/";

$successCount = 0;
for ($i = 0; $i < 4; $i++) {
    $response = curl_multi_getcontent($curlHandles[$i]);
    $httpCode = curl_getinfo($curlHandles[$i], CURLINFO_HTTP_CODE);
    
    curl_multi_remove_handle($mh, $curlHandles[$i]);
    curl_close($curlHandles[$i]);
    
    if ($httpCode === 200 && strlen($response) > 1000) {
        $imageName = "cake_" . time() . "_" . $i . ".png";
        $imagePath = "generated/" . $imageName;
        file_put_contents($imagePath, $response);
        $imageUrls[] = $baseUrl . $imagePath;
        $successCount++;
    }
}

curl_multi_close($mh);

/* =====================================
   RESPONSE
===================================== */

if ($successCount > 0) {
    echo json_encode([
        "status" => "success",
        "images" => $imageUrls,
        "prompt_used" => $customerPrompt,
        "generated_count" => $successCount
    ]);
} else {
    // Fallback to regular model if Turbo fails
    echo json_encode([
        "status" => "error",
        "message" => "AI generation failed. Please try again."
    ]);
}
