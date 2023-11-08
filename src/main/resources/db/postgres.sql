create table if not exists public.video
(
    id            bigint not null
        constraint video_pk
            primary key,
    video_id      bigint,
    url           varchar(512),
    real_url      varchar(1024),
    title         varchar(512),
    image         varchar(1024),
    duration      varchar(32),
    author        varchar(32),
    look_num      integer,
    collect_num   integer,
    m3u8_content  text,
    video_path    varchar(1024),
    video_website integer,
    publish_time  timestamp,
    create_time   timestamp,
    update_time   timestamp
);

create index video_video_id_index
    on public.video (video_id);

create index video_create_time_index
    on public.video (create_time);

create index video_video_website_index
    on public.video (video_website);

