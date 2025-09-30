-- Create menu_items table
CREATE TABLE menu_items (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0.01),
    image_url VARCHAR(500),
    available BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on name for faster lookups
CREATE INDEX idx_menu_items_name ON menu_items(name);

-- Create index on available for filtering
CREATE INDEX idx_menu_items_available ON menu_items(available);