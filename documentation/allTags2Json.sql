select 
CONCAT(
  'db.posts.updateOne({ code: "',
  p.titleAsId, 
  '"}',
  ', {$set:{tags:',
  CONCAT("[\"", GROUP_CONCAT(DISTINCT c.name SEPARATOR "\",\""), "\"]"),
  '}});'
) 'update'
from category c inner join post_category pc on pc.categories_id=c.id inner join post p on p.id=pc.post_id
where p.titleAsId!=''
GROUP BY p.titleAsId
INTO OUTFILE '/var/lib/mysql-files/tags.json'
FIELDS TERMINATED BY ''
ENCLOSED BY ''
ESCAPED BY '' 
LINES TERMINATED BY '\n'