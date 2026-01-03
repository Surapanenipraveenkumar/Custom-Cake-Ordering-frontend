-- Create chat_messages table for bakercustomer messaging
CREATE TABLE IF NOT EXISTS chat_messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    baker_id INT NOT NULL,
    user_id INT NOT NULL,
    sender_type ENUM('baker', 'customer') NOT NULL,
    message TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (baker_id) REFERENCES bakers(baker_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Index for faster lookups
CREATE INDEX idx_chat_baker_user ON chat_messages(baker_id, user_id);
CREATE INDEX idx_chat_created ON chat_messages(created_at);
