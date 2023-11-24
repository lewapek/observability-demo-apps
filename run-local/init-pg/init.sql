CREATE DATABASE workshop_product;
CREATE DATABASE workshop_order;

CREATE USER workshop WITH PASSWORD 'workshop';

GRANT ALL PRIVILEGES ON DATABASE workshop_product TO workshop;
GRANT ALL PRIVILEGES ON DATABASE workshop_order TO workshop;
