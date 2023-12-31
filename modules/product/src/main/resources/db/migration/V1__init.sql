CREATE TABLE product_info (
  id                     bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name                   varchar(255) NOT NULL,
  fun_fact               text,
  additional_fun_fact    text
);
CREATE INDEX product_info__name__idx ON product_info USING HASH(name);
