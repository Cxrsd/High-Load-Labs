--EXPLAIN-анализ и аналитические запросы

-- Partition Pruning

-- Заказы за февраль 2026
-- Ожидаем: в плане ТОЛЬКО orders_2026_02, остальные партиции отсечены
-- Посмотреть на строки вида:
--   ->  Seq Scan on orders_2026_02 orders  (cost=...) (26_01,02 отсутствуют)
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT order_id,
       order_date,
       customer_id,
       total_price
FROM   orders
WHERE  order_date BETWEEN '2026-02-01' AND '2026-02-28';

-- планировщик отсекает всё кроме orders_2026_01 по дате
-- затем фильтрует по customer_id внутри одной партиции
-- Задействуется индекс idx_orders_date (Index Scan) + Filter по клиенту
EXPLAIN (ANALYZE, FORMAT TEXT)
SELECT order_id,
       order_date,
       customer_id,
       total_price
FROM   orders
WHERE  customer_id = 42
  AND  order_date BETWEEN '2026-01-01' AND '2026-01-31';

-- Дорогие заказы за весь период (все партиции)
-- Pruning не применяется (нет фильтра по дате), но на партиции февраля 
--используется локальный индекс idx_orders_2026_02_price
-- В плане видно Index Scan на orders_2026_02 и Seq Scan на остальных

EXPLAIN (ANALYZE, FORMAT TEXT)
SELECT order_id,
       order_date,
       customer_id,
       total_price
FROM   orders
WHERE  total_price > 20000
ORDER  BY total_price DESC;

-- Аналитика Топ-10 самых дорогих заказов
SELECT order_id,
       order_date,
       customer_id,
       total_price,
       total_weight
FROM   orders
ORDER  BY total_price DESC
LIMIT  10;


-- Оборот и количество заказов по месяцам (как данные реально распределились по партициям)
SELECT DATE_TRUNC('month', order_date)   AS month,
       count(*)                          AS orders_count,
       round(SUM(total_price),  2)       AS revenue,
       round(AVG(total_price),  2)       AS avg_check
FROM   orders
WHERE  order_date < '2026-04-01'
GROUP  BY 1
ORDER  BY 1;


-- Средний чек по всему периоду

SELECT round(AVG(total_price), 2) AS overall_avg_check
FROM   orders;

-- Топ-5 самых популярных товаров (по числу вхождений)

SELECT p.product_id,
       p.name,
       p.price,
       count(oi.id)     AS times_ordered,
       SUM(oi.quantity) AS total_qty_sold
FROM   order_items oi
JOIN   products    p ON p.product_id = oi.product_id
GROUP  BY p.product_id, p.name, p.price
ORDER  BY times_ordered DESC
LIMIT  5;
