-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 30, 2025 at 04:32 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `custom-cake`
--

-- --------------------------------------------------------

--
-- Table structure for table `ai_cake_images`
--

CREATE TABLE `ai_cake_images` (
  `image_id` int(11) NOT NULL,
  `customer_name` varchar(100) DEFAULT NULL,
  `customer_input` text DEFAULT NULL,
  `ai_prompt` text DEFAULT NULL,
  `generated_image_url` text DEFAULT NULL,
  `status` enum('generated','failed') DEFAULT 'generated',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `bakers`
--

CREATE TABLE `bakers` (
  `baker_id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `shop_name` varchar(150) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bakers`
--

INSERT INTO `bakers` (`baker_id`, `name`, `shop_name`, `email`, `phone`, `address`, `password`) VALUES
(1, 'Ravi Kumar', 'Sweet Oven Bakery', 'sweetoven@gmail.com', '9876543210', 'Bangalore, Karnataka', '$2y$10$YVAb8Bo/ivUzUGl58jhxIernVlfil6DClp3Ls87c1/rmbz3/JJCyq'),
(2, 'mahesh', 'Sweet Bakery', 'mahesh@gmail.com', '9876543210', 'Bangalore, Karnataka', '$2y$10$jCWN7uujd.kdtsFzKJ61tOWE1PMiHIOstaKfIlsbd5fvhil/zaYMy');

-- --------------------------------------------------------

--
-- Table structure for table `cakes`
--

CREATE TABLE `cakes` (
  `cake_id` int(11) NOT NULL,
  `baker_id` int(11) NOT NULL,
  `cake_name` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `availability` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cakes`
--

INSERT INTO `cakes` (`cake_id`, `baker_id`, `cake_name`, `description`, `price`, `image`, `created_at`, `availability`) VALUES
(4, 1, 'Updated Chocolate Cake', 'Extra creamy chocolate cake', 999.00, '', '2025-12-17 04:32:15', 1),
(5, 1, 'Chocolate Truffle Deluxe', 'Updated chocolate truffle cake', 999.00, '', '2025-12-17 04:34:55', 1),
(7, 1, 'Chocolate Delight', 'Rich chocolate cake', 899.00, '', '2025-12-24 09:22:28', 1),
(10, 1, 'Black Forest Cake', 'Classic black forest cake with cherries', 750.00, '', '2025-12-24 09:43:35', 1),
(12, 2, 'white cream Cake', 'Classic black forest cake with cherries', 750.00, '', '2025-12-29 07:03:28', 1),
(13, 1, 'brown chocolate cake', 'very beautiful and delicious cake', 300.00, '', '2025-12-29 07:33:54', 1),
(14, 1, 'vanella', NULL, 200.00, NULL, '2025-12-29 07:55:15', 1),
(15, 1, 'choco', NULL, 100.00, NULL, '2025-12-29 08:03:08', 1),
(16, 2, 'white cream Cake', 'Classic black forest cake with cherries', 750.00, '', '2025-12-29 08:17:32', 1);

-- --------------------------------------------------------

--
-- Table structure for table `cake_colours`
--

CREATE TABLE `cake_colours` (
  `id` int(11) NOT NULL,
  `cake_id` int(11) NOT NULL,
  `colour` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cake_colours`
--

INSERT INTO `cake_colours` (`id`, `cake_id`, `colour`) VALUES
(1, 10, 'White'),
(2, 10, 'Brown'),
(3, 5, 'Brown'),
(6, 12, 'White'),
(7, 12, 'Brown'),
(8, 13, 'Purple'),
(9, 13, 'Red'),
(10, 16, 'White'),
(11, 16, 'Brown');

-- --------------------------------------------------------

--
-- Table structure for table `cake_flavours`
--

CREATE TABLE `cake_flavours` (
  `id` int(11) NOT NULL,
  `cake_id` int(11) NOT NULL,
  `flavour` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cake_flavours`
--

INSERT INTO `cake_flavours` (`id`, `cake_id`, `flavour`) VALUES
(1, 10, 'Chocolate'),
(2, 5, 'Chocolate'),
(4, 12, 'Chocolate'),
(5, 13, 'Strawberry'),
(6, 13, 'Red Velvet'),
(7, 16, 'Chocolate');

-- --------------------------------------------------------

--
-- Table structure for table `cake_shapes`
--

CREATE TABLE `cake_shapes` (
  `id` int(11) NOT NULL,
  `cake_id` int(11) NOT NULL,
  `shape` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cake_shapes`
--

INSERT INTO `cake_shapes` (`id`, `cake_id`, `shape`) VALUES
(1, 10, 'Round'),
(2, 5, 'Round'),
(3, 5, 'Heart'),
(5, 12, 'Round'),
(6, 13, 'Round'),
(7, 13, 'Square'),
(8, 16, 'Round');

-- --------------------------------------------------------

--
-- Table structure for table `cake_toppings`
--

CREATE TABLE `cake_toppings` (
  `id` int(11) NOT NULL,
  `cake_id` int(11) NOT NULL,
  `topping` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cake_toppings`
--

INSERT INTO `cake_toppings` (`id`, `cake_id`, `topping`) VALUES
(1, 10, 'Cherries'),
(2, 10, 'Chocolate Chips'),
(3, 5, 'Nuts'),
(4, 5, 'Choco Chips'),
(7, 12, 'Cherries'),
(8, 12, 'Chocolate Chips'),
(9, 13, 'Chocolate Chips'),
(10, 13, 'Edible Flowers'),
(11, 16, 'Cherries'),
(12, 16, 'Chocolate Chips');

-- --------------------------------------------------------

--
-- Table structure for table `cart`
--

CREATE TABLE `cart` (
  `cart_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `cake_id` int(11) NOT NULL,
  `quantity` int(11) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cart`
--

INSERT INTO `cart` (`cart_id`, `user_id`, `cake_id`, `quantity`, `created_at`) VALUES
(6, 2, 5, 2, '2025-12-28 11:56:30');

-- --------------------------------------------------------

--
-- Table structure for table `delivery_persons`
--

CREATE TABLE `delivery_persons` (
  `delivery_id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `vehicle` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `delivery_persons`
--

INSERT INTO `delivery_persons` (`delivery_id`, `name`, `email`, `phone`, `vehicle`, `password`) VALUES
(1, 'Arun Kumar', 'arun.deliver@gmail.com', '9876543210', 'Bike', '$2y$10$2LnNgPtLaFsFKG.nIyjVteNjmrdUwx5t0448/DzjJINyz.Bl9D.Aa');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `baker_id` int(11) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `cake_id` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT 1,
  `status` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `user_id`, `baker_id`, `total_amount`, `cake_id`, `quantity`, `status`, `created_at`) VALUES
(1, 1, 1, 1998.00, NULL, 1, NULL, '2025-12-28 12:10:10'),
(2, 1, 1, 1998.00, NULL, 1, NULL, '2025-12-28 12:10:31'),
(3, 1, 1, 1998.00, NULL, 1, NULL, '2025-12-28 12:11:42');

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `cake_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_items`
--

INSERT INTO `order_items` (`order_item_id`, `order_id`, `cake_id`, `quantity`, `price`, `created_at`) VALUES
(1, 3, 5, 2, 999.00, '2025-12-28 12:11:42');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `name`, `email`, `phone`, `address`, `password`) VALUES
(1, 'praveen', 'praveen@gmail.com', '9876543210', 'Chennai', '$2y$10$8kzw4M0nI8.Up1ajvfowueIIEE232xEj7dV5yAYQ1c2XwR21QEK.S'),
(2, 'praveen', 'praveen1@gmail.com', '9876543210', 'Chennai', '$2y$10$bna359DIIScdFyVT9rIJBuV27ggJzm.MMnAJ2SGDWzS5N7oCWHnOS'),
(3, 'sasi', 'sasi@gmail.com', '9876543210', 'Chennai', '$2y$10$ALPIhSLzeFciUhHpLQC.BeP7Hr6k1RvfYxjSmZLdOQVDsHHrXHSci'),
(4, 'sai', 'sai@gmail.com', '9848001113', 'kavali', '$2y$10$9SpTYmQIKvMP82ZA8dGEkuV0EsSGO2OYwFzZ7rZWx89vzpkTdqARC');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `ai_cake_images`
--
ALTER TABLE `ai_cake_images`
  ADD PRIMARY KEY (`image_id`);

--
-- Indexes for table `bakers`
--
ALTER TABLE `bakers`
  ADD PRIMARY KEY (`baker_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `cakes`
--
ALTER TABLE `cakes`
  ADD PRIMARY KEY (`cake_id`);

--
-- Indexes for table `cake_colours`
--
ALTER TABLE `cake_colours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cake_id` (`cake_id`);

--
-- Indexes for table `cake_flavours`
--
ALTER TABLE `cake_flavours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cake_id` (`cake_id`);

--
-- Indexes for table `cake_shapes`
--
ALTER TABLE `cake_shapes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cake_id` (`cake_id`);

--
-- Indexes for table `cake_toppings`
--
ALTER TABLE `cake_toppings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cake_id` (`cake_id`);

--
-- Indexes for table `cart`
--
ALTER TABLE `cart`
  ADD PRIMARY KEY (`cart_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `cake_id` (`cake_id`);

--
-- Indexes for table `delivery_persons`
--
ALTER TABLE `delivery_persons`
  ADD PRIMARY KEY (`delivery_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`order_item_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `ai_cake_images`
--
ALTER TABLE `ai_cake_images`
  MODIFY `image_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `bakers`
--
ALTER TABLE `bakers`
  MODIFY `baker_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `cakes`
--
ALTER TABLE `cakes`
  MODIFY `cake_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `cake_colours`
--
ALTER TABLE `cake_colours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `cake_flavours`
--
ALTER TABLE `cake_flavours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `cake_shapes`
--
ALTER TABLE `cake_shapes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `cake_toppings`
--
ALTER TABLE `cake_toppings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `cart`
--
ALTER TABLE `cart`
  MODIFY `cart_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `delivery_persons`
--
ALTER TABLE `delivery_persons`
  MODIFY `delivery_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `cake_colours`
--
ALTER TABLE `cake_colours`
  ADD CONSTRAINT `cake_colours_ibfk_1` FOREIGN KEY (`cake_id`) REFERENCES `cakes` (`cake_id`) ON DELETE CASCADE;

--
-- Constraints for table `cake_flavours`
--
ALTER TABLE `cake_flavours`
  ADD CONSTRAINT `cake_flavours_ibfk_1` FOREIGN KEY (`cake_id`) REFERENCES `cakes` (`cake_id`) ON DELETE CASCADE;

--
-- Constraints for table `cake_shapes`
--
ALTER TABLE `cake_shapes`
  ADD CONSTRAINT `cake_shapes_ibfk_1` FOREIGN KEY (`cake_id`) REFERENCES `cakes` (`cake_id`) ON DELETE CASCADE;

--
-- Constraints for table `cake_toppings`
--
ALTER TABLE `cake_toppings`
  ADD CONSTRAINT `cake_toppings_ibfk_1` FOREIGN KEY (`cake_id`) REFERENCES `cakes` (`cake_id`) ON DELETE CASCADE;

--
-- Constraints for table `cart`
--
ALTER TABLE `cart`
  ADD CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`cake_id`) REFERENCES `cakes` (`cake_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
