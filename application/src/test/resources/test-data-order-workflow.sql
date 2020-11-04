TRUNCATE TABLE products;
TRUNCATE TABLE products_in_stock;
TRUNCATE TABLE deliveries;
TRUNCATE TABLE payments;

INSERT INTO products VALUES
    ('1', 'Prod 1', 'Prod 1 Desc', 1.00),
    ('2', 'Prod 2', 'Prod 2 Desc', 2.00),
    ('3', 'Prod 3', 'Prod 3 Desc', 3.00);

INSERT INTO products_in_stock VALUES
    ('1', 5),
    ('2', 0),
    ('3', 13);