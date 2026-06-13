use souvenirdb;

CREATE TABLE IF NOT EXISTS `site_settings` (
  `setting_key` VARCHAR(128) NOT NULL,
  `setting_value` TEXT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_carts` (
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Default settings used by the admin settings UI
INSERT INTO `site_settings` (`setting_key`, `setting_value`) VALUES
  ('site_name', 'INOLA'),
  ('site_email', 'support@inola.com'),
  ('site_phone', '+84 123 456 789'),
  ('site_address', 'Trường Đại Học Nông Lâm TP.HCM'),
  ('payment_cod', 'true'),
  ('payment_vnpay', 'true'),
  ('payment_momo', 'true'),
  ('payment_card', 'true'),
  ('shipping_ghn', 'true'),
  ('shipping_ghtk', 'true'),
  ('shipping_jnt', 'true'),
  ('default_language', 'vi'),
  ('default_currency', 'VND'),
  ('items_per_page', '12'),
  ('tax_rate', '0.00'),
  ('maintenance_mode', 'true'),
  ('site_logo_url', ''),
  ('meta_description', ''),
  ('social_facebook', ''),
  ('social_instagram', '');
