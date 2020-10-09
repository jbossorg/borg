select JSON_OBJECT('feed', f.name, 'group', g.name, 'title', p.title, 'code',p.titleAsId,'url',p.link,'author',p.author,'content', p.content, 
'published', json_object("$date", DATE_FORMAT(p.published,'%Y-%m-%dT%TZ')), 'updated', json_object("$date", DATE_FORMAT(p.modified,'%Y-%m-%dT%TZ')), 'tags', null)
from post p 
inner join remotefeed f on f.id=p.feed_id 
inner join feedgroup g on g.id=f.group_id
order by p.modified desc
INTO OUTFILE '/var/lib/mysql-files/posts.json'
FIELDS TERMINATED BY ''
ENCLOSED BY ''
ESCAPED BY '' 
LINES TERMINATED BY '\n'