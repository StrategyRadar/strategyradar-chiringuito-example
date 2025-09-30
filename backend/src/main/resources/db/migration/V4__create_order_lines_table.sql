-- Create order_lines table
CREATE TABLE order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    menu_item_id UUID NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1 AND quantity <= 50),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0.01),
    line_total DECIMAL(10, 2) NOT NULL CHECK (line_total >= 0.01),
    CONSTRAINT fk_order_lines_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_lines_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Create index on order_id for fast lookups by order
CREATE INDEX idx_order_lines_order_id ON order_lines(order_id);

-- Create unique constraint to prevent duplicate menu items in same order
CREATE UNIQUE INDEX idx_order_lines_order_menu_item ON order_lines(order_id, menu_item_id);