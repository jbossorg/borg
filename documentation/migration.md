# Migration data to MongoDB


## Export data from  Mysql to JSONs

```
# Copy sql files  to mysql server e.g. docker:
docker cp allPosts2Json.sql mysql:.
docker cp allTags2Json.sql mysql:.

# Connect to mysql container
docker exec -it mysql bash

# Create all posts JSON
mysql -vv -u root -proot borg < allPosts2Json.sql
# Create all tags to coreponsing posts
mysql -vv -u root -proot borg < allTags2Json.sql

exit

# copy from docker to local machine
docker cp mysql:/var/lib/mysql-files/posts.json .
docker cp mysql:/var/lib/mysql-files/tags.json .
```



Mongo

```
docker run -it --rm --name mongo -p 27017:27017 mongo:3.6


docker cp posts.json mongo:.
docker cp tags.json mongo:.

docker exec -it mongo bash
```

```
# Delete any posts
mongo feeds2mongo
db.posts.deleteMany({});
exit

# Import
mongoimport --db feeds2mongo --collection posts posts.json
mongoimport --db feeds2mongo --collection posts tags.json
```
