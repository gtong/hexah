CREATE TABLE auction_house_feeds (
  filename varchar(512) primary key,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  type char not null,
  in_progress boolean not null default false,
  num_loaded int not null default 0,
  completed timestamp with time zone
);

CREATE TABLE auction_house_data (
  date date not null,
  name varchar(512) not null,
  rarity int not null,
  name_key varchar(512) not null,
  currency char not null,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  trades int not null,
  low int not null,
  high int not null,
  median int not null,
  average numeric(10,2) not null,
  primary key(name_key, currency, date)
);

CREATE TABLE auction_house_aggregates (
  name varchar(512) not null,
  rarity int not null,
  name_key varchar(512) not null,
  currency char not null,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  stats json not null,
  primary key(name_key, currency)
);

CREATE TABLE hex_objects (
  id serial primary key,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  guid uuid unique not null,
  set_guid uuid,
  type char not null,
  name varchar(512) not null,
  rarity int not null,
  name_key varchar(512) not null,
  alternate_art boolean not null
);
CREATE INDEX name_idx ON hex_objects (name, type);
CREATE INDEX name_key_idx ON hex_objects (name_key);
