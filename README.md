# SchematicTransmit
SchematicTransmit allows you upload a file to a server (especially Minecraft server). As the version is upgrading, it will allow your server players upload more kinds of files.

# Server
SchematicTransmitServer

## use
1. Every time Client started, it will upload its version string (like "1.0") to server, and server returns if it's supposed to upadte.
When the newest client exist in server fold client\, it'll be sent to user's computer so as to auto update. 
(If the newest client doesn't exist, server will tell user that client needn't update).
客户端每次开启都会向服务器发送版本号，由服务器审核该版本是否可用。可通过修改设置中的 usableVersion 项以拒绝部分过时版本启动。当版本不可用，会检查服务器端上是否有最新版本软件。若有，则会自动下载到用户的电脑上，以实现自动更新。

2. View user uploaded files and other behavioral information;
查看用户上传文件等行为信息；

3. View or modify user's relevant information (password, IP, etc.);
查看或修改用户的相关信息（密码、IP 等）；

4. View the upload and download records of specific SCHEM files;
查看特定 schem 文件的上传和下载记录；

5. View or delete the command record of the console;
查看或删除控制台的指令记录；

6. Users who are banned or unbanned (reasons are optional);
封禁和解禁用户（封禁时可带原因）；

7. Examine and approve the registration of users;
审批用户的注册行为；

# Client
## use
1. connect to server, and upload files.
连接到服务器，并上传文件。
2. register
注册新账户
