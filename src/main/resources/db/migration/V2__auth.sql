CREATE TABLE users (
  id serial primary key,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  email varchar(512) unique not null,
  status char not null,
  guid uuid unique not null,
  last_active timestamp with time zone not null,
  hex_user varchar(512)
);
