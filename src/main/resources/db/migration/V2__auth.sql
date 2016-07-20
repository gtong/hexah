CREATE TABLE users (
  id serial primary key,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  email varchar(512) unique not null,
  status char not null,
  guid uuid unique not null,
  last_active timestamp with time zone not null,
  hex_user varchar(512),
  sell_cards boolean not null,
  sell_equipment boolean not null
);

CREATE TABLE user_items (
  user_id integer not null,
  item_guid uuid not null,
  type char not null,
  updated timestamp with time zone not null,
  number integer not null,
  sell boolean not null,
  primary key(user_id, item_guid)
);
CREATE INDEX guid_number_idx ON user_items (item_guid, number);
