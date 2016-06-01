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
  currency char not null,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  trades int not null,
  low int not null,
  high int not null,
  median int not null,
  average numeric(10,2) not null,
  primary key(name, rarity, currency, date)
);

CREATE TABLE auction_house_aggregates (
  name varchar(512) not null,
  rarity int not null,
  currency char not null,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  total_trades int not null,
  total_trades_per_day numeric(10,2) not null,
  total_median numeric(10,2) not null,
  total_average numeric(10,2) not null,
  last7_trades int not null,
  last7_trades_per_day numeric(10,2) not null,
  last7_median numeric(10,2) not null,
  last7_average numeric(10,2) not null,
  primary key(name, rarity, currency)
);

CREATE TABLE hex_objects (
  id serial primary key,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  guid uuid unique not null,
  set_guid uuid not null,
  name varchar(512) not null,
  type char not null,
  rarity int not null,
  alternate_art boolean not null,
  image_path varchar(512) not null
);
CREATE INDEX name_idx ON hex_objects (name, type);