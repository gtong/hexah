CREATE TABLE auction_house_feeds (
  filename varchar(255) primary key,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  type char not null,
  in_progress boolean not null default false,
  num_loaded int not null default 0,
  completed timestamp with time zone
);

CREATE TABLE hex_objects (
  id serial primary key,
  created timestamp with time zone not null,
  updated timestamp with time zone not null,
  guid uuid unique not null,
  set_guid uuid not null,
  name varchar(255) not null,
  type char not null,
  rarity int not null,
  alternate_art boolean not null
);
