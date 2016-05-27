CREATE TABLE auction_house_feeds (
  filename varchar(255) primary key,
  created datetime not null,
  updated datetime not null,
  type char not null,
  in_progress boolean not null default false,
  num_loaded int not null default 0,
  completed datetime,
);
