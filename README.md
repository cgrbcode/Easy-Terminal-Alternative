First my sql will have to be setup. I'm assuming you can install MySql, if it isn't installed and you have admin access to the database.

Login to your mysql server with an admin account.

Create an eta database. This can be anything but logically eta is a good choice.
CREATE DATABASE eta;

Then create the user that ETA will connect as. again eta is logical. Replace password with the password of your choosing.
CREATE user 'eta'@'%' IDENTIFIED BY 'password';

Now the permission for that user must be setup. We will give this user access to only the database we just created.
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON eta.* TO 'eta'@'%';

Now we can setup the database. Download the eta_database.sql file that is in the downloads. And now execute the the sql file from mysql.
 
 USE eta;
 SOURCE sql_structure.sql;
 

You can also use a GUI and import the file if you prefer using a GUI.

Now you need to setup some settings so ETA can connect to mysql
in our war/WEB-INF folder there is a settings file, open that up and fill in some info
sqlusername: the username of the user that will connect to the mysql database
sqluserpass: the password for the user above
sqlserver: the address of the mysql server to connect to
sqlschemata: the database to use
admin-users: a comma seperated list of users that will have admin access, good idea to add yourself

