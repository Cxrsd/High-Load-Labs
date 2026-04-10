-- //Создание схемы (таблицы, партиции)

-- Очистка объектов на случай перезапуска

DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders      CASCADE;
DROP TABLE IF EXISTS products    CASCADE;
DROP SEQUENCE IF EXISTS orders_order_id_seq;

-- Таблица товаров (чай)

CREATE TABLE products (
    product_id SERIAL  PRIMARY KEY,
    name       TEXT    NOT NULL,
    weight     NUMERIC NOT NULL,
    price      NUMERIC NOT NULL 
);

CREATE SEQUENCE orders_order_id_seq;

-- Родительская таблица заказов с партиционированием по дате

CREATE TABLE orders (
    order_id     BIGINT  NOT NULL DEFAULT nextval('orders_order_id_seq'),
    order_date   DATE    NOT NULL,
    customer_id  INT,
    total_price  NUMERIC,
    total_weight NUMERIC
) PARTITION BY RANGE (order_date);


-- Январь 2026
CREATE TABLE orders_2026_01 PARTITION OF orders
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- Февраль 2026
CREATE TABLE orders_2026_02 PARTITION OF orders
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- Март 2026
CREATE TABLE orders_2026_03 PARTITION OF orders
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');

-- Дефолт партиция если что-то не попадет 
CREATE TABLE orders_default PARTITION OF orders DEFAULT;

-- Таблица позиций заказа обычная (заказ с товаром через order_id и product_id)

CREATE TABLE order_items (
    id         SERIAL  PRIMARY KEY,
    order_id   BIGINT  NOT NULL,
    product_id INT     NOT NULL REFERENCES products (product_id),
    quantity   NUMERIC NOT NULL CHECK (quantity > 0)
);

-- Статус
SELECT 'Создана схемма: products, orders (p), order_items' AS status;
