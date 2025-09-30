-- Insert sample Spanish chiringuito menu items
INSERT INTO menu_items (id, name, description, price, image_url, available) VALUES
    (RANDOM_UUID(), 'Paella Valenciana', 'Traditional Spanish rice dish with seafood, chicken, and vegetables', 12.50, 'https://example.com/paella.jpg', true),
    (RANDOM_UUID(), 'Tortilla Española', 'Classic Spanish potato omelette with onions', 6.00, 'https://example.com/tortilla.jpg', true),
    (RANDOM_UUID(), 'Gazpacho', 'Cold tomato soup with cucumber, peppers, and olive oil', 5.50, 'https://example.com/gazpacho.jpg', true),
    (RANDOM_UUID(), 'Calamares Fritos', 'Crispy fried squid rings served with lemon', 8.50, 'https://example.com/calamares.jpg', true),
    (RANDOM_UUID(), 'Patatas Bravas', 'Fried potatoes with spicy tomato sauce and aioli', 5.00, 'https://example.com/bravas.jpg', true),
    (RANDOM_UUID(), 'Gambas al Ajillo', 'Garlic shrimp sautéed in olive oil with chili', 10.00, 'https://example.com/gambas.jpg', true),
    (RANDOM_UUID(), 'Jamón Ibérico', 'Premium Iberian ham served with bread and tomato', 15.00, 'https://example.com/jamon.jpg', true),
    (RANDOM_UUID(), 'Pulpo a la Gallega', 'Galician-style octopus with paprika and olive oil', 14.00, 'https://example.com/pulpo.jpg', true),
    (RANDOM_UUID(), 'Ensalada Mixta', 'Mixed salad with lettuce, tomato, onion, and tuna', 7.00, 'https://example.com/ensalada.jpg', true),
    (RANDOM_UUID(), 'Churros con Chocolate', 'Fried dough pastries with thick hot chocolate', 4.50, 'https://example.com/churros.jpg', true);